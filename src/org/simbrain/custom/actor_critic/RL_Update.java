package org.simbrain.custom.actor_critic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;

/**
 * A custom updater for use in applying TD Learning and other custom update
 * features (e.g. only activating one vehicle network at a time based on the
 * output of a feed-forward net).
 *
 * For background on TD Learning see.
 * http://www.scholarpedia.org/article/Temporal_difference_learning
 */
//CHECKSTYLE:OFF
public class RL_Update implements NetworkUpdateAction {

    /** Reference to RL_Sim object that has all the main variables used. */
    ActorCritic sim;

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

    /** Current winning output neuron. */
    Neuron winner;

    /** For training the prediction network. */
    double[] lastPredictionLeft;
    double[] lastPredictionRight;
    double learningRate = .1;

    // Variables to help with the above
    private double previousReward;
    double[] previousInput;
    int counter = 0;


    /**
     * Construct the updater.
     */
    public RL_Update(ActorCritic sim) {
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

    /**
     * Custom update of the network, including application of TD Rules.
     */
    @Override
    public void invoke() {

        // Update inputs nodes
//        sim.inputs.update();

//        // Update Prediction subnets
//        sim.predictionLeft.update();
//        sim.predictionRight.update();

        // Train predition subnets
        trainPredictionNodes();

        // Outputs and vehicles
        Network.updateNeurons(Collections.singletonList(sim.value));

    }

    /**
     * Train the prediction nodes to predict the next input states.
     */
    private void trainPredictionNodes() {

//        setErrors(sim.leftInputs, sim.predictionLeft, lastPredictionLeft);
//        setErrors(sim.rightInputs, sim.predictionRight, lastPredictionRight);
//
//        trainDeltaRule(sim.leftInputToLeftPrediction);
//        trainDeltaRule(sim.outputToLeftPrediction);
//        trainDeltaRule(sim.rightInputToRightPrediction);
//        trainDeltaRule(sim.outputToRightPrediction);
//
//        lastPredictionLeft = sim.predictionLeft.getActivations();
//        lastPredictionRight = sim.predictionRight.getActivations();
    }
    
    /** 
     * Set errors on neuron groups
     */
    void setErrors(NeuronGroup inputs, NeuronGroup predictions,
            double[] lastPrediction) {
        int i = 0;
        double error = 0;
        sim.preditionError = 0;
        for (Neuron neuron : predictions.getNeuronList()) {
            error = inputs.getNeuronList().get(i).getActivation()
                    - lastPrediction[i];
            sim.preditionError += error * error;
            neuron.setAuxValue(error);
            i++;
        }
        sim.preditionError = Math.sqrt(sim.preditionError);
    }

    /** 
     * Train synapse groups
     */
    void trainDeltaRule(SynapseGroup group) {
        for (Synapse synapse : group.getAllSynapses()) {
            double newStrength = synapse.getStrength()
                    + learningRate * synapse.getSource().getActivation()
                            * synapse.getTarget().getAuxValue();
            synapse.setStrength(newStrength);
        }
    }

    /**
     * TD Error. Used to drive all learning in the network.
     */
    void updateTDError() {
        tdError.setActivation(sim.reward.getActivation()
                + sim.gamma * value.getActivation()
                - value.getLastActivation());
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

//    /**
//     * Update all "actor" neurons. (Roughly) If the last input > output
//     * connection led to reward, reinforce that connection.
//     */
//    void updateActor() {
//        for (Neuron neuron : sim.outputs.getNeuronList()) {
//            // Just update the last winner
//            if (neuron.getLastActivation() > 0) {
//                for (Synapse synapse : neuron.getFanIn()) {
//                    double previousActivation = getPreviousNeuronValue(
//                            synapse.getSource());
//                    double newStrength = synapse.getStrength() + sim.alpha
//                            * tdError.getActivation() * previousActivation;
//                    // synapse.setStrength(synapse.clip(newStrength));
//                    synapse.setStrength(newStrength);
//                }
//            }
//        }
//    }


}
