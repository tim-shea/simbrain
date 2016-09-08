package org.simbrain.custom.hippocampus;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.simbrain.network.connections.AllToAll;
import org.simbrain.network.core.Network;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.network.subnetworks.CompetitiveGroup;
import org.simbrain.simulation.ControlPanel;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.Simulation;
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
public class Hippocampus {

    /** The main simulation object. */
    final Simulation sim;

    /** Other variables. */
    NetBuilder net;
    Network network;
    ControlPanel panel;
    boolean hippoLesioned = false;
    boolean learningEnabled = true;

    /**
     * @param desktop
     */
    public Hippocampus(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
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
        sim.addDocViewer(832, 9, 284, 544, "Information",
                "src/org/simbrain/custom/hippocampus/Hippocampus.html");

    }

    /**
     * Build hippocampal model.
     */
    private void buildNetwork() {

        // Set up
        net = sim.addNetwork(254, 8, 588, 572, "Hippocampus");
        network = net.getNetwork();

        // TODO: Consider a rename to upper / lower cortex 1 + 2 to better match
        // paper

        // LC1
        CompetitiveGroup LC1 = new CompetitiveGroup(network, 4);
        LC1.setLabel("Left Cortex 1");
        LC1.applyLayout();
        LC1.setLocation(0, 0);
        LC1.setUpdateMethod("AS");
        network.addGroup(LC1);

        // LC2
        CompetitiveGroup LC2 = new CompetitiveGroup(network, 4);
        LC2.setLabel("Left Cortex 2");
        LC2.applyLayout();
        LC2.setLocation(0, 800);
        LC2.setUpdateMethod("AS");
        network.addGroup(LC2);

        // RC1
        CompetitiveGroup RC1 = new CompetitiveGroup(network, 4);
        RC1.setLabel("Right Cortex 1");
        RC1.applyLayout();
        RC1.setLocation(1600, 0);
        RC1.setUpdateMethod("AS");
        network.addGroup(RC1);

        // RC2
        CompetitiveGroup RC2 = new CompetitiveGroup(network, 4);
        RC2.setLabel("Right Cortex 2");
        RC2.applyLayout();
        RC2.offset(1600, 800);
        RC2.setUpdateMethod("AS");
        network.addGroup(RC2);

        // Hippocampus
        CompetitiveGroup hippocampus = new CompetitiveGroup(network, 4);
        hippocampus.setLabel("Hippocampus");
        hippocampus.applyLayout();
        hippocampus.setUpdateMethod("AS");
        hippocampus.offset(800, 1200);
        network.addGroup(hippocampus);

        // Make hippocampal synapse groups
        SynapseGroup HtoLC1 = SynapseGroup.createSynapseGroup(hippocampus, LC1,
                new AllToAll());
        HtoLC1.setLabel("H to LC1");
        network.addGroup(HtoLC1);
        // HtoLC1.setLearningRate(0.1);

        SynapseGroup HtoLC2 = SynapseGroup.createSynapseGroup(hippocampus, LC2,
                new AllToAll());
        HtoLC2.setLabel("H to LC2");
        network.addGroup(HtoLC2);
        // HtoLC2.setLearningRate(0.1);

        SynapseGroup HtoRC1 = SynapseGroup.createSynapseGroup(hippocampus, RC1,
                new AllToAll());
        HtoRC1.setLabel("H to RC1");
        network.addGroup(HtoRC1);
        // HtoRC1.setLearningRate(0.1);

        SynapseGroup HtoRC2 = SynapseGroup.createSynapseGroup(hippocampus, RC2,
                new AllToAll());
        HtoRC2.setLabel("H to RC2");
        network.addGroup(HtoRC2);
        // HtoRC2.setLearningRate(0.1);

        SynapseGroup LC1toH = SynapseGroup.createSynapseGroup(LC1, hippocampus,
                new AllToAll());
        LC1toH.setLabel("LC1 to H");
        network.addGroup(LC1toH);
        // LC1toH.setLearningRate(0.1);

        SynapseGroup LC2toH = SynapseGroup.createSynapseGroup(LC2, hippocampus,
                new AllToAll());
        LC2toH.setLabel("LC2 to H");
        network.addGroup(LC2toH);
        // LC2toH.setLearningRate(0.1);

        SynapseGroup RC1toH = SynapseGroup.createSynapseGroup(RC1, hippocampus,
                new AllToAll());
        RC1toH.setLabel("RC1 to H");
        network.addGroup(RC1toH);
        // RC1toH.setLearningRate(0.1);

        SynapseGroup RC2toH = SynapseGroup.createSynapseGroup(RC2, hippocampus,
                new AllToAll());
        RC2toH.setLabel("RC2 to H");
        network.addGroup(RC2toH);
        // RC2toH.setLearningRate(0.1);

        // Make cortico-cortical synapse groups
        SynapseGroup LC1toRC1 = SynapseGroup.createSynapseGroup(LC1, RC1,
                new AllToAll());
        LC1toRC1.setLabel("LC1 to RC1");
        network.addGroup(LC1toRC1);
        // LC1toRC1.setLearningRate(0.002);

        SynapseGroup LC1toRC2 = SynapseGroup.createSynapseGroup(LC1, RC2,
                new AllToAll());
        LC1toRC2.setLabel("LC1 to RC2");
        network.addGroup(LC1toRC2);
        // LC1toRC2.setLearningRate(0.002);

        SynapseGroup LC2toRC1 = SynapseGroup.createSynapseGroup(LC2, RC1,
                new AllToAll());
        LC2toRC1.setLabel("LC2 to RC1");
        network.addGroup(LC2toRC1);
        // LC2toRC1.setLearningRate(0.002);

        SynapseGroup LC2toRC2 = SynapseGroup.createSynapseGroup(LC2, RC2,
                new AllToAll());
        LC2toRC2.setLabel("LC2 to RC2");
        network.addGroup(LC2toRC2);
        // LC2toRC2.setLearningRate(0.002);

        SynapseGroup RC1toLC1 = SynapseGroup.createSynapseGroup(RC1, LC1,
                new AllToAll());
        RC1toLC1.setLabel("RC1 to LC1");
        network.addGroup(RC1toLC1);
        // RC1toLC1.setLearningRate(0.002);

        SynapseGroup RC1toLC2 = SynapseGroup.createSynapseGroup(RC1, LC2,
                new AllToAll());
        RC1toLC2.setLabel("RC1 to LC2");
        network.addGroup(RC1toLC2);
        /// RC1toLC2.setLearningRate(0.002);

        SynapseGroup RC2toLC1 = SynapseGroup.createSynapseGroup(RC2, LC1,
                new AllToAll());
        RC2toLC1.setLabel("RC2 to LC1");
        network.addGroup(RC2toLC1);
        // RC2toLC1.setLearningRate(0.002);

        SynapseGroup RC2toLC2 = SynapseGroup.createSynapseGroup(RC2, LC2,
                new AllToAll());
        RC2toLC2.setLabel("RC2 to LC2");
        network.addGroup(RC2toLC2);
        // RC2toLC2.setLearningRate(0.002);

    }

