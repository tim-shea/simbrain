package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class EditEntityAction extends AbstractAction {
    private ThreeDWorld world;
    
    public EditEntityAction(ThreeDWorld world, boolean enabled) {
        super("Edit Entity");
        this.world = world;
        setEnabled(enabled);
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        world.getSelectionController().editSelection();
    }
}
