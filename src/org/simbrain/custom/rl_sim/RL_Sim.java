package org.simbrain.custom.rl_sim;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.simulation.NetBuilder;
import org.simbrain.simulation.OdorWorldBuilder;
import org.simbrain.simulation.Simulation;
import org.simbrain.simulation.Vehicle;
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
        NetBuilder net = sim.addNetwork(10, 10, 450, 450, "My first network");

        // Create the odor world
        OdorWorldBuilder world = sim.addOdorWorld(460, 10, 450, 450,
                "My first world");
        world.getWorld().setObjectsBlockMovement(false);

        // Add the agent!
        RotatingEntity mouse = world.addAgent(20, 20, "Mouse");

        // Populate the odor world with stimuli.  Last component is reward
        OdorWorldEntity cheese = world.addEntity(10, 100, "Swiss.gif",
                new double[] { 0, 1, 0, 0, 0, 1 });
        cheese.getSmellSource().setDispersion(250);
        OdorWorldEntity flower = world.addEntity(350, 300, "Flax.gif",
                new double[] { 0, 0, 0, 0, 1, 0 });
        flower.getSmellSource().setDispersion(250);
        // Yang add more.

        // Add main input-output network to be trained by RL
        NeuronGroup outputs = net.addNeuronGroup(0, 0, 5);
        outputs.setLabel("Outputs");
        // TODO: Better way to lay this out...
        NeuronGroup inputs = net.addNeuronGroup(0, 250, 5);
        inputs.setLabel("Inputs");
        inputs.setClamped(true);
        SynapseGroup inputOutputConnection = net.addSynapseGroup(inputs, outputs);
        sim.couple(mouse, inputs);

        // Reward, Value TD
        Neuron reward = net.addNeuron(500, 0);
        reward.setClamped(true);
        reward.setLabel("Reward");
        sim.couple(mouse.getSensor("Smell-Center"), 5, reward);
        Neuron value = net.addNeuron(550, 0);
        value.setLabel("Value");
        net.connectAllToAll(inputs, value);
        Neuron tdError = net.addNeuron(600, 0);
        tdError.setLabel("TD Error");

        // Add vehicle networks
        Vehicle vehicleBuilder = new Vehicle(sim, net, world);
        int centerX = (int) outputs.getCenterX();
        NeuronGroup pursueCheese = vehicleBuilder.addPursuer(centerX - 200,
                -250, mouse, 2);
        pursueCheese.setLabel("Pursue Cheese");
        setUpVehicle(pursueCheese);
        NeuronGroup pursueFlower = vehicleBuilder.addPursuer(centerX + 200,
                -250, mouse, 5);
        pursueFlower.setLabel("Pursue Flower");
        setUpVehicle(pursueFlower);
        // Yang add more pursuers and avoiders

        // Couple output nodes to vehicles
        net.connect(outputs.getNeuronList().get(0),
                pursueCheese.getNeuronByLabel("Speed"), 0);
        net.connect(outputs.getNeuronList().get(1),
                pursueFlower.getNeuronByLabel("Speed"), 0);

        // Add custom update rule
        RL_Update rl = new RL_Update(net.getNetwork(), inputs, outputs, value,
                reward, tdError);
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
        toUpdate.setUpdateRule("ProductRule");
        toUpdate.setActivation(0);
        toUpdate.setClamped(false);
    }

}
