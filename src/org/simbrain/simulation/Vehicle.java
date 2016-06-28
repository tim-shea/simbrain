package org.simbrain.simulation;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.world.odorworld.entities.RotatingEntity;

// TODO: Rename this?  VehicleBuilder?  VehicleHelper?
// TODO: Document everything.

/**
 * A wrapper for a NetworkComponent that makes it easy to add stuff to a
 * network.
 */
public class Vehicle {

    private final Simulation sim;

    private final NetBuilder net;

    private final OdorWorldBuilder world;

    private int DEFAULT_WEIGHT = 70;

    public enum VehicleType {
        PURSUER, AVOIDER
    }

    /**
     * @param net
     * @param world
     */
    public Vehicle(Simulation sim, NetBuilder net, OdorWorldBuilder world) {
        this.sim = sim;
        this.net = net;
        this.world = world;
    }

    private void setNodeDefaults(Neuron neuron, NeuronGroup ng) {
        neuron.setLowerBound(-10);
        neuron.setUpperBound(100);
        ng.addNeuron(neuron);
    }

    public NeuronGroup addVehicle(int x, int y, RotatingEntity agent,
            VehicleType type, int stimulusDimension) {

        // Create the network
        NeuronGroup vehicle = new NeuronGroup(net.getNetwork());
        Neuron leftTurn = net.addNeuron(x, y);
        leftTurn.setLabel("Left");
        setNodeDefaults(leftTurn, vehicle);
        Neuron straight = net.addNeuron(x + 50, y);
        straight.setLabel("Straight");
        straight.setActivation(3);
        setNodeDefaults(straight, vehicle);
        straight.setClamped(true);
        Neuron rightTurn = net.addNeuron(x + 100, y);
        rightTurn.setLabel("Right");
        setNodeDefaults(rightTurn, vehicle);
        Neuron leftInput = net.addNeuron(x, y + 100);
        leftInput.setClamped(true);
        setNodeDefaults(leftInput, vehicle);
        Neuron rightInput = net.addNeuron(x + 100, y + 100);
        rightInput.setClamped(true);
        setNodeDefaults(rightInput, vehicle);

        net.getNetwork().addGroup(vehicle);

        // Set weights here
        if (type == VehicleType.PURSUER) {
            net.connect(leftInput, leftTurn, DEFAULT_WEIGHT);
            net.connect(rightInput, rightTurn, DEFAULT_WEIGHT);
        } else if (type == VehicleType.AVOIDER) {
            net.connect(leftInput, rightTurn, DEFAULT_WEIGHT);
            net.connect(rightInput, leftTurn, DEFAULT_WEIGHT);
        }

        // Couple network to agent
        sim.couple(agent.getSensor("Smell-Left"), stimulusDimension - 1,
                leftInput);
        sim.couple(agent.getSensor("Smell-Right"), stimulusDimension - 1,
                rightInput);
        sim.couple(straight, agent.getEffector("Go-straight"));
        sim.couple(leftTurn, agent.getEffector("Go-left"));
        sim.couple(rightTurn, agent.getEffector("Go-right"));

        return vehicle;
    }

    public NeuronGroup addPursuer(int x, int y, RotatingEntity agent,
            int stimulusDimension) {
        return addVehicle(x, y, agent, VehicleType.PURSUER, stimulusDimension);
    }

    public NeuronGroup addAvoider(int x, int y, RotatingEntity agent,
            int stimulusDimension) {
        return addVehicle(x, y, agent, VehicleType.AVOIDER, stimulusDimension);
    }

}
