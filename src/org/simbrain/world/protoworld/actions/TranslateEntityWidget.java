package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.collision.CollisionResult;
import com.jme3.math.Vector3f;

public class TranslateEntityWidget extends CursorWidget {
    public TranslateEntityWidget(ProtoWorld world) {
        super(world);
    }
    
    @Override
    public void apply(float tpf, ProtoEntity entity) {
        CollisionResult contact = getCursorContact(entity.getNode());
        if (contact != null) {
            Vector3f contactPoint = contact.getContactPoint();
            if (isSnapped())
                contactPoint = snapToGrid(contactPoint, 1);
            Vector3f contactNormal = contact.getContactNormal();
            entity.setLocation(contactPoint.add(contactNormal));
        }
    }
}