package org.simbrain.custom.actor_critic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.layouts.LineLayout;
import org.simbrain.network.subnetworks.WinnerTakeAll;
import org.simbrain.network.synapse_update_rules.StaticSynapseRule;
import org.simbrain.network.util.NeuronWithMemory;
import org.simbrain.simulation.ControlPanel;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.PlotBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.util.environment.SmellSource;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.OdorWorld;
import org.simbrain.world.odorworld.entities.BasicEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;
import org.simbrain.world.odorworld.sensors.TileSensor;

/**
 * Class to build RL Simulation.
 *
 * TODO: Add .htmlfile to folder and make docs based on that
 */
// CHECKSTYLE:OFF
public class ActorCritic_Main {

    /** The main simulation desktop. */
    final Simulation sim;

    /** Number of trials per run. */
    int numTrials = 5;

    /** Learning Rate. */
    double alpha = 1;

    /**
     * Eligibility trace. 0 for no trace; 1 for permanent trace. .9 default. Not
     * currently used.
     */
    double lambda = 0;

    /** Prob. of taking a random action. "Exploitation" vs. "exploration". */
    double epsilon = .25;

    /**
     * Discount factor . 0-1. 0 predict next value only. .5 predict future
     * values. As it increases toward one, values of y in the more distant
     * future become more significant.
     */
    double gamma = .4;

    /** GUI Variables. */
    ControlPanel controlPanel;
    JTabbedPane tabbedPane = new JTabbedPane();

    /** Other variables and references. */
    boolean stop = false;
    boolean goalAchieved = false;
    OdorWorld world;
    OdorWorldBuilder ob;
    PlotBuilder plot;
    
    /** Tile World. */
    int numTiles = 5; // Number of rows / cols in each tileset
    int worldWidth = 320; 
    int worldHeight = 320; 
    double initTilesX = 100;
    double initTilesY = 100;
    int tileSets = 1; // Number of tilesets
    int tileSize = worldHeight / numTiles;
    double rewardDispersionFactor = 2; // Number of tiles for reward to disperse
    double movementFactor = 1; // Number of tiles to move
    double tileIncrement = (worldHeight / numTiles) / tileSets;
    double hitRadius = rewardDispersionFactor * (tileSize/2);
    int location = (tileSize*numTiles) - tileSize/2;

    /** Entities that a simulation can refer to. */
    RotatingEntity mouse;
    BasicEntity cheese; //TODO: Change to goal or generify like RL_Sim?

    /** Couplings. */
    List effectorCouplings;
    List sensorCouplings;

    /** Neural net variables. */
    Network network;
    List<Neuron> tileNeurons;
    Neuron reward;
    Neuron value;
    Neuron tdError;
    double preditionError; // used to set "confidence interval" on plot halo
    WinnerTakeAll outputs;
    JTextField trialField = new JTextField();
    JTextField discountField = new JTextField();
    JTextField alphaField = new JTextField();
    JTextField lambdaField = new JTextField();
    JTextField epsilonField = new JTextField();
    RL_Update updateMethod;

    /**
     * Construct the reinforcement learning simulation.
     *
     * @param desktop
     */
    public ActorCritic_Main(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
    }

    /**
     * Run the simulation!
     */
    public void run() {
        buildSim();
    }
    
    //TODO: Break in to parts
    public void buildSim() {

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Create the network builder
        NetBuilder net = sim.addNetwork(252,0,563,597, "Neural Network");
        network = net.getNetwork();

        // Set up the control panel and tabbed pane
        makeControlPanel();
        controlPanel.addBottomComponent(tabbedPane);

        // Set up the main input-output network that is trained via RL
        setUpInputOutputNetwork(net);

        // Set up the reward and td error nodes
        setUpRLNodes(net);

        // Set up the tile world
        setUpWorld();

        // Clear all learnable weights
        //clearWeights();

        // Set up the time series plot
        setUpTimeSeries(net);


        // Set up projection plot
//        plot = sim.addProjectionPlot(798,326,355,330,
//                "Sensory states + Predictions");
//        plot.getProjectionModel().init(leftInputs.size() + rightInputs.size());
//        plot.getProjectionModel().getProjector().setTolerance(.01);
//        // TODO: Use annotations
//        Producer inputProducer = net.getNetworkComponent().createProducer(this,
//                "getCombinedInputs", double[].class);
//        Consumer plotConsumer = plot.getProjectionPlotComponent()
//                .createConsumer(plot.getProjectionPlotComponent(), "addPoint",
//                        double[].class);
//        sim.addCoupling(new Coupling(inputProducer, plotConsumer));

        // Set custom network update
        updateMethod = new RL_Update(this);
        addCustomAction();

        // Add workspace level update action
//        sim.getWorkspace().addUpdateAction(new ColorPlot(this));

    }
    
