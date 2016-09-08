package org.simbrain.custom.hippocampus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.simbrain.docviewer.DocViewerComponent;
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

    NetBuilder net;
    Network network;

    ControlPanel panel;

    // Variables
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
        sim.addDocViewer(0,187,260,433,
                "Information",
                "src/org/simbrain/custom/hippocampus/Hippocampus.html");

    }

    /**
     * Build hippocampal model.
     */
    private void buildNetwork() {

        // Set up
        net = sim.addNetwork(250, 10, 700, 650, "Hippocampus");
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

        // Show pattern one
        panel.addButton("Pattern 1", () -> {
            setUpTrainButton(network, new double[] { 1, 0, 0, 0 });
        });

        // Show pattern two
        panel.addButton("Pattern 2", () -> {
            setUpTrainButton(network, new double[] { 0, 1, 0, 0 });
        });

        // Show pattern three
        panel.addButton("Pattern 3", () -> {
            setUpTrainButton(network, new double[] { 0, 0, 1, 0 });
        });

        // Show pattern four
        panel.addButton("Pattern 4", () -> {
            setUpTrainButton(network, new double[] { 0, 0, 0, 1 });
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

}
