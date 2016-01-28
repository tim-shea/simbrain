package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;

import com.jme3.math.Vector3f;

public class EntityLocationAdapter {
    public static AttributeType producerAttribute;
    public static AttributeType consumerAttribute;
    
    public static AttributeType getProducerType(WorkspaceComponent component) {
        producerAttribute = new AttributeType(component, "Entity", "Position", double.class, true);
        return producerAttribute;
    }
    
    public static AttributeType getConsumerType(WorkspaceComponent component) {
        consumerAttribute = new AttributeType(component, "Entity", "Position", double.class, true);
        return consumerAttribute;
    }
    
    private Entity entity;
    
    EntityLocationAdapter(Entity entity) {
        this.entity = entity;
    }
    
    public double getX() {
        return entity.getPosition().x;
    }
    
    public void setX(double value) {
        Vector3f location = entity.getPosition();
        location.x = (float)value;
        entity.queuePosition(location);
    }
    
    public double getY() {
        return entity.getPosition().y;
    }
    
    public void setY(double value) {
        Vector3f location = entity.getPosition();
        location.y = (float)value;
        entity.queuePosition(location);
    }
    
    public double getZ() {
        return entity.getPosition().z;
    }
    
    public void setZ(double value) {
        Vector3f location = entity.getPosition();
        location.z = (float)value;
        entity.queuePosition(location);
    }
    
    public List<PotentialProducer> getPotentialProducers() {
        if (producerAttribute.isVisible()) {
            WorkspaceComponent component = producerAttribute.getParentComponent();
            return Arrays.asList(
                    createPotentialProducer(component.getAttributeManager(), "X"),
                    createPotentialProducer(component.getAttributeManager(), "Y"),
                    createPotentialProducer(component.getAttributeManager(), "Z"));
        } else
            return new ArrayList<PotentialProducer>();
    }
    
    private PotentialProducer createPotentialProducer(AttributeManager attributeManager, String dimension) {
        PotentialProducer producer = attributeManager.createPotentialProducer(this, "get" + dimension, double.class);
        producer.setCustomDescription("Entity:Position:" + dimension);
        return producer;
    }
    
    public List<PotentialConsumer> getPotentialConsumers() {
        if (consumerAttribute.isVisible()) {
            WorkspaceComponent component = consumerAttribute.getParentComponent();
            return Arrays.asList(
                    createPotentialConsumer(component.getAttributeManager(), "X"),
                    createPotentialConsumer(component.getAttributeManager(), "Y"),
                    createPotentialConsumer(component.getAttributeManager(), "Z"));
        } else
            return new ArrayList<PotentialConsumer>();
    }
    
    private PotentialConsumer createPotentialConsumer(AttributeManager attributeManager, String dimension) {
        PotentialConsumer consumer = attributeManager.createPotentialConsumer(this, "set" + dimension, double.class);
        consumer.setCustomDescription("Entity:Position:" + dimension);
        return consumer;
    }
}