package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class ScaleEntityWidget extends CursorWidget {
    private CollisionResult initialContact = null;
    
    public ScaleEntityWidget(ProtoWorld world) {
        super(world);
    }
    
    @Override
    public void setActive(boolean value) {
        if (value)
            initialContact = getCursorContact(null);
        else
            initialContact = null;
        super.setActive(value);
    }
    
    @Override
    public void apply(float tpf, ProtoEntity entity) {
        CollisionResult contact = getCursorContact(null);
        if (initialContact != null && contact != null) {
            float initialDistanceToEntity = initialContact.getContactPoint().distance(entity.getLocation());
            initialDistanceToEntity = Float.max(initialDistanceToEntity, 0.0001f);
            float currentDistanceToEntity = contact.getContactPoint().distance(entity.getLocation());
            float scale = currentDistanceToEntity / initialDistanceToEntity;
            if (isSnapped())
                scale = snapToStep(scale, 0.1f);
            entity.setScale(scale);
        }
    }
}