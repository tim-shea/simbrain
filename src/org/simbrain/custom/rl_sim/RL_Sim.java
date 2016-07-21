package org.simbrain.custom.rl_sim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.network.layouts.LineLayout;
import org.simbrain.network.subnetworks.WinnerTakeAll;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
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

    // TODO
    int worldSize = 300;
    double hitRadius = 75;
    int initialMouseLocation_x = 30;
    int initialMouseLocation_y = 30;
    
    JInternalFrame controlPanelFrame;
    boolean stop = false;
    boolean goalAchieved = false;
    Network network;
    RotatingEntity mouse;
    OdorWorldEntity cheese;
    Neuron reward;
    Neuron value;
    Neuron tdError;
    Neuron deltaReward;
    NeuronGroup inputs;
    WinnerTakeAll outputs;
    JTextField trialField = new JTextField();
    JTextField discountField = new JTextField();
    JTextField alphaField = new JTextField();
    JTextField lambdaField = new JTextField();
    JTextField epsilonField = new JTextField();
    
    SynapseGroup inputOutputConnection;

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
        
        // TODO: Below is a problem.  This is running on the EDT which explains
        //  the poor performance.  Need to add utilities to Simbrain to make 
        // sure tasks are "loaded" on the right threads
        // System.out.println(SwingUtilities.isEventDispatchThread());
        
        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Create the network builder
        NetBuilder net = sim.addNetwork(223, 1, 616, 615, "Neural Network");
        network = net.getNetwork();

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(826, 1, worldSize, worldSize,
                "Simple World");
        world.getWorld().setObjectsBlockMovement(true);
        world.getWorld().setWrapAround(true);

        // Create a control panel
        controlPanelFrame = sim.addFrame(-6, 1, "RL Controls");
        makeControlPanel();

        // Add the agent
        mouse = world.addAgent(initialMouseLocation_x, initialMouseLocation_y,
                "Mouse");

        // Set up the odor world with objects. Last component is reward
        cheese = world.addEntity(159, 159, "Swiss.gif",
                new double[] { 0, 1, 0, 0, 0, 1 });
        cheese.getSmellSource().setDispersion(400);
//        OdorWorldEntity flower = world.addEntity(63, 293, "Flax.gif",
//                new double[] { 0, 0, 0, 0, 1, 0 });
//        flower.getSmellSource().setDispersion(200);
//        OdorWorldEntity candle = world.addEntity(168, 142, "Candle.png",
//                new double[] { 0, 0, 0, 1, 0, 0 });
//        candle.getSmellSource().setDispersion(200);

        // Add main input-output network to be trained by RL
        outputs = net.addWTAGroup(-234, 58, 6);
        outputs.setUseRandom(true);
        outputs.setRandomProb(epsilon);
        // Add a little extra spacing between neurons to accommodate labels
        outputs.setLayout(
                new LineLayout(80, LineLayout.LineOrientation.HORIZONTAL));
        outputs.applyLayout();
        outputs.setLabel("Outputs");
        inputs = net.addNeuronGroup(-128, 350, 5);
        inputs.setLabel("Inputs");
        inputs.setClamped(true);
        inputOutputConnection = net.addSynapseGroup(inputs,
                outputs);
        sim.couple(mouse, inputs);

        // Reward, Value TD
        reward = net.addNeuron(300, 0);
        reward.setClamped(true);
        reward.setLabel("Reward");
        sim.couple(mouse.getSensor("Smell-Center"), 5, reward);
        value = net.addNeuron(350, 0);
        value.setLabel("Value");
        net.connectAllToAll(inputs, value);
        tdError = net.addNeuron(400, 0);
        tdError.setLabel("TD Error");
        
        //TODO
        deltaReward = net.addNeuron(300, -50);
        deltaReward.setClamped(true);
        deltaReward.setLabel("Delta Reward");
        
        // Clear all learnable weights
        clearWeights();


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
        outputs.getNeuronList().get(2).setLabel(strPursueFlower);
        outputs.getNeuronList().get(3).setLabel(strAvoidFlower);
        outputs.getNeuronList().get(4).setLabel(strPursueCandle);
        outputs.getNeuronList().get(5).setLabel(strAvoidCandle);

        // Connect output nodes to vehicle speed nodes
//        net.connect(outputs.getNeuronByLabel(strPursueCheese),
//                pursueCheese.getNeuronByLabel("Speed"), 10);
//        net.connect(outputs.getNeuronByLabel(strAvoidCheese),
//                avoidCheese.getNeuronByLabel("Speed"), 10);
//        net.connect(outputs.getNeuronByLabel(strPursueFlower),
//                pursueFlower.getNeuronByLabel("Speed"), 10);
//        net.connect(outputs.getNeuronByLabel(strAvoidFlower),
//                avoidFlower.getNeuronByLabel("Speed"), 10);
//        net.connect(outputs.getNeuronByLabel(strPursueCandle),
//                pursueCandle.getNeuronByLabel("Speed"), 10);
//        net.connect(outputs.getNeuronByLabel(strAvoidCandle),
//                avoidCandle.getNeuronByLabel("Speed"), 10);

        // Add custom update rule
        RL_Update rl = new RL_Update(this);
        net.getNetwork().getUpdateManager().clear();
        net.getNetwork().addUpdateAction(rl);

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
        inputOutputConnection.setStrength(0, Polarity.BOTH);
        for(Synapse synapse : value.getFanIn()) {
            synapse.setStrength(0);
        }
        network.fireNeuronsUpdated();
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
                for (int i = 1; i < numTrials + 1; i++) {

                    if (stop) {
                        return;
                    }

                    // Set up the trial
                    trialField.setText("" + ((numTrials + 1) - i));
                    goalAchieved = false;

                    // Clear network activations between trials
                    network.clearActivations();

                    // Reset the positions of the mouse
                    mouse.setLocation(initialMouseLocation_x,
                            initialMouseLocation_y);
                    mouse.setHeading(0);

                    // Keep iterating until the mouse achieves its goal
                    // Goal is currently to get near the cheese
                    while (!goalAchieved) {
                        int distance = (int) SimbrainMath.distance(
                                mouse.getCenterLocation(),
                                cheese.getCenterLocation());
                        if (distance < hitRadius) {
                            goalAchieved = true;
                        }
                        CountDownLatch latch = new CountDownLatch(1);
                        sim.getWorkspace().iterate(latch);
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                trialField.setText("" + numTrials);
            }
        });
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