    void setUpWorld() {
        ob = sim.addOdorWorld(806, 11, worldWidth,worldHeight, "Tile World");
        world = ob.getWorld();
        world.setObjectsBlockMovement(true);
        world.setWrapAround(false);
        mouse = new RotatingEntity(world);
        mouse.setCenterLocation(location,location);
        world.addAgent(mouse);
        cheese = new BasicEntity("Swiss.gif", world);
        double dispersion = rewardDispersionFactor * (tileSize/2);
        cheese.setCenterLocation(tileSize/2, tileSize/2);
        cheese.setSmellSource(
                new SmellSource(new double[]{1,0},
                        SmellSource.DecayFunction.STEP, 
                        dispersion,
                        cheese.getCenterLocation()));
        world.addEntity(cheese);
        tileNeurons = new ArrayList();
        sensorCouplings = new ArrayList();
        for(int i = 0; i < tileSets; i++) {
            for (int j = 0; j < numTiles; j++) {
                for(int k = 0; k < numTiles; k++) {
                    double x = (j * tileSize) -i * tileIncrement;
                    double y = (k * tileSize) -i * tileIncrement;
                    TileSensor sensor = new TileSensor(mouse, (int) x, (int) y,
                            tileSize, tileSize);
                    mouse.addSensor(sensor);
                    NeuronWithMemory tileNeuron;
                    if (lambda == 0) {
                        tileNeuron = new NeuronWithMemory(network, "LinearRule");                    
                    } else {
                        tileNeuron = new NeuronWithMemory(network, "DecayRule");
                    }
                    tileNeurons.add(tileNeuron);
                    tileNeuron.setX(initTilesX + (double)x);
                    tileNeuron.setY(initTilesY + (double)y);
                    network.addNeuron(tileNeuron);
                    
                    //TODO
//                    PotentialProducer tileProducer = worldComponent.getAttributeManager().createPotentialProducer(sensor, "getValue", double.class);
//                    tileProducer.setCustomDescription(sensor.getLabel());
//                    PotentialConsumer neuronConsumer = networkComponent.getAttributeManager().createPotentialConsumer(tileNeuron, "setInputValue", double.class);
//                    neuronConsumer.setCustomDescription(tileNeuron.getId());
//                    Coupling tileCoupling = new Coupling(tileProducer, neuronConsumer);
//                    sensorCouplings.add(tileCoupling);
//                    workspace.getCouplingManager().addCoupling(tileCoupling);
                    // Sensor neurons to action neurons
                    
                    for (Neuron actionNeuron : outputs.getNeuronList()) {
                        Synapse synapse = new Synapse(tileNeuron, actionNeuron, new StaticSynapseRule());
                        network.addSynapse(synapse);
                        synapse.setStrength(0);
                    }
                    // Sensor neurons to value neuron
                    Synapse synapse = new Synapse(tileNeuron, value, new StaticSynapseRule());
                    synapse.setStrength(.01);
                    network.addSynapse(synapse);
                }
            }
        }
        
//        // Add one smell coupling
//        PotentialProducer smell = worldComponent.getAttributeManager().createPotentialProducer(world.getSensor(mouse.getId(),"Sensor_2"), "getCurrentValue", double.class, new Class[]{int.class}, new Object[]{0}); 
//        smell.setCustomDescription("Reward");
//        PotentialConsumer reward = networkComponent.getAttributeManager().createPotentialConsumer(rewardNeuron, "setInputValue", double.class); 
//        reward.setCustomDescription("Reward neuron");
//        Coupling rewardCoupling = new Coupling(smell, reward);
//        sensorCouplings.add(rewardCoupling);
//        workspace.getCouplingManager().addCoupling(rewardCoupling);
//
//        // Absolute movement couplings
//        effectorCouplings = new ArrayList();
//        PotentialProducer northProducer = networkComponent.getAttributeManager().createPotentialProducer(northNeuron, "getActivation", double.class); 
//        PotentialConsumer northMovement = worldComponent.getAttributeManager().createPotentialConsumer(mouse, "moveNorth", double.class); 
//        northMovement.setCustomDescription("North");
//        Coupling northCoupling = new Coupling(northProducer, northMovement);
//        effectorCouplings.add(northCoupling);
//        workspace.getCouplingManager().addCoupling(northCoupling);
//        
//        PotentialProducer southProducer = networkComponent.getAttributeManager().createPotentialProducer(southNeuron, "getActivation", double.class); 
//        PotentialConsumer southMovement = worldComponent.getAttributeManager().createPotentialConsumer(mouse, "moveSouth", double.class); 
//        southMovement.setCustomDescription("South");
//        Coupling southCoupling = new Coupling(southProducer, southMovement);
//        effectorCouplings.add(southCoupling);
//        workspace.getCouplingManager().addCoupling(southCoupling);
//        
//        PotentialProducer eastProducer = networkComponent.getAttributeManager().createPotentialProducer(eastNeuron, "getActivation", double.class); 
//        PotentialConsumer eastMovement = worldComponent.getAttributeManager().createPotentialConsumer(mouse, "moveEast", double.class); 
//        eastMovement.setCustomDescription("East");
//        Coupling eastCoupling = new Coupling(eastProducer, eastMovement);
//        effectorCouplings.add(eastCoupling);
//        workspace.getCouplingManager().addCoupling(eastCoupling);
//        
//        PotentialProducer westProducer = networkComponent.getAttributeManager().createPotentialProducer(westNeuron, "getActivation", double.class); 
//        PotentialConsumer westMovement = worldComponent.getAttributeManager().createPotentialConsumer(mouse, "moveWest", double.class); 
//        westMovement.setCustomDescription("West");
//        Coupling westCoupling = new Coupling(westProducer, westMovement);
//        effectorCouplings.add(westCoupling);
//        workspace.getCouplingManager().addCoupling(westCoupling);
    }


