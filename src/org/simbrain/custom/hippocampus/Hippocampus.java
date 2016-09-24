package org.simbrain.custom.hippocampus;

import java.awt.FlowLayout;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.network.subnetworks.CompetitiveGroup;
import org.simbrain.simulation.ControlPanel;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.util.SimbrainConstants.Polarity;
import org.simbrain.util.randomizer.PolarizedRandomizer;
import org.simbrain.workspace.gui.SimbrainDesktop;

/**
 * Simulation of the Squire Alvarez Hippocampus model (PNAS, 1994). TODO: This
 * model illustrates serious issues with synapse group arrow rendering
 *
 * @author Jeff Yoshimi
 * @author Jeff Rodny
 * @author Alex Pabst
 *
 */
// CHECKSTYLE:OFF
public class Hippocampus {

    /** The main simulation object. */
    final Simulation sim;

    /** Randomizer for creating new synapse groups. */
    PolarizedRandomizer exRand = new PolarizedRandomizer(Polarity.EXCITATORY);

    /** Other variables. */
    NetBuilder net;
    Network network;
    ControlPanel panel;
    boolean hippoLesioned = false;
    boolean learningEnabled = true;
    JLabel errorLabel;
    JTextField sleepField;

    /** References to main neuron and synapse groups. */
    CompetitiveGroup LC1, LC2, RC1, RC2, hippocampus;
    SynapseGroup HtoLC1, HtoLC2, HtoRC1, HtoRC2, LC1toH, LC2toH, RC1toH, RC2toH;

    /**
     * @param desktop
     */
    public Hippocampus(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
        exRand.setParam2(0.02); // Set up randomizer
    }

    /**
     * Run the simulation!
     */
    public void run() {

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Build the network
        buildNetwork();

        // Set up control panel
        setUpControlPanel();

        // Add docviewer
        sim.addDocViewer(807, 12, 307, 591, "Information",
                "src/org/simbrain/custom/hippocampus/Hippocampus.html");

        // If manually updating in script, must clear built-in updates
        // network.getUpdateManager().clear();

    }

    /**
     * Build hippocampal model.
     */
    private void buildNetwork() {

        // Set network variables
        net = sim.addNetwork(187, 12, 632, 585, "Hippocampus");
        network = net.getNetwork();

        // Add cortical groups
        LC1 = addCorticalGroup("Left Cortex Top", 185, -114);
        LC2 = addCorticalGroup("Left Cortex Bottom", 185, -28);
        RC1 = addCorticalGroup("Right Cortex Top", 597, -114);
        RC2 = addCorticalGroup("Right Cortex Top", 597, -28);

        // Add hippocampus (MTL)
        hippocampus = addHippocampus("Hippocampus", 368, 179);

        // Cortex to MTL connections
        HtoLC1 = addSynapseGroup(hippocampus, LC1, "H to LC1");
        HtoLC2 = addSynapseGroup(hippocampus, LC2, "H to LC2");
        HtoRC1 = addSynapseGroup(hippocampus, RC1, "H to RC1");
        HtoRC2 = addSynapseGroup(hippocampus, RC2, "H to RC2");
        LC1toH = addSynapseGroup(LC1, hippocampus, "LC1 to H");
        LC2toH = addSynapseGroup(LC2, hippocampus, "LC2 to H");
        RC1toH = addSynapseGroup(RC1, hippocampus, "RC1 to H");
        RC2toH = addSynapseGroup(RC2, hippocampus, "RC2 to H");
        // Cortico-corticol connections
        addSynapseGroup(LC1, RC1, "LC1 to RC1");
        addSynapseGroup(LC1, RC2, "LC1 to RC2");
        addSynapseGroup(LC2, RC1, "LC2 to RC1");
        addSynapseGroup(LC2, RC2, "LC2 to RC2");
        addSynapseGroup(RC1, LC1, "RC1 to LC1");
        addSynapseGroup(RC1, LC2, "RC1 to LC2");
        addSynapseGroup(RC2, LC1, "RC2 to LC1");
        addSynapseGroup(RC2, LC2, "RC2 to LC2");

    }

    /**
     * Add the MTL.
     */
    private CompetitiveGroup addHippocampus(String label, double x, double y) {
        CompetitiveGroup cg = addCompetitiveGroup(label, x, y);
        // See Alvarez-Squire Fig. 2
        cg.setSynpaseDecayPercent(.04);
        cg.setLearningRate(.1);
        return cg;
    }

