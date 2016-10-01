package org.simbrain.custom.hippocampus;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.subnetworks.CompetitiveGroup;
import org.simbrain.network.subnetworks.WinnerTakeAll;
import org.simbrain.util.randomizer.Randomizer;

/**
 * Extends competitive group with functions specific to the hippocampus
 * simulation.
 */
public class AlvarezSquire extends CompetitiveGroup {

    /** Noise generator. */
    private Randomizer noiseGenerator = new Randomizer();

    /**
     * Construct the group.
     *
     * @param root parent network
     * @param numNeurons number neurons
     */
    public AlvarezSquire(Network root, int numNeurons) {
        super(root, numNeurons);
        // TODO
        noiseGenerator.setParam1(-.05);
        noiseGenerator.setParam2(.05);
    }

    @Override
    public void update() {
        super.update();

        // For this simulation we can assume that if one neuron is clamped, they
        // all are
        boolean clamped = getNeuronList().get(0).isClamped();
        Neuron winner = WinnerTakeAll.getWinner(getNeuronList(), clamped);

        // Update weights on winning neuron
        for (int i = 0; i < getNeuronList().size(); i++) {
            Neuron neuron = getNeuronList().get(i);
            if (neuron == winner) {
                // TODO: Use library for clipping
                double val = .7 * neuron.getActivation()
                        + neuron.getWeightedInputs()
                        + noiseGenerator.getRandom();
                neuron.forceSetActivation((val > 0) ? val : 0);
                neuron.forceSetActivation((val < 1) ? val : 1);
                updateWeights(neuron);
                // decayAllSynapses();
            } else {
                neuron.setActivation(this.getLoseValue());
            }
        }
    }

    /**
     * Custom weight update
     *
     * @param neuron winning neuron whose incoming synapses will be updated
     */
    private void updateWeights(final Neuron neuron) {
        double lambda;

        // TODO. Remove reliance on labels
        for (Synapse synapse : neuron.getFanIn()) {
            if (synapse.getSource().getParentGroup().getLabel()
                    .equalsIgnoreCase("Hippocampus")) {
                lambda = .1;
            } else if (synapse.getTarget().getParentGroup().getLabel()
                    .equalsIgnoreCase("Hippocampus")) {
                lambda = .1;
            } else {
                lambda = .002;
            }
            double deltaw = lambda * synapse.getTarget().getActivation()
                    * (synapse.getSource().getActivation()
                            - synapse.getTarget().getAverageInput());
            synapse.setStrength(synapse.clip(synapse.getStrength() + deltaw));

        }
    }

}
