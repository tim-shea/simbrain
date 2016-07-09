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

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.simulation.Vehicle;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;

/**
 * A reinforcement learning simulation in which an agent learns to associate
 * smells with different pursuer / avoider combinations.
 * 
 * TODO: More documentation as this evolves...
 */
public class RL_Sim {

    /** The main simulation desktop. */
    final Simulation sim;

    /** List of vehicles. */
    List<NeuronGroup> vehicles = new ArrayList<NeuronGroup>();

    // Number of trials to run
    int numTrials = 5;

    // Learning Rate
    double alpha = .25;

    // Eligibility trace. 0 for no trace; 1 for permanent trace. .9 default.
    // Must set in script.
    double lambda = 0;

    // Prob. of taking a random action.
    double epsilon = .1;

    /**
     * Discount factor . 0-1. 0 predict next value only. .5 predict future
     * values. As it increases toward one, values of y in the more distant
     * future become more significant.
     */
    double gamma = 1;

    // TODO
    JInternalFrame controlPanelFrame;
    boolean stop = false;
    boolean goalAchieved = false;
    Network network;
    RotatingEntity mouse;
    double hitRadius = 50;
    OdorWorldEntity cheese;
    int initialMouseLocation_x = 30;
    int initialMouseLocation_y = 30;
    Neuron reward;
    Neuron value;
    Neuron tdError;
    NeuronGroup inputs;
    NeuronGroup outputs;
    JTextField trialField = new JTextField();
    JTextField discountField = new JTextField();
    JTextField lambdaField = new JTextField();
    JTextField epsilonField = new JTextField();

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

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Create the network builder
        NetBuilder net = sim.addNetwork(223,1,474,614, "Neural Network");
        network = net.getNetwork();

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(690,1,460,567,
                "Simple World");
        world.getWorld().setObjectsBlockMovement(false);
        
        // Create a control panel
        controlPanelFrame = sim.addFrame(1,1, "RL Controls");
        initControlPanel();

        // Add the agent
        mouse = world.addAgent(initialMouseLocation_x, initialMouseLocation_y,
                "Mouse");

        // Set up the odor world with objects. Last component is reward
        cheese = world.addEntity(200, 250, "Swiss.gif",
                new double[] { 0, 1, 0, 0, 0, 1 });
        cheese.getSmellSource().setDispersion(250);
        //OdorWorldEntity flower = world.addEntity(0, 200, "Flax.gif",
        //        new double[] { 0, 0, 0, 0, 1, 0 });
        //flower.getSmellSource().setDispersion(250);
        OdorWorldEntity candle = world.addEntity(100, 100, "Candle.png",
                new double[] { 0, 0, 0, 1, 0, 0 });

        // Add main input-output network to be trained by RL
        outputs = net.addNeuronGroup(0, 0, 5);
        outputs.setLabel("Outputs");
        // TODO: Better way to lay this out...
        inputs = net.addNeuronGroup(0, 250, 5);
        inputs.setLabel("Inputs");
        inputs.setClamped(true);
        SynapseGroup inputOutputConnection = net.addSynapseGroup(inputs,
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

        // Add vehicle networks
        Vehicle vehicleBuilder = new Vehicle(sim, net, world);
        int centerX = (int) outputs.getCenterX();

        NeuronGroup pursueCheese = vehicleBuilder.addPursuer(centerX - 200,
                -250, mouse, 1);
        pursueCheese.setLabel("Pursue Cheese");
        setUpVehicle(pursueCheese);

        //NeuronGroup pursueFlower = vehicleBuilder.addPursuer(centerX + 200,
        //        -250, mouse, 4);
        //pursueFlower.setLabel("Pursue Flower");
        //setUpVehicle(pursueFlower);
        
        NeuronGroup avoidcandle = vehicleBuilder.addAvoider(centerX, -250,
                mouse, 3);
        avoidcandle.setLabel("Avoid Candle");
        setUpVehicle(avoidcandle);

        // Couple output nodes to vehicles
        net.connect(outputs.getNeuronList().get(0),
                pursueCheese.getNeuronByLabel("Speed"), 10);
        //net.connect(outputs.getNeuronList().get(1),
        //        pursueFlower.getNeuronByLabel("Speed"), 10);
        net.connect(outputs.getNeuronList().get(2),
                avoidcandle.getNeuronByLabel("Speed"), 10);

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
        vehicles.add(vehicle);
    }

    /**
     * Run one trial from an initial state until it reaches cheese.
     */
    protected void runTrial() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {

                numTrials = Integer.parseInt(trialField.getText());
                gamma = Double.parseDouble(discountField.getText());
                lambda = Double.parseDouble(lambdaField.getText());
                epsilon = Double.parseDouble(epsilonField.getText());
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

                    // Randomize position of the mouse
                    mouse.setLocation(initialMouseLocation_x,
                            initialMouseLocation_y);
                    mouse.setHeading(0);

                    // Move mouse up to object by iterating n times
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
    void initControlPanel() {

        LabelledItemPanel panel = new LabelledItemPanel();
        controlPanelFrame.add(panel);

        lambdaField.setText("" + lambda);
        trialField.setText("" + numTrials);
        panel.addItem("Trials", trialField);
        discountField.setText("" + gamma);
        panel.addItem("Discount rate", discountField);
        // panel.addItem("Lambda", lambdaField); 
        epsilonField.setText("" + epsilon);
        panel.addItem("Epsilon", epsilonField);

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
        
        controlPanelFrame.pack();
    }
}
