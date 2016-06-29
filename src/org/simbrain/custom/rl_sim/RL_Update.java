package org.simbrain.custom.rl_sim;

import java.util.Collections;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;

public class RL_Update implements NetworkUpdateAction {

    // Simulation parameters
    int numTrials = 5;
    double alpha = .25; // Learning rate
    double gamma = 1; // Discount factor . 0-1. 0 predict next value only. .5
                      // predict future values.
                      // As it increases toward one, values of y in the more
                      // distant future become more significant.}
    double lambda = 0; // 0 for no trace; 1 for permanent trace. .9 default.
                       // Must set in script.
    double epsilon = .1; // Prob. of taking a random action

    // TODO: Replace with a reference to the whole netbuilder objects
    Network network;
    NeuronGroup inputs;
    NeuronGroup outputs;
    Neuron value;
    Neuron reward;
    Neuron tdError;

    public RL_Update(Network network, NeuronGroup inputs, NeuronGroup outputs,
            Neuron value, Neuron reward, Neuron tdError) {
        super();
        this.network = network;
        this.inputs = inputs;
        this.outputs = outputs;
        this.value = value;
        this.reward = reward;
        this.tdError = tdError;
    }

    public String getDescription() {
        return "Custom TD Rule";
    }

    public String getLongDescription() {
        return "Custom TD Rule";
    }

    public void invoke() {

        // Update all neurons (Just state neurons?)
        network.updateNeurons(Collections.singletonList(value));
        network.updateNeurons(Collections.singletonList(reward));

        // Set main variables
        tdError.setActivation(
                (reward.getActivation() + gamma * value.getActivation())
                        - value.getLastActivation());
        System.out.println("td error:" + value.getActivation() + " + "
                + reward.getActivation() + " - " + value.getLastActivation());

        // Update all value synapses
        for (Synapse synapse : value.getFanIn()) {
            Neuron sourceNeuron = (Neuron) synapse.getSource();
            // Reinforce based on the source neuron's last activation (not its
            // current value),
            // since that is what the current td error reflects.
            double newStrength = synapse.getStrength()
                    + alpha * tdError.getActivation()
                            * sourceNeuron.getLastActivation();
            // synapse.setStrength(synapse.clip(newStrength));
            synapse.setStrength(newStrength);
        }

        // Update all actor neurons. Go through "outputs"
        for (Neuron neuron : outputs.getNeuronList()) {
            // Just update the last winner
            if (neuron.getLastActivation() > 0) {
                for (Synapse synapse : neuron.getFanIn()) {
                    Neuron sourceNeuron = synapse.getSource();
                    // Reinforce actions based on the source neuron's last
                    // activation (not its current value),
                    // since that is what the current td error reflects.
                    double newStrength = synapse.getStrength()
                            + alpha * tdError.getActivation()
                                    * sourceNeuron.getLastActivation();
                    synapse.setStrength(synapse.clip(newStrength));
                    // synapse.setStrength(newStrength);
                    // System.out.println("Neuron (" + neuron.getLabel() + ") /
                    // Tile //neuron (" + sourceNeuron.getId() + "):" +
                    // newStrength);
                }

            }
        }

    }

}
