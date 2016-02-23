package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;

public class LoadSceneAction extends AbstractAction {
    private static final long serialVersionUID = -1555371103072097299L;
    
    private ThreeDWorld world;
    
    public LoadSceneAction(ThreeDWorld world) {
        super("Load Scene");
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            // TODO: Show dialog for scene selection
        });
    }
}
