package org.simbrain.custom.rl_sim;

import java.util.ArrayList;
import java.util.List;

import org.simbrain.simulation.ControlPanel;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;

/**
 * Superclass for particular RL Sim, which simply configures the mouse and
 * object positions and smell properties, as well as setting the goal objects.
 */
// CHECKSTYLE:OFF
public abstract class RL_Sim {

    /** Back reference to main simulation object. */
    RL_Sim_Main mainSim;

    /** Sub-control panel for this RL_Sim. */
    ControlPanel cp = new ControlPanel();

    /**
     * Initial mouse location in this world. Every time a new trial is run the
     * mouse is set to these values.
     */
    int initialMouseLocation_x;
    int initialMouseLocation_y;
    int initialMouseHeading;

    /** List of goal entities in this world. */
    List<OdorWorldEntity> goalEntities = new ArrayList<OdorWorldEntity>();

    /**
     * Construct an RL sim with a reference to the main simulation object.
     *
     * @param mainSim the main rl sim, with reference to relevant objects.
     */
    public RL_Sim(RL_Sim_Main mainSim) {
        this.mainSim = mainSim;
    }

    /**
     * Load the simulation.
     */
    public abstract void load();

}
