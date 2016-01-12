package org.simbrain.world.threedworld.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class DebugPhysicsAction extends AbstractAction {
    private ThreeDWorld world;
    
    public DebugPhysicsAction(ThreeDWorld world) {
        super("Debug Physics");
        this.world = world;
        ImageIcon icon = ResourceManager.getImageIcon("Physics.png");
        icon.setImage(icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "Debug Physics");
    }
    
    @Override public void actionPerformed(ActionEvent event) {
        JToggleButton source = (JToggleButton)event.getSource();
        world.getEngine().setDebugPhysics(source.isSelected());
    }
}
