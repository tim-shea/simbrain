package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDEngine;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class RunAction extends AbstractAction {
    private ThreeDWorld world;
    
    public RunAction(ThreeDWorld world) {
        super("Update");
        this.world = world;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("Play.png"));
        putValue(SHORT_DESCRIPTION,
                "Run ThreeWorld (\"spacebar\")");
    }
    
    @Override public void actionPerformed(ActionEvent event) {
        world.getEngine().setState(ThreeDEngine.State.Run, false);
    }
}
