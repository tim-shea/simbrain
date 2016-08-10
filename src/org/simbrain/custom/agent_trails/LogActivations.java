package org.simbrain.custom.agent_trails;

import org.simbrain.network.core.NetworkUpdateAction;
import org.simbrain.util.Utils;

/**
 * TODO:
 */
public class LogActivations implements NetworkUpdateAction {

    /** Reference to simulation object that has all the main variables used. */
    AgentTrails sim;

    /**
     * Construct the updater.
     */
    public LogActivations(AgentTrails sim) {
        super();
        this.sim = sim;
    }


    public void invoke() {
        sim.activationList.add(
                Utils.getVectorString(sim.sensoryNet.getActivations(), ","));

        // System.out.print("Action:"
        // + Utils.getVectorString(sim.actionNet.getActivations(), ","));
        // System.out.print(Utils.getVectorString(sim.sensoryNet.getActivations(),
        // ","));
        // System.out.print(" Prediction:" + Utils
        // .getVectorString(sim.predictionNet.getActivations(), ","));
        // System.out.println();
    }

    // This is how the action appears in the update manager dialog
    public String getDescription() {
        return "Print activations";
    }

    // This is a longer description for the tooltip
    public String getLongDescription() {
        return "Print activations for diagnostic purposes";
    }

}
