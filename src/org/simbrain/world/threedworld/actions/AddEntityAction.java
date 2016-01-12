package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.math.Vector3f;

public class AddEntityAction extends AbstractAction {
    private ThreeDWorld world;
    private int index = 0;
    
    public AddEntityAction(ThreeDWorld world) {
        super("Add Entity");
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            Entity entity = Entity.createBox(world.getEngine(), "Box" + index, 1, 1, 1);
            //entity.getBody().setMass(1);
            //entity.getBody().setGravity(new Vector3f(0, -10, 0));
            int x = (int)(Math.random() * 18) - 9;
            int z = (int)(Math.random() * 18) - 9;
            entity.setLocation(x, 5, z);
            world.getEngine().getRootNode().attachChild(entity.getNode());
            world.getEntities().add(entity);
            world.getEngine().getPhysicsSpace().add(entity.getBody());
            ++index;
            return null;
        });
    }
}
