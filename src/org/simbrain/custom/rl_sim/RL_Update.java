package org.simbrain.custom.rl_sim;

import java.util.Collections;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;

/**
 * A custom updater for use in applying TD Learning and other custom update
 * features (e.g. only activating one vehicle network at a time based on the
 * output of a feed-forward net).
 *
 * For background on TD Learning see.
 * http://www.scholarpedia.org/article/Temporal_difference_learning
 */
public class RL_Update implements NetworkUpdateAction {

    /** Reference to RL_Sim object that has all the main variables used. */
    RL_Sim sim;

    /**
     * Reference to main neurons used in td learning.
     */
    Neuron reward, value, tdError;

    /**
     * 
     * This variable is a hack needed because the reward neuron's lastactivation
     * value is not being updated properly in this simulation now.
     * 
     * Todo: Remove after fixing the issue. The issue is probably based on
     * coupling update.
     */
    double lastReward;

    /**
     * Construct the updater.
     */
    public RL_Update(RL_Sim sim) {
        super();
        this.sim = sim;
        reward = sim.reward;
        value = sim.value;
        tdError = sim.tdError;

    }

    @Override
    public String getDescription() {
        return "Custom TD Rule";
    }

    @Override
    public String getLongDescription() {
        return "Custom TD Rule";
    }

    @Override
    public void invoke() {
        mainUpdateMethod();
    }

    /**
     * Custom update of the network, including application of TD Rules.
     */
    private void mainUpdateMethod() {

        // Inputs
        Network.updateNeurons(sim.inputs.getNeuronList());

        // Reward and Delta Reward
        lastReward = sim.reward.getLastActivation();
        Network.updateNeurons(Collections.singletonList(sim.reward));
        updateDeltaReward();

        // Outputs and vehicles
        sim.outputs.update();
        Neuron winner = sim.outputs.getWinningNeuron();
        Network.updateNeurons(Collections.singletonList(sim.value));
        // Update the vehicle whose name corresponds to the winning output
        // neuron
        for (NeuronGroup vehicle : sim.vehicles) {
            if (vehicle.getLabel().equalsIgnoreCase(winner.getLabel())) {
                vehicle.update();
                // System.out.println(vehicle.getLabel());
            } else {
                vehicle.clearActivations();
            }
        }

        // TD Error. Used to drive all learning in the network.
        tdError.setActivation((sim.deltaReward.getActivation()
                + sim.gamma * value.getActivation())
                - value.getLastActivation());

        // Update value synapses.
        // Learn the value function. The "critic".
        for (Synapse synapse : value.getFanIn()) {
            Neuron sourceNeuron = (Neuron) synapse.getSource();
            double newStrength = synapse.getStrength()
                    + sim.alpha * tdError.getActivation()
                            * sourceNeuron.getLastActivation();
            synapse.setStrength(newStrength);
        }

        // Update all "actor" neurons. (Roughly) If the last input > output
        // connection led to reward, reinforce that connection.
        for (Neuron neuron : sim.outputs.getNeuronList()) {
            // Just update the last winner
            if (neuron.getLastActivation() > 0) {
                for (Synapse synapse : neuron.getFanIn()) {
                    Neuron sourceNeuron = synapse.getSource();
                    double newStrength = synapse.getStrength()
                            + sim.alpha * tdError.getActivation()
                                    * sourceNeuron.getLastActivation();
                    // synapse.setStrength(synapse.clip(newStrength));
                    synapse.setStrength(newStrength);
                }
            }
        }
    }

    /**
     * Update the delta-reward neuron, by taking the difference between the
     * reward neuron's last state and its current state.
     * 
     * Currently artificially scales the delta for positive diff.
     */
    private void updateDeltaReward() {
        double diff = reward.getActivation() - lastReward;
        // Exaggerate positive differences, resulting in more learning when
        // reward goes up
        if (diff > 0) {
            diff *= 200; // TODO: Name for this factor?
        }
        sim.deltaReward.forceSetActivation(diff);
    }

}
