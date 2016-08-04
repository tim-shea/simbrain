package org.simbrain.custom.rl_sim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.network.layouts.LineLayout;
import org.simbrain.network.subnetworks.WinnerTakeAll;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.OdorWorldXML;
import org.simbrain.simulation.OdorWorldXML.EntityDescription;
import org.simbrain.simulation.PlotBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.simulation.Vehicle;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.SimbrainConstants.Polarity;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;

/**
 * A reinforcement learning simulation in which an agent learns to associate
 * smells with different pursuer / avoider combinations.
 *
 * TODO: Add .htmlfile to folder and make docs based on that
 */
//CHECKSTYLE:OFF
public class RL_Sim {

    /** The main simulation desktop. */
    final Simulation sim;

    /** List of vehicles. */
    List<NeuronGroup> vehicles = new ArrayList<NeuronGroup>();

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

    /** Size of the (square) world.  */
    int worldSize = 350;
    
    /** Distance in pixels within which a goal object is counted as being arrived at. */
    double hitRadius = 50;
    
    /** Initial mouse location. */
    int initialMouseLocation_x = 43;
    int initialMouseLocation_y = 110;
    int initialMouseHeading = 0;

    /** Other variables and references. */
    JInternalFrame controlPanelFrame;
    boolean stop = false;
    boolean goalAchieved = false;
    Network network;
    RotatingEntity mouse;
    List<OdorWorldEntity> worldEntities = new ArrayList<OdorWorldEntity>();
    List<OdorWorldEntity> goalEntities = new ArrayList<OdorWorldEntity>();
    Neuron reward;
    Neuron value;
    Neuron tdError;
    Neuron deltaReward;
    NeuronGroup rightInputs, leftInputs;
    SynapseGroup rightInputOutput, leftInputOutput;
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
    public RL_Sim(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
    }

    /**
     * Run the simulation!
     */
    public void run() {

        // TODO: Below is a problem. This is running on the EDT which explains
        // the poor performance. Need to add utilities to Simbrain to make
        // sure tasks are "loaded" on the right threads
        // System.out.println(SwingUtilities.isEventDispatchThread());

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Create the network builder
        NetBuilder net = sim.addNetwork(223, 1, 616, 615, "Neural Network");
        network = net.getNetwork();

        // Create the odor world builder
        OdorWorldBuilder world = sim.addOdorWorld(826, 1, worldSize, worldSize,
                "Simple World");
        world.getWorld().setObjectsBlockMovement(true);
        world.getWorld().setWrapAround(false);

        // Create a control panel
        controlPanelFrame = sim.addFrame(-6, 1, "RL Controls");
        makeControlPanel();

        // Set up the odor world
        setUpWorld(world);

        // Set up the main input-output network that is trained via RL
        setUpInputOutputNetwork(net);

        // Set up the reward and td error nodes
        setUpRLNodes(net);

        // Clear all learnable weights
        clearWeights();

        // Set up the vehicle networks
        setUpVehicleNets(net, world);

        // Set up the time series plot
        setUpTimeSeries(net);

        // Configure a custom update rule
        updateMethod = new RL_Update(this);
        net.getNetwork().getUpdateManager().clear();
        net.getNetwork().addUpdateAction(updateMethod);

    }

    /**
     * Set up the world
     */
    private void setUpWorld(OdorWorldBuilder world) {

        // TODO: make sure this works when Simbrain is deployed
        File xmlFile = new File(
                "src/org/simbrain/custom/rl_sim/worldDescription.xml");
        worldEntities = world.loadWorld(xmlFile);

        // First two objects are the "goal" entities
        goalEntities.add(worldEntities.get(0));
        goalEntities.add(worldEntities.get(1));

        // Add the agent
        mouse = world.addAgent(initialMouseLocation_x, initialMouseLocation_y,
                "Mouse");
        mouse.setHeading(initialMouseHeading);

    }

