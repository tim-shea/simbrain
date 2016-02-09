package org.simbrain.world.threedworld.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.engine.ThreeDEngine;

public class ToggleRunAction extends AbstractAction {
    private static final long serialVersionUID = -8541379191902665764L;
    
    private ThreeDWorld world;
    
    public ToggleRunAction(ThreeDWorld world) {
        super("Toggle Run");
        this.world = world;
        ImageIcon icon = ResourceManager.getImageIcon("Physics.png");
        icon.setImage(icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "Toggle Run");
    }
    
    @Override public void actionPerformed(ActionEvent event) {
        ThreeDEngine.State toggleState = (world.getEngine().getState() == ThreeDEngine.State.RenderOnly ?
                ThreeDEngine.State.RunAll : ThreeDEngine.State.RenderOnly);
        world.getEngine().queueState(toggleState, false);
    }
}
