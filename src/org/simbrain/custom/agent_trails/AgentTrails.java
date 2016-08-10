package org.simbrain.custom.agent_trails;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JInternalFrame;

import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.entities.RotatingEntity;

/**
 * Todo Stop button
 *
 */
public class AgentTrails {

    /** The main simulation object. */
    final Simulation sim;


    NetBuilder net;
    RotatingEntity mouse;
    LabelledItemPanel panel;
    NeuronGroup sensoryNet, actionNet, predictionNet;
    Neuron leftNeuron, straightNeuron, rightNeuron;
    Neuron cheeseNeuron, flowerNeuron, fishNeuron;
    Neuron errorNeuron;
    Path csvFile;
    List<String> activationList = new ArrayList<String>();

    // TODO: Figure out how to relate this to xml
    int dispersion = 65;
    int fishX = 50;
    int fishY = 100;
    int flowerX = 200;
    int flowerY = 100;
    int cheeseX = 120;
    int cheeseY = 180;

    /**
     * @param desktop
     */
    public AgentTrails(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
    }

    /**
     * Run the simulation!
     */
    public void run() {

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Initialize file to save activations
        csvFile = Paths.get("agentTrails.csv");

        // Build a network
        net = sim.addNetwork(195, 9, 447, 296, "Simple Predicter");
        sensoryNet = net.addNeuronGroup(-9.25, 95.93, 3);
        sensoryNet.setClamped(true);
        sensoryNet.setLabel("Sensory");
        cheeseNeuron = sensoryNet.getNeuronList().get(0);
        cheeseNeuron.setLabel("Cheese");
        flowerNeuron = sensoryNet.getNeuronList().get(1);
        flowerNeuron.setLabel("Flower");
        fishNeuron = sensoryNet.getNeuronList().get(2);
        fishNeuron.setLabel("Fish");

        actionNet = net.addNeuronGroup(0, -0.79, 3);
        actionNet.setLabel("Actions");
        actionNet.setClamped(true);
        actionNet.setLabel("Sensory");
        leftNeuron = actionNet.getNeuronList().get(0);
        leftNeuron.setLabel("Left");
        straightNeuron = actionNet.getNeuronList().get(1);
        straightNeuron.setLabel("Straight");
        rightNeuron = actionNet.getNeuronList().get(2);
        rightNeuron.setLabel("Right");
        predictionNet = net.addNeuronGroup(231.02, 24.74, 3);
        predictionNet.setLabel("Predicted");

        net.connectAllToAll(sensoryNet, predictionNet);
        net.connectAllToAll(actionNet, predictionNet);

        errorNeuron  = net.addNeuron(268, 108);
        errorNeuron.setClamped(true);
        errorNeuron.setLabel("Error");

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(629, 9, 315, 383,
                "Three Objects");
        world.getWorld().setObjectsBlockMovement(false);
        mouse = world.addAgent(120, 245, "Mouse");
        mouse.setHeading(90);
        File xmlFile = new File(
                "src/org/simbrain/custom/agent_trails/worldDescription.xml");
        world.loadWorld(xmlFile);

        // Couple network to agent
        sim.couple(straightNeuron, mouse.getEffector("Go-straight"));
        sim.couple(rightNeuron, mouse.getEffector("Go-left"));
        sim.couple(leftNeuron, mouse.getEffector("Go-right"));

        // Couple agent to network
        sim.couple(mouse.getSensor("Smell-Center"), 0, cheeseNeuron);
        sim.couple(mouse.getSensor("Smell-Center"), 1, flowerNeuron);
        sim.couple(mouse.getSensor("Smell-Center"), 2, fishNeuron);

        setUpControlPanel();

        // Configure custom updating
        net.getNetwork().getUpdateManager().clear();
        net.getNetwork().addUpdateAction(new NetworkUpdateAction() {

            // TODO: I'm not happy with this. Change!
            @Override
            public void invoke() {
                actionNet.update();
                sensoryNet.update();
                predictionNet.update();
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public String getLongDescription() {
                return "";
            }
        });
        net.getNetwork().addUpdateAction(new TrainPredictionNet(this));
        net.getNetwork().addUpdateAction(new LogActivations(this));

    }

    private void setUpControlPanel() {
        // Set up internal frame
        JInternalFrame internalFrame = new JInternalFrame("Train / Test", true,
                true);
        panel = new LabelledItemPanel();

        // Move past cheese
        JButton button1 = new JButton("Cheese");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        net.getNetwork().clearActivations();
                        mouse.setLocation(cheeseX, cheeseY + dispersion);
                        mouse.setHeading(90);
                        straightNeuron.forceSetActivation(1);
                        iterate(180);
                    }
                });
            }
        });
        panel.addItem("", button1);

        // Move past Fish
        JButton button2 = new JButton("Fish");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        net.getNetwork().clearActivations();
                        mouse.setLocation(fishX, fishY + dispersion);
                        mouse.setHeading(90);
                        straightNeuron.forceSetActivation(1);
                        iterate(180);
                    }
                });
            }
        });
        panel.addItem("", button2);

        // Move past flower
        JButton button3 = new JButton("Flower");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        net.getNetwork().clearActivations();
                        mouse.setLocation(flowerX, flowerY + dispersion);
                        mouse.setHeading(90);
                        straightNeuron.forceSetActivation(1);
                        iterate(180);

                    }
                });
            }
        });
        panel.addItem("", button3);

        // Cheese > Fish
        JButton button4 = new JButton("Cheese > Flower");
        button4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        net.getNetwork().clearActivations();
                        mouse.setLocation(cheeseX, cheeseY + dispersion);
                        mouse.setHeading(90);
                        straightNeuron.forceSetActivation(1);
                        iterate(50);
                        rightNeuron.forceSetActivation(1.5);
                        iterate(25);
                        rightNeuron.forceSetActivation(0);
                        iterate(220);
                    }
                });
            }
        });
        panel.addItem("", button4);

        // Cheese > Flower
        JButton button5 = new JButton("Cheese > Fish");
        button5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    public void run() {
                        net.getNetwork().clearActivations();
                        mouse.setLocation(cheeseX, cheeseY + dispersion);
                        mouse.setHeading(90);
                        straightNeuron.forceSetActivation(1);
                        iterate(50);
                        leftNeuron.forceSetActivation(1.5);
                        iterate(25);
                        leftNeuron.forceSetActivation(0);
                        iterate(220);
                    }
                });
            }
        });
        panel.addItem("", button5);

        // Save File
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    Files.write(csvFile, activationList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        panel.addItem("", saveButton);

        // Set up Frame
        internalFrame.setLocation(5, 10);
        internalFrame.getContentPane().add(panel);
        internalFrame.setVisible(true);
        internalFrame.pack();
        sim.getDesktop().addInternalFrame(internalFrame);
    }

    /**
     * Iterate the simulation a specific number of times and don't move forward
     * in the script until done.
     *
     * @param iterations
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

}
