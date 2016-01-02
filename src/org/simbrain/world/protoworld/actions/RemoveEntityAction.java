package org.simbrain.world.protoworld.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

public class RemoveEntityAction implements ActionListener {
    private ProtoWorld world;
    
    public RemoveEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.enqueue(() -> {
            ProtoEntity entity = world.getSelectedEntity();
            if (entity != null) {
                world.getRootNode().detachChild(entity.getNode());
                world.getPhysicsSpace().remove(entity.getNode().getControl(RigidBodyControl.class));
                world.getEntities().remove(entity);
            }
            world.setSelectedEntity(null);
            return null;
        });
    }
}