    /**
     * Add a cortical group.
     */
    private CompetitiveGroup addCorticalGroup(String label, double x,
            double y) {
        CompetitiveGroup cg = addCompetitiveGroup(label, x, y);
        // See Alvarez-Squire Fig. 2
        cg.setSynpaseDecayPercent(.0008);
        cg.setLearningRate(.002);
        return cg;
    }

    /**
     * Add and properly initialize a competitive neuron group.
     */
    private CompetitiveGroup addCompetitiveGroup(String label, double x,
            double y) {
        CompetitiveGroup cg = new CompetitiveGroup(network, 4);
        cg.setLabel(label);
        cg.applyLayout();
        cg.setLocation(x, y);
        cg.setUpdateMethod("AS");

        network.addGroup(cg);
        return cg;
    }

    /**
     * Add and properly initialize a synapse group.
     */
    private SynapseGroup addSynapseGroup(NeuronGroup source, NeuronGroup target,
            String name) {

        // Initialize with uniform distribution from 0 to .1
        SynapseGroup synGroup = SynapseGroup.createSynapseGroup(source, target,
                SynapseGroup.DEFAULT_CONNECTION_MANAGER, 1, exRand,
                SynapseGroup.DEFAULT_IN_RANDOMIZER);
        synGroup.setLabel(name);
        synGroup.setLowerBound(0, Polarity.EXCITATORY);
        synGroup.setUpperBound(1, Polarity.EXCITATORY);
        network.addGroup(synGroup);
        return synGroup;
    }

