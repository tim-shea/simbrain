package org.simbrain.custom.rl_sim;

import java.util.Collections;

import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.util.NeuronWithMemory;

public class RL_Update implements NetworkUpdateAction {

    public String getDescription() {
        return "Custom TD Rule";
    }

    public String getLongDescription() {
        return "Custom TD Rule";
    }

    public void invoke() {
        // Update all neurons (Just state neurons?)
//        network.updateNeurons(tileNeurons);
//        network.updateNeurons(Collections.singletonList(valueNeuron));
//        network.updateNeurons(Collections.singletonList(rewardNeuron));
//
//        // Determine winning neuron and update action neurons
//        //  TODO: Break ties randomly
//        //  TOOD: Use WTA subnetwork?
//        NeuronWithMemory winningNeuron;
//        double maxVal; 
//        if (Math.random() > epsilon) {
//            maxVal = Double.NEGATIVE_INFINITY;
//            for(NeuronWithMemory neuron : actionNeurons) {
//                if (neuron.getWeightedInputs() > maxVal) {
//                    maxVal = neuron.getWeightedInputs();
//                    winningNeuron = neuron;
//                }
//            }      
//            // Break ties randomly
//            if (maxVal == 0) {
//                int winner = generator.nextInt(actionNeurons.size());
//                winningNeuron = actionNeurons.get(winner);                    
//            }
//        } else {                
//            // Choose winner randomly
//            int winner = generator.nextInt(actionNeurons.size());
//            winningNeuron = actionNeurons.get(winner);
//        }            
//        for(NeuronWithMemory neuron : actionNeurons) {
//            if (neuron == winningNeuron) {
//                neuron.setActivation(tileSize * movementFactor);
//                neuron.update();
//            } else {
//                neuron.setActivation(0);
//            }
//        }
//
//        // Set main variables
//        //valueNeuron.setActivation(maxVal);
//        tdErrorNeuron.setActivation((rewardNeuron.getActivation() + gamma * valueNeuron.getActivation()) - valueNeuron.getLastActivation());
//        //System.out.println("td error:" + valueNeuron.getActivation() + " + " + rewardNeuron.getActivation() + " - " + valueNeuron.lastActivation);
//
//
//        // Update all value synapses 
//        for (Synapse synapse : valueNeuron.getFanIn()) {
//            NeuronWithMemory sourceNeuron = synapse.getSource(); 
//            // Reinforce based on the source neuron's last activation (not its current value), 
//            //  since that is what the current td error reflects.
//            double newStrength = synapse.getStrength() + alpha * tdErrorNeuron.getActivation() * sourceNeuron.getLastActivation();
//            //synapse.setStrength(synapse.clip(newStrength)); 
//            synapse.setStrength(newStrength); 
//            //System.out.println("Value Neuron / Tile neuron (" + sourceNeuron.getId() + "):" + newStrength);
//        }
//        // Update all actor neurons
//        for (NeuronWithMemory neuron : actionNeurons) {
//            // Just update the last winner
//            if (neuron.getLastActivation() > 0) {
//                for (Synapse synapse : neuron.getFanIn()) {
//                    Neuron sourceNeuron = synapse.getSource(); 
//                    // Reinforce actions based on the source neuron's last activation (not its current value), 
//                    //  since that is what the current td error reflects.
//                    double newStrength = synapse.getStrength() + alpha * tdErrorNeuron.getActivation() * sourceNeuron.getLastActivation();
//                    synapse.setStrength(synapse.clip(newStrength)); 
//                    //synapse.setStrength(newStrength); 
//                    //System.out.println("Neuron (" + neuron.getLabel() + ") / Tile //neuron (" + sourceNeuron.getId() + "):" + newStrength);
//                }
//                
//            }
//        } 

    
    }

}