    /**
     * Set up the time series plot.
     */
    private void setUpTimeSeries(NetBuilder net) {
        // Create a time series plot
        PlotBuilder plot = sim.addTimeSeriesPlot(832,353, 293, 332,
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
        outputs = net.addWTAGroup(-234, 58, 2);
        outputs.setUseRandom(true);
        outputs.setRandomProb(epsilon);
        // Add a little extra spacing between neurons to accommodate labels
        outputs.setLayout(
                new LineLayout(80, LineLayout.LineOrientation.HORIZONTAL));
        outputs.applyLayout();
        outputs.setLabel("Outputs");

        // Inputs
        rightInputs = net.addNeuronGroup(-104, 350, 5);
        rightInputs.setLabel("Right Inputs");
        rightInputs.setClamped(true);
        leftInputs = net.addNeuronGroup(-481, 350, 5);
        leftInputs.setLabel("Left Inputs");
        leftInputs.setClamped(true);

        // Connections
        rightInputOutput = net.addSynapseGroup(rightInputs, outputs);
        sim.couple(mouse, rightInputs, 2);
        leftInputOutput = net.addSynapseGroup(leftInputs, outputs);
        sim.couple(mouse, leftInputs, 1);
    }

    /**
     * Set up the reward, value and td nodes
     */
    private void setUpRLNodes(NetBuilder net) {
        reward = net.addNeuron(300, 0);
        reward.setClamped(true);
        reward.setLabel("Reward");
        sim.couple(mouse.getSensor("Smell-Center"), 5, reward);
        value = net.addNeuron(350, 0);
        value.setLabel("Value");
        net.connectAllToAll(leftInputs, value);

        tdError = net.addNeuron(400, 0);
        tdError.setLabel("TD Error");

        // TODO
        deltaReward = net.addNeuron(300, -50);
        deltaReward.setClamped(true);
        deltaReward.setLabel("Delta Reward");
    }

    /**
     * Set up the vehicle networks
     */
    private void setUpVehicleNets(NetBuilder net, OdorWorldBuilder world) {
        // Labels for vehicles, which must be the same as the label for
        // the corresponding output node
        String strPursueCheese = "Pursue Cheese";
        String strAvoidCheese = "Avoid Cheese";
        String strPursueFlower = "Pursue Flower";
        String strAvoidFlower = "Avoid Flower";
        String strPursueCandle = "Pursue Candle";
        String strAvoidCandle = "Avoid Candle";

        // Make the vehicle networks
        // Positions determined by laying by hand and in console running
        // print(getNetwork("Neural Network"));
        Vehicle vehicleBuilder = new Vehicle(sim, net, world);
        NeuronGroup pursueCheese = vehicleBuilder.addPursuer(-509, -460, mouse,
                1);
        pursueCheese.setLabel(strPursueCheese);
        setUpVehicle(pursueCheese);
        NeuronGroup avoidCheese = vehicleBuilder.addAvoider(-340, -247, mouse,
                1);
        avoidCheese.setLabel(strAvoidCheese);
        setUpVehicle(avoidCheese);
        NeuronGroup pursueFlower = vehicleBuilder.addPursuer(-171, -469, mouse,
                4);
        pursueFlower.setLabel(strPursueFlower);
        setUpVehicle(pursueFlower);
        NeuronGroup avoidFlower = vehicleBuilder.addAvoider(-41, -240, mouse,
                4);
        avoidFlower.setLabel(strAvoidFlower);
        setUpVehicle(avoidFlower);
        NeuronGroup pursueCandle = vehicleBuilder.addAvoider(163, -475, mouse,
                3);
        pursueCandle.setLabel(strPursueCandle);
        setUpVehicle(pursueCandle);
        NeuronGroup avoidCandle = vehicleBuilder.addAvoider(218, -239, mouse,
                3);
        avoidCandle.setLabel(strAvoidCandle);
        setUpVehicle(avoidCandle);

        // Label output nodes according to the subnetwork they control.
        // The label is also used in RL_Update to enable or disable vehicle
        // subnets
        outputs.getNeuronList().get(0).setLabel(strPursueCheese);
        outputs.getNeuronList().get(1).setLabel(strAvoidCheese);
        // outputs.getNeuronList().get(2).setLabel(strPursueFlower);
        // outputs.getNeuronList().get(3).setLabel(strAvoidFlower);
        // outputs.getNeuronList().get(4).setLabel(strPursueCandle);
        // outputs.getNeuronList().get(5).setLabel(strAvoidCandle);

        // Connect output nodes to vehicle speed nodes
        // net.connect(outputs.getNeuronByLabel(strPursueCheese),
        // pursueCheese.getNeuronByLabel("Speed"), 10);
        // net.connect(outputs.getNeuronByLabel(strAvoidCheese),
        // avoidCheese.getNeuronByLabel("Speed"), 10);
        // net.connect(outputs.getNeuronByLabel(strPursueFlower),
        // pursueFlower.getNeuronByLabel("Speed"), 10);
        // net.connect(outputs.getNeuronByLabel(strAvoidFlower),
        // avoidFlower.getNeuronByLabel("Speed"), 10);
        // net.connect(outputs.getNeuronByLabel(strPursueCandle),
        // pursueCandle.getNeuronByLabel("Speed"), 10);
        // net.connect(outputs.getNeuronByLabel(strAvoidCandle),
        // avoidCandle.getNeuronByLabel("Speed"), 10);
    }

    /**
     * Helper method to set up vehicles to this sim's specs.
     *
     * @param vehicle vehicle to modify
     */
    private void setUpVehicle(NeuronGroup vehicle) {
        Neuron toUpdate = vehicle.getNeuronByLabel("Speed");
        toUpdate.setUpdateRule("LinearRule");
        toUpdate.setActivation(0);
        toUpdate.setUpperBound(100);
        toUpdate.setClamped(false);
        Neuron turnLeft = vehicle.getNeuronByLabel("Left");
        turnLeft.setUpperBound(200);
        Neuron turnRight = vehicle.getNeuronByLabel("Right");
        turnRight.setUpperBound(200);
        vehicles.add(vehicle);
    }

    /**
     * Clear all learnable weights
     */
    void clearWeights() {
        rightInputOutput.setStrength(0, Polarity.BOTH);
        leftInputOutput.setStrength(0, Polarity.BOTH);
        for (Synapse synapse : value.getFanIn()) {
            synapse.setStrength(0);
        }
        network.fireNeuronsUpdated();
        if (updateMethod != null) {
            updateMethod.initMap(); // TODO: Is this needed?
        }
    }

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
                        updateGoalState();
                    }

                }

                // Reset the text in the trial field
                trialField.setText("" + numTrials);
            }
        });
    }

    /**
     * What counts as achieving a goal is codified here.
     */
    void updateGoalState() {
        for (OdorWorldEntity entity : goalEntities) {
            int distance = (int) SimbrainMath.distance(
                    mouse.getCenterLocation(), entity.getCenterLocation());
            if (distance < hitRadius) {
                goalAchieved = true;
            }
        }
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

        // Reset the positions of the mouse
        mouse.setLocation(initialMouseLocation_x, initialMouseLocation_y);
        mouse.setHeading(initialMouseHeading);
    }

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
     * Set up control panel.
     */
    void makeControlPanel() {

        LabelledItemPanel panel = new LabelledItemPanel();
        controlPanelFrame.add(panel);

        lambdaField.setText("" + lambda);
        trialField.setText("" + numTrials);
        panel.addItem("Trials", trialField);
        discountField.setText("" + gamma);
        panel.addItem("Discount (gamma)", discountField);
        epsilonField.setText("" + epsilon);
        panel.addItem("Epsilon", epsilonField);
        alphaField.setText("" + alpha);
        panel.addItem("Learning rt. (alpha)", alphaField);

        // Run Button
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                runTrial();
            }
        });
        panel.addItem("Simulation", runButton);

        // Stop Button
        JButton stopButton = new JButton("Stop");
        panel.addItem("Stop simulation", stopButton);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goalAchieved = true;
                stop = true;
            }
        });

        // Clear Weights Button
        JButton clearButton = new JButton("Clear");
        panel.addItem("Clear weights", clearButton);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearWeights();
            }
        });

        controlPanelFrame.pack();
    }
}