    /**
     * Add the custom action which handles RL updates.
     */
    void addCustomAction() {
        network.getUpdateManager().clear();
        network.addUpdateAction(updateMethod);
    }

    /**
     * Remove the custom action which handles RL Updates. Useful to be able to
     * remove it sometimes while running other simulations.
     */
    void removeCustomAction() {
        network.getUpdateManager().clear();
    }

    /**
     * Set up the time series plot.
     */
    private void setUpTimeSeries(NetBuilder net) {
        // Create a time series plot
        PlotBuilder plot = sim.addTimeSeriesPlot(810,340,293,332,
                "Reward, TD Error");
        sim.couple(net.getNetworkComponent(), reward,
                plot.getTimeSeriesComponent(), 0);
        sim.couple(net.getNetworkComponent(), tdError,
                plot.getTimeSeriesComponent(), 1);
        plot.getTimeSeriesModel().setAutoRange(false);
        plot.getTimeSeriesModel().setRangeUpperBound(2);
        plot.getTimeSeriesModel().setRangeLowerBound(-1);
    }

    /**
     * Add main input-output network to be trained by RL.
     */
    private void setUpInputOutputNetwork(NetBuilder net) {

        // Outputs
        outputs = net.addWTAGroup(-43, 7, 4);
        outputs.setUseRandom(true);
        outputs.setRandomProb(epsilon);
        // Add a little extra spacing between neurons to accommodate labels
        outputs.setLayout(
                new LineLayout(80, LineLayout.LineOrientation.HORIZONTAL));
        outputs.applyLayout();
        outputs.setLabel("Outputs");


//        // Connections
//        inputToOutput = net.addSynapseGroup(inputs, outputs);
//        sim.couple((SmellSensor) mouse.getSensors().get(2), inputs);
//
//        // TODO: Move to a new method
//        // Prediction Network
//        predictionLeft = net.addNeuronGroup(-589.29, 188.50, 5);
//        predictionLeft.setLabel("Predicted (L)");
//        predictionRight = net.addNeuronGroup(126, 184, 5);
//        predictionRight.setLabel("Predicted (R)");
//        rightInputToRightPrediction = net.addSynapseGroup(rightInputs,
//                predictionRight);
//        outputToRightPrediction = net.addSynapseGroup(outputs, predictionRight);
//        leftInputToLeftPrediction = net.addSynapseGroup(leftInputs,
//                predictionLeft);
//        outputToLeftPrediction = net.addSynapseGroup(outputs, predictionLeft);
    }

