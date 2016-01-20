package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.engine.ThreeDEngine;

public class RunAction extends AbstractAction {
    private ThreeDWorld world;
    
    public RunAction(ThreeDWorld world) {
        super("Run");
        this.world = world;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("Play.png"));
        putValue(SHORT_DESCRIPTION,
                "Run ThreeWorld (\"spacebar\")");
    }
    
    @Override public void actionPerformed(ActionEvent event) {
        if (world.getPreferences().isRealTime())
            world.getEngine().queueState(ThreeDEngine.State.Run, false);
        else
            world.getEngine().queueState(ThreeDEngine.State.UpdateSync, false);
        world.getAction("Update").setEnabled(false);
    }
}
