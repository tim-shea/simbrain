package org.simbrain.world.threedworld.entities;

import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public interface Entity {
    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getProducerType(component),
                EntityRotationAdapter.getProducerType(component));
    }
    
    public static List<AttributeType> getConsumerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getConsumerType(component),
                EntityRotationAdapter.getConsumerType(component));
    }
    
    String getName();
    
    void setName(String value);
    
    Node getNode();
    
    Vector3f getPosition();
    
    void setPosition(Vector3f value);
    
    void queuePosition(Vector3f value);
    
    void move(Vector3f offset);
    
    Quaternion getRotation();
    
    void setRotation(Quaternion value);
    
    void queueRotation(Quaternion value);
    
    void rotate(Quaternion rotation);
    
    BoundingVolume getBounds();
    
    List<PotentialProducer> getPotentialProducers();
    
    List<PotentialConsumer> getPotentialConsumers();
    
    void update(float t);
    
    void delete();
    
    Editor getEditor();
}
