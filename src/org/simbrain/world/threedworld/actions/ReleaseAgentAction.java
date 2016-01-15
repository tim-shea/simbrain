package org.simbrain.world.threedworld.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;

public class ReleaseAgentAction extends AbstractAction {
    private ThreeDWorld world;
    
    public ReleaseAgentAction(ThreeDWorld world) {
        super("Release Agent");
        this.world = world;
        ImageIcon icon = ResourceManager.getImageIcon("ControlEmpty.png");
        icon.setImage(icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "Release Agent");
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (world.getAgentController().isEnabled())
            world.getAgentController().release();
    }
}
