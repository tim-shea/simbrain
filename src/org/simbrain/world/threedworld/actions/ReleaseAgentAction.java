package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;

public class ReleaseAgentAction extends AbstractAction {
    private ThreeDWorld world;
    
    public ReleaseAgentAction(ThreeDWorld world) {
        super("Release Agent");
        this.world = world;
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (world.getAgentController().getAgent() != null) {
            world.getAgentController().setAgent(null);
            world.getAgentController().setEnabled(false);
            world.getSelectionController().setEnabled(true);
            world.getCameraController().setEnabled(true);
        }
    }
}
