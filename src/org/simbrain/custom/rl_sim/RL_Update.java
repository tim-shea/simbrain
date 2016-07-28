package org.simbrain.custom.rl_sim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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


    /* Iterations to leave vehicle on between weight updates. */
    private final int iterationsBetweenWeightUpdates = 15;
    // Variables to help with the above
    private double previousReward;
    double[] previousInput;
    int counter = 0;
    // Helper which associates neurons with integer indices of the array that tracks past states
    Map<Neuron, Integer> neuronIndices = new HashMap();
    
    /**
     * Construct the updater.
     */
    public RL_Update(RL_Sim sim) {
        super();
        this.sim = sim;
        reward = sim.reward;
        value = sim.value;
        tdError = sim.tdError;
        initMap();
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
        updateVehicleNet(winner);

        // Update the weights
        if (counter++ % iterationsBetweenWeightUpdates == 0) {

            updateTDError();

            updateCritic();

            updateActor();

            // Record the "before" state of the system.
            previousReward = sim.reward.getActivation();
            System.arraycopy(sim.inputs.getActivations(), 0,
                    previousInput, 0,
                    sim.inputs.getActivations().length);
        }

    }

    /**
     * TD Error. Used to drive all learning in the network.
     */
    void updateTDError() {
        tdError.setActivation(sim.deltaReward.getActivation()
                + sim.gamma * value.getActivation()
                - value.getLastActivation());
    }

    /**
     * Update the vehicle whose name corresponds to the winning output.
     * 
     * @param winner
     */
    void updateVehicleNet(Neuron winner) {
        for (NeuronGroup vehicle : sim.vehicles) {
            if (vehicle.getLabel().equalsIgnoreCase(winner.getLabel())) {
                vehicle.update();
                // System.out.println(vehicle.getLabel());
            } else {
                vehicle.clearActivations();
            }
        }
    }

    /**
     * Update value synapses. Learn the value function. The "critic".
     */
    void updateCritic() {
        for (Synapse synapse : value.getFanIn()) {
            Neuron sourceNeuron = (Neuron) synapse.getSource();
            double newStrength = synapse.getStrength()
                    + sim.alpha * tdError.getActivation()
                            * sourceNeuron.getLastActivation();
            synapse.setStrength(newStrength);
        }
    }

    /**
     * Update all "actor" neurons. (Roughly) If the last input > output
     * connection led to reward, reinforce that connection.
     */
    void updateActor() {
        for (Neuron neuron : sim.outputs.getNeuronList()) {
            // Just update the last winner
            if (neuron.getLastActivation() > 0) {
                for (Synapse synapse : neuron.getFanIn()) {
                    // TODO: Below getXMomentsAgo(Neuron neuron)
                    double previousActivation = getPreviousNeuronValue(
                            synapse.getSource());
                    double newStrength = synapse.getStrength() + sim.alpha
                            * tdError.getActivation() * previousActivation;
                    // synapse.setStrength(synapse.clip(newStrength));
                    synapse.setStrength(newStrength);
                }
            }
        }
    }


    /**
     * Returns the "before" state of the given neuron.
     */
    private double getPreviousNeuronValue(Neuron neuron) {
        //System.out.println(previousInput[neuronIndices.get(neuron)]);
        return previousInput[neuronIndices.get(neuron)];
    }
    
    /**
     * Initialize the map from neurons to indices.
     */
    void initMap() {
        int index = 0;
        for (Neuron neuron : sim.inputs.getNeuronList()) {
            neuronIndices.put(neuron, index++);
        }
        previousInput = new double[index];
    }

    /**
     * Update the delta-reward neuron, by taking the difference between the
     * reward neuron's last state and its current state.
     * 
     * TODO: Rename needed around here? This is now the "reward" used by the TD
     * algorithm, which is different from the reward signal coming directory
     * from the environment.
     */
    private void updateDeltaReward() {
        double diff = reward.getActivation() - previousReward;
        sim.deltaReward.forceSetActivation(diff);
    }

}
