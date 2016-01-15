package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.math.Vector3f;

public class AddEntityAction extends AbstractAction {
    private ThreeDWorld world;
    
    public AddEntityAction(ThreeDWorld world) {
        super("Add Entity");
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            Entity entity = Entity.createBox(world.getEngine(), "Box" + world.createId(), 1, 1, 1);
            world.getEngine().getRootNode().attachChild(entity.getNode());
            world.getEntities().add(entity);
            world.getEngine().getPhysicsSpace().add(entity.getBody());
            world.getSelectionController().select(entity);
            world.getSelectionController().translateToCursor();
            return null;
        });
    }
}