    /**
     * Set up the reward, value and td nodes
     */
    private void setUpRLNodes(NetBuilder net) {
        reward = net.addNeuron(300, 0);
        reward.setClamped(true);
        reward.setLabel("Reward");
        //sim.couple((SmellSensor) mouse.getSensor("Smell-Center"), 5, reward);
        value = net.addNeuron(350, 0);
        value.setLabel("Value");

        tdError = net.addNeuron(400, 0);
        tdError.setLabel("TD Error");

    }

//    /**
//     * Clear all learnable weights
//     */
//    void clearWeights() {
//        inputToOutput.setStrength(0, Polarity.BOTH);
//        for (Synapse synapse : value.getFanIn()) {
//            synapse.setStrength(0);
//        }
//        network.fireNeuronsUpdated();
//    }

    /**
     * Run one trial from an initial state until it reaches cheese.
     */
    void runTrial() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                // At the beginning of each trial, load the values
                // from the control panel in.
                numTrials = Integer.parseInt(trialField.getText());
                gamma = Double.parseDouble(discountField.getText());
                lambda = Double.parseDouble(lambdaField.getText());
                epsilon = Double.parseDouble(epsilonField.getText());
                alpha = Double.parseDouble(alphaField.getText());
                outputs.setRandomProb(epsilon);
                stop = false;

                // Run the trials
                for (int i = 1; i < numTrials + 1; i++) {
                    if (stop) {
                        return;
                    }
                    resetTrial(i);

                    // Keep iterating until the mouse achieves its goal
                    // Goal is currently to get near a cheese
                    while (!goalAchieved) {
                        iterateSimulation();
                        //updateGoalState();
                    }

                }

                // Reset the text in the trial field
                trialField.setText("" + numTrials);
            }
        });
    }

    /**
     * Set up a new trial. Reset things as needed.
     *
     * @param trialNum the trial to set
     */
    void resetTrial(int trialNum) {
        // Set up the trial
        trialField.setText("" + ((numTrials + 1) - trialNum));
        goalAchieved = false;

        // Clear network activations between trials
        network.clearActivations();

//        resetMouse();
    }



    // TODO: All iteration methods must go to workspace level!

    /**
     * Iterate the workspace one iteration.
     */
    void iterateSimulation() {
        CountDownLatch latch = new CountDownLatch(1);
        sim.getWorkspace().iterate(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate for a set number of iterations.
     *
     * @param iterations number of iteration.
     */
    void iterate(int iterations) {
        CountDownLatch iterationLatch = new CountDownLatch(1);
        sim.getWorkspace().iterate(iterationLatch, iterations);
        try {
            iterationLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the top-level control panel.
     */
    void makeControlPanel() {

        // Create control panel
        controlPanel = ControlPanel.makePanel(sim, "RL Controls", -6, 1);

        // Set up text fields
        trialField = controlPanel.addTextField("Trials", "" + numTrials);
        discountField = controlPanel.addTextField("Discount (gamma)",
                "" + gamma);
        lambdaField = controlPanel.addTextField("Lambda", "" + lambda);
        epsilonField = controlPanel.addTextField("Epsilon", "" + epsilon);
        alphaField = controlPanel.addTextField("Learning rt.", "" + alpha);

        // Run Button
        controlPanel.addButton("Run", () -> {
            runTrial();
        });

        // Stop Button
        controlPanel.addButton("Stop", () -> {
            goalAchieved = true;
            stop = true;
        });

//        // Clear Weights Button
//        controlPanel.addButton("Clear", () -> {
//            clearWeights();
//        });

    }

}
