package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;

public class ControlAgentAction extends AbstractAction {
    private ThreeDWorld world;
    
    public ControlAgentAction(ThreeDWorld world) {
        super("Control Agent");
        this.world = world;
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (!world.getSelectionController().hasSelection())
            return;
        Entity entity = world.getSelectionController().getSelectedEntity();
        if (entity instanceof Agent) {
            world.getAgentController().setAgent((Agent)entity);
            world.getAgentController().setEnabled(true);
            world.getSelectionController().setEnabled(false);
            world.getCameraController().setEnabled(false);
        }
    }
}
