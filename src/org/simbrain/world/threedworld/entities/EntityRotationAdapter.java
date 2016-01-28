package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;

public class EntityRotationAdapter {
    public static AttributeType producerAttribute;
    public static AttributeType consumerAttribute;
    
    public static AttributeType getProducerType(WorkspaceComponent component) {
        producerAttribute = new AttributeType(component, "Entity", "Rotation", double.class, true);
        return producerAttribute;
    }
    
    public static AttributeType getConsumerType(WorkspaceComponent component) {
        consumerAttribute = new AttributeType(component, "Entity", "Rotation", double.class, true);
        return consumerAttribute;
    }
    
    private Entity entity;
    
    EntityRotationAdapter(Entity entity) {
        this.entity = entity;
    }
    
    public double getYaw() {
        return FastMath.RAD_TO_DEG * getAngles()[1];
    }
    
    public void setYaw(double value) {
        float[] angles = getAngles();
        angles[1] = FastMath.DEG_TO_RAD * (float)value;
        setAngles(angles);
    }
    
    public double getPitch() {
        return FastMath.RAD_TO_DEG * getAngles()[0];
    }
    
    public void setPitch(double value) {
        float[] angles = getAngles();
        angles[0] = FastMath.DEG_TO_RAD * (float)value;
        setAngles(angles);
    }
    
    public double getRoll() {
        return FastMath.RAD_TO_DEG * getAngles()[2];
    }
    
    public void setRoll(double value) {
        float[] angles = getAngles();
        angles[2] = FastMath.DEG_TO_RAD * (float)value;
        setAngles(angles);
    }
    
    private float[] getAngles() {
        return entity.getRotation().toAngles(null);
    }
    
    private void setAngles(float[] angles) {
        Quaternion rotation = entity.getRotation();
        rotation.fromAngles(angles);
        entity.queueRotation(rotation);
    }
    
    public List<PotentialProducer> getPotentialProducers() {
        if (producerAttribute.isVisible()) {
            WorkspaceComponent component = producerAttribute.getParentComponent();
            return Arrays.asList(
                    createPotentialProducer(component.getAttributeManager(), "Yaw"),
                    createPotentialProducer(component.getAttributeManager(), "Pitch"),
                    createPotentialProducer(component.getAttributeManager(), "Roll"));
        } else
            return new ArrayList<PotentialProducer>();
    }
    
    private PotentialProducer createPotentialProducer(AttributeManager attributeManager, String dimension) {
        PotentialProducer producer = attributeManager.createPotentialProducer(this, "get" + dimension, double.class);
        producer.setCustomDescription("Entity:Rotation:" + dimension);
        return producer;
    }
    
    public List<PotentialConsumer> getPotentialConsumers() {
        if (consumerAttribute.isVisible()) {
            WorkspaceComponent component = consumerAttribute.getParentComponent();
            return Arrays.asList(
                    createPotentialConsumer(component.getAttributeManager(), "Yaw"),
                    createPotentialConsumer(component.getAttributeManager(), "Pitch"),
                    createPotentialConsumer(component.getAttributeManager(), "Roll"));
        } else
            return new ArrayList<PotentialConsumer>();
    }
    
    private PotentialConsumer createPotentialConsumer(AttributeManager attributeManager, String dimension) {
        PotentialConsumer consumer = attributeManager.createPotentialConsumer(this, "set" + dimension, double.class);
        consumer.setCustomDescription("Entity:Rotation:" + dimension);
        return consumer;
    }
}