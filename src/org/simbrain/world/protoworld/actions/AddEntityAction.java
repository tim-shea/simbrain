package org.simbrain.world.protoworld.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.math.Vector3f;

public class AddEntityAction implements ActionListener {
    private ProtoWorld world;
    private int index = 0;
    
    public AddEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.enqueue(() -> {
            ProtoEntity entity = ProtoEntity.createBox(world, "Box" + index,
                    world.getAssetManager(), world.getPhysicsSpace(), 1, 1, 1);
            entity.getBody().setMass(1);
            entity.getBody().setGravity(new Vector3f(0, -10, 0));
            int x = (int)(Math.random() * 18) - 9;
            int z = (int)(Math.random() * 18) - 9;
            entity.setLocation(x, 5, z);
            world.getRootNode().attachChild(entity.getNode());
            world.getEntities().add(entity);
            world.getPhysicsSpace().add(entity.getBody());
            ++index;
            return null;
        });
    }
}
