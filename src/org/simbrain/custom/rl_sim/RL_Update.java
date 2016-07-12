package org.simbrain.custom.rl_sim;

import java.util.Collections;
import java.util.Random;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;

/**
 * For background see.
 * http://www.scholarpedia.org/article/Temporal_difference_learning
 */
public class RL_Update implements NetworkUpdateAction {

    RL_Sim sim;

    Random generator = new Random();

    Neuron reward, value, tdError;

    public RL_Update(RL_Sim sim) {
        super();
        this.sim = sim;
        reward = sim.reward;
        value = sim.value;
        tdError = sim.tdError;
    }

    public String getDescription() {
        return "Custom TD Rule";
    }

    public String getLongDescription() {
        return "Custom TD Rule";
    }

    /**
     * The custom update function.
     */
    public void invoke() {

        // Update the neurons and neuron groups in an appropriate order
        Network.updateNeurons(sim.inputs.getNeuronList());
        Network.updateNeurons(Collections.singletonList(sim.reward));
        sim.outputs.update();
        Neuron winner = sim.outputs.getWinningNeuron();
        Network.updateNeurons(Collections.singletonList(sim.value));

        // Only update the vehicle corresponding to the winning output node
        for (NeuronGroup vehicle : sim.vehicles) {
            if (vehicle.getLabel().equalsIgnoreCase(winner.getLabel())) {
                vehicle.update();                
            } else {
                vehicle.clearActivations();
            }
        }

        // Set TD Error
        tdError.setActivation(
                (reward.getActivation() + sim.gamma * value.getActivation())
                        - value.getLastActivation());

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
                    //synapse.setStrength(synapse.clip(newStrength));
                    synapse.setStrength(newStrength);
                }
            }
        }

    }

}
