package org.simbrain.custom.test;

import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.simulation.Vehicle;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;

/**
 * Playground for testing new features. A lot of stuff is commented out but
 * should work.
 */
public class TestSim {

    /** The main simulation object. */
    final Simulation sim;

    /**
     * @param desktop
     */
    public TestSim(SimbrainDesktop desktop) {
        sim = new Simulation(desktop);
    }

    /**
     * Run the simulation!
     */
    public void run() {

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Build a network
        NetBuilder net = sim.addNetwork(10, 10, 450, 450, "My first network");
        // nb1.addNeurons(0, 0, 20, "horizontal line", "LinearRule");
        // nb1.addNeurons(0, 89, 20, "vertical line", "LinearRule");
        // nb1.addNeurons(89, 89, 49, "grid", "LinearRule");
        // NeuronGroup inputs = net1.addNeuronGroup(0, 300, 6, "horizontal
        // line",
        // "DecayRule");
        // inputs.setLabel("Inputs");
        // NeuronGroup outputs = net1.addNeuronGroup(0, 0, 3, "horizontal line",
        // "DecayRule");
        // outputs.setUpperBound(10);
        // outputs.setLabel("Outputs");
        // // nb1.connectAllToAll(inputs, outputs);
        // SynapseGroup in_out = net1.addSynapseGroup(inputs, outputs);
        // in_out.setExcitatoryRatio(.5);
        // in_out.randomizeConnectionWeights(); // TODO Not working?

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(460, 10, 450, 450,
                "My first world");
        world.getWorld().setObjectsBlockMovement(false);
        RotatingEntity mouse = world.addAgent(20, 20, "Mouse");
        RotatingEntity mouse2 = world.addAgent(200, 200, "Mouse");
        RotatingEntity mouse3 = world.addAgent(400, 200, "Mouse");

        OdorWorldEntity cheese = world.addEntity(150, 150, "Swiss.gif",
                new double[] { 0, 1, 0, 0 });
        cheese.getSmellSource().setDispersion(200);

        // Coupling agent to network
        // sim.couple(mouse, inputs); // Agent sensors to neurons
        // sim.couple(outputs, mouse); // Neurons to movement effectors
        
        // Add vehicles
        Vehicle vehicleBuilder = new Vehicle(sim, net, world);
        vehicleBuilder.setWeightSize(10);
        NeuronGroup pursuer1 = vehicleBuilder.addPursuer(0, 400, mouse, 1);
        pursuer1.setLabel("Pursuer 1");
        NeuronGroup pursuer2 = vehicleBuilder.addPursuer(240, 400, mouse2, 1);
        pursuer2.setLabel("Pursuer 2");
        NeuronGroup avoider1 = vehicleBuilder.addAvoider(480, 400, mouse3, 1);
        avoider1.setLabel("Avoider 1");

    }

}