    /**
     * Set up the controls.
     */
    private void setUpControlPanel() {

        panel = ControlPanel.makePanel(sim, "Control Panel", 5, 10);
        Random generator = new Random();

        // Show pattern one
        panel.addButton("Train All", () -> {
            train(network, new double[] { 1, 0, 0, 0 });
            train(network, new double[] { 0, 1, 0, 0 });
            train(network, new double[] { 0, 0, 1, 0 });
            train(network, new double[] { 0, 0, 0, 1 });
            train(network, new double[] { 1, 0, 0, 0 });
            train(network, new double[] { 0, 1, 0, 0 });
            train(network, new double[] { 0, 0, 1, 0 });
            train(network, new double[] { 0, 0, 0, 1 });
        });

        // Test all the patterns
        panel.addButton("Test All", () -> {
            double error = 0;
            test(network, new double[] { 1, 0, 0, 0 });
            error += getError(0);
            test(network, new double[] { 0, 1, 0, 0 });
            error += getError(1);
            test(network, new double[] { 0, 0, 1, 0 });
            error += getError(2);
            test(network, new double[] { 0, 0, 0, 1 });
            error += getError(3);
            errorLabel.setText("" + error);
        });

        // panel.addButton("Train 1", () -> {
        // train(network, new double[] { 1, 0, 0, 0 });
        // });
        //
        // // Show pattern two
        // panel.addButton("Train 2", () -> {
        // train(network, new double[] { 0, 1, 0, 0 });
        // });
        //
        // // Show pattern three
        // panel.addButton("Train 3", () -> {
        // train(network, new double[] { 0, 0, 1, 0 });
        // });
        //
        // // Show pattern four
        // panel.addButton("Train 4", () -> {
        // train(network, new double[] { 0, 0, 0, 1 });
        // });

        // Reset weights
        panel.addButton("Reset weights", () -> {
            // Quick and easy weight reset.
            for (Synapse synapse : network.getFlatSynapseList()) {
                synapse.setStrength(.2 * Math.random());
            }
            network.fireSynapsesUpdated();
        });

        panel.addSeparator();

        // Show pattern one
        panel.addButton("Test 1", () -> {
            test(network, new double[] { 1, 0, 0, 0 });
        });

        // Show pattern two
        panel.addButton("Test 2", () -> {
            test(network, new double[] { 0, 1, 0, 0 });
        });

        // Show pattern three
        panel.addButton("Test 3", () -> {
            test(network, new double[] { 0, 0, 1, 0 });
        });

        // Show pattern four
        panel.addButton("Test 4", () -> {
            test(network, new double[] { 0, 0, 0, 1 });
        });

        panel.addSeparator();

        ControlPanel bottomPanel = new ControlPanel();
        errorLabel = bottomPanel.addLabel("Error:", "");

        // Lesion checkbox
        bottomPanel.addCheckBox("Lesion MTL", hippoLesioned, () -> {
            if (hippoLesioned == true) {
                hippoLesioned = false;
            } else {
                hippoLesioned = true;
            }
            enableHippocampus(hippoLesioned);
        });

        // Freeze weights checkbox
        // bottomPanel.addCheckBox("Learning", learningEnabled, () -> {
        // if (learningEnabled == true) {
        // learningEnabled = false;
        // } else {
        // learningEnabled = true;
        // }
        // network.freezeSynapses(!learningEnabled);
        // });

        // Consolidate
        JPanel sleepPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton sleepButton = new JButton("Sleep");
        sleepPanel.add(sleepButton);
        sleepField = new JTextField();
        sleepField.setColumns(3);
        sleepField.setText("1");
        sleepPanel.add(sleepField);
        sleepButton.addActionListener(e -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                double[] activations = new double[4];
                int actNeuron = generator.nextInt(4);
                if (actNeuron == 0) {
                    activations = new double[] { 1, 0, 0, 0 };
                } else if (actNeuron == 1) {
                    activations = new double[] { 0, 1, 0, 0 };
                } else if (actNeuron == 2) {
                    activations = new double[] { 0, 0, 1, 0 };
                } else if (actNeuron == 3) {
                    activations = new double[] { 0, 0, 0, 1 };
                }
                network.clearActivations();
                hippocampus.setClamped(true);
                hippocampus.forceSetActivations(activations);
                iterate(Integer.parseInt(sleepField.getText()));
                hippocampus.setClamped(false);
            });
        });
        panel.addComponent(sleepPanel);

        panel.addBottomComponent(bottomPanel);

    }

    /**
     * Compute the error on the indicated test pattern (half patterns 1-4).
     *
     * @param index which pattern to test on
     * @return the error for that pattern.
     */
    public double getError(int index) {
        double diff1 = LC1.getNeuronList().get(index).getActivation()
                - RC1.getNeuronList().get(index).getActivation();
        double diff2 = LC2.getNeuronList().get(index).getActivation()
                - RC2.getNeuronList().get(index).getActivation();
        double error = Math.abs(diff1) + Math.abs(diff2);
        return error;
    }

    /**
     * Iterate the simulation a specific number of times and don't move forward
     * in the script until done.
     *
     * @param iterations
     */
    void iterate(int iterations) {
        CountDownLatch iterationLatch = new CountDownLatch(1);

        // TODO: Temporary for testing
        // LC1.update1();
        // LC2.update1();
        // RC1.update1();
        // RC2.update1();
        // hippocampus.update1();
        // LC1.update2();
        // LC2.update2();
        // RC1.update2();
        // RC2.update2();
        // hippocampus.update2();
        // LC1.update3();
        // LC2.update3();
        // RC1.update3();
        // RC2.update3();
        // hippocampus.update3();

        sim.getWorkspace().iterate(iterationLatch, iterations);
        try {
            iterationLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up a train button that will clamp the the cortical neurons, then
     * train the network for three iterations, then unclamp the cortical
     * neurons.
     *
     * @param network
     * @param activations
     */
    void train(Network network, double[] activations) {
        // Clamp nodes and set activations
        LC1.setClamped(true);
        LC2.setClamped(true);
        RC1.setClamped(true);
        RC2.setClamped(true);
        LC1.forceSetActivations(activations);
        LC2.forceSetActivations(activations);
        RC1.forceSetActivations(activations);
        RC2.forceSetActivations(activations);
        hippocampus.forceSetActivations(new double[] { 0, 0, 0, 0 });

        // Iterate 3 times
        iterate(3);

        // Unclamp nodes
        LC1.setClamped(false);
        LC2.setClamped(false);
        RC1.setClamped(false);
        RC2.setClamped(false);
    }

    /**
     * Set up a test button that will clamp half of the cortical neurons, then
     * runs the network for three iterations, then unclamp the cortical neurons.
     *
     * @param network
     * @param activations
     */
    void test(Network network, double[] activations) {

        // Turn off learning during testing
        network.freezeSynapses(true);
        network.clearActivations();

        // Clamp nodes and set activations
        LC1.setClamped(true);
        LC2.setClamped(true);
        LC1.forceSetActivations(activations);
        LC2.forceSetActivations(activations);

        // Iterate 3 times
        iterate(3);

        // Unclamp nodes
        LC1.setClamped(false);
        LC2.setClamped(false);

        // Turn on learning after testing
        network.freezeSynapses(false);

    }

    /**
     * Enable / disable the hippocampus
     */
    private void enableHippocampus(boolean lesioned) {

        HtoLC1.setEnabled(!lesioned);
        HtoLC2.setEnabled(!lesioned);
        HtoRC1.setEnabled(!lesioned);
        HtoRC2.setEnabled(!lesioned);
        LC1toH.setEnabled(!lesioned);
        LC2toH.setEnabled(!lesioned);
        RC1toH.setEnabled(!lesioned);
        RC2toH.setEnabled(!lesioned);
    }

}