    private void setUpControlPanel() {

        panel = ControlPanel.makePanel(sim, "Control Panel", 5, 10);
        Random generator = new Random();

        // Show pattern one
        panel.addButton("Train 1", () -> {
            setUpTrainButton(network, new double[] { 1, 0, 0, 0 });
        });

        // Show pattern two
        panel.addButton("Train 2", () -> {
            setUpTrainButton(network, new double[] { 0, 1, 0, 0 });
        });

        // Show pattern three
        panel.addButton("Train 3", () -> {
            setUpTrainButton(network, new double[] { 0, 0, 1, 0 });
        });

        // Show pattern four
        panel.addButton("Train 4", () -> {
            setUpTrainButton(network, new double[] { 0, 0, 0, 1 });
        });

        // Consolidate
        panel.addButton("Consolidate", () -> {
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
            NeuronGroup hippo = (NeuronGroup) network
                    .getGroupByLabel("Hippocampus");
            network.clearActivations();
            hippo.setClamped(true);
            hippo.forceSetActivations(activations);
            iterate(1);
            hippo.setClamped(false);
        });

        // Hippocampus checkbox
        panel.addCheckBox("Hippocampus", hippoLesioned, () -> {
            if (hippoLesioned == true) {
                hippoLesioned = false;
            } else {
                hippoLesioned = true;
            }
            enableHippocampus(network, hippoLesioned);
        });

        // Freeze weights checkbox
        panel.addCheckBox("Learning", learningEnabled, () -> {
            if (learningEnabled == true) {
                learningEnabled = false;
            } else {
                learningEnabled = true;
            }
            network.freezeSynapses(!learningEnabled);
        });

        // Show pattern one
        panel.addButton("Test 1", () -> {
            setUpTestButton(network, new double[] { 1, 0, 0, 0 });
        });

        // Show pattern two
        panel.addButton("Test 2", () -> {
            setUpTestButton(network, new double[] { 0, 1, 0, 0 });
        });

        // Show pattern three
        panel.addButton("Test 3", () -> {
            setUpTestButton(network, new double[] { 0, 0, 1, 0 });
        });

        // Show pattern four
        panel.addButton("Test 4", () -> {
            setUpTestButton(network, new double[] { 0, 0, 0, 1 });
        });
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

    /**
     * Set up a train button that will clamp the the cortical neurons, then
     * train the network for three iterations, then unclamp the cortical
     * neurons.
     *
     * @param network
     * @param activations
     */
    void setUpTrainButton(Network network, double[] activations) {
        NeuronGroup LC1 = (NeuronGroup) network
                .getGroupByLabel("Left Cortex 1");
        NeuronGroup LC2 = (NeuronGroup) network
                .getGroupByLabel("Left Cortex 2");
        NeuronGroup RC1 = (NeuronGroup) network
                .getGroupByLabel("Right Cortex 1");
        NeuronGroup RC2 = (NeuronGroup) network
                .getGroupByLabel("Right Cortex 2");
        NeuronGroup HP = (NeuronGroup) network.getGroupByLabel("Hippocampus");

        // Clamp nodes and set activations
        LC1.setClamped(true);
        LC2.setClamped(true);
        RC1.setClamped(true);
        RC2.setClamped(true);
        LC1.forceSetActivations(activations);
        LC2.forceSetActivations(activations);
        RC1.forceSetActivations(activations);
        RC2.forceSetActivations(activations);
        HP.forceSetActivations(new double[] { 0, 0, 0, 0 });

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
    void setUpTestButton(Network network, double[] activations) {
        NeuronGroup LC1 = (NeuronGroup) network
                .getGroupByLabel("Left Cortex 1");
        NeuronGroup LC2 = (NeuronGroup) network
                .getGroupByLabel("Left Cortex 2");
        NeuronGroup HP = (NeuronGroup) network.getGroupByLabel("Hippocampus");

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
    }

    /**
     * Enable / disable the hippocampus
     */
    void enableHippocampus(Network network, boolean lesioned) {
        SynapseGroup HtoLC1 = (SynapseGroup) network
                .getGroupByLabel("H to LC1");
        SynapseGroup HtoLC2 = (SynapseGroup) network
                .getGroupByLabel("H to LC2");
        SynapseGroup HtoRC1 = (SynapseGroup) network
                .getGroupByLabel("H to RC1");
        SynapseGroup HtoRC2 = (SynapseGroup) network
                .getGroupByLabel("H to RC2");
        SynapseGroup LC1toH = (SynapseGroup) network
                .getGroupByLabel("LC1 to H");
        SynapseGroup LC2toH = (SynapseGroup) network
                .getGroupByLabel("LC2 to H");
        SynapseGroup RC1toH = (SynapseGroup) network
                .getGroupByLabel("RC1 to H");
        SynapseGroup RC2toH = (SynapseGroup) network
                .getGroupByLabel("RC2 to H");

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
