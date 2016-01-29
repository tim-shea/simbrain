package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class DeleteEntityAction extends AbstractAction {
    private static final long serialVersionUID = -4679471843215287046L;
    
    private ThreeDWorld world;
    
    public DeleteEntityAction(ThreeDWorld world, boolean enabled) {
        super("Delete Entity");
        this.world = world;
        setEnabled(enabled);
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            world.getSelectionController().deleteSelection();
            return null;
        });
    }
}
