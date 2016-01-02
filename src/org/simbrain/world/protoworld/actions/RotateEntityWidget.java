package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class RotateEntityWidget extends CursorWidget {
    private Vector3f up = Vector3f.UNIT_Y;
    
    public RotateEntityWidget(ProtoWorld world) {
        super(world);
    }
    
    public Vector3f getUpVector() {
        return up;
    }
    
    public void setUpVector(Vector3f value) {
        up = value;
    }
    
    @Override
    public void apply(float tpf, ProtoEntity entity) {
        CollisionResult contact = getCursorContact(entity.getNode());
        if (contact != null) {
            Vector3f target = contact.getContactPoint().subtract(entity.getLocation());
            target = target.subtract(up.mult(target.dot(up)));
            Quaternion rotation = entity.getOrientation();
            rotation.lookAt(target, up);
            if (isSnapped()) {
            float[] angles = rotation.toAngles(null);
                angles[0] = snapToStep(angles[0], (float)Math.PI / 8);
                angles[1] = snapToStep(angles[1], (float)Math.PI / 8);
                angles[2] = snapToStep(angles[2], (float)Math.PI / 8);
                rotation.fromAngles(angles);
            }
            entity.setOrientation(rotation);
        }
    }
}