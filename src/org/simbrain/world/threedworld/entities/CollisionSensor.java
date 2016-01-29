package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;

public class CollisionSensor implements Sensor, PhysicsCollisionListener {
    private static AttributeType collidingAttribute;
    private static AttributeType otherNameAttribute;
    
    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        collidingAttribute = new AttributeType(component, "CollisionSensor", "Colliding", double.class, true);
        otherNameAttribute = new AttributeType(component, "CollisionSensor", "CollisionName", String.class, true);
        return Arrays.asList(collidingAttribute, otherNameAttribute);
    }
    
    private Agent agent;
    private PhysicsCollisionObject other;
    private boolean colliding;
    private String collisionName;
    
    public CollisionSensor(Agent agent) {
        this.agent = agent;
        agent.addSensor(this);
        agent.getEngine().getPhysicsSpace().addCollisionListener(this);
    }
    
    public boolean isColliding() {
        return colliding;
    }
    
    public double coupledIsColliding() {
        return colliding ? 1 : 0;
    }
    
    public String getCollisionName() {
        return collisionName;
    }
    
    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA().equals(agent))
            other = event.getObjectB();
        else if (event.getObjectB().equals(agent))
            other = event.getObjectA();
    }
    
    @Override
    public void update(float tpf) {
        if (other != null) {
            colliding = true;
            collisionName = other.toString();
            other = null;
        } else {
            colliding = false;
            collisionName = "";
        }
    }
    
    @Override
    public void delete() {
        agent.getEngine().getPhysicsSpace().removeCollisionListener(this);
        agent.removeSensor(this);
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = new ArrayList<PotentialProducer>();
        if (collidingAttribute.isVisible()) {
            AttributeManager attributeManager = collidingAttribute.getParentComponent().getAttributeManager();
            PotentialProducer producer = attributeManager.createPotentialProducer(this, "coupledIsColliding", double.class);
            producer.setCustomDescription("CollisionSensor:Colliding");
            producers.add(producer);
        }
        if (otherNameAttribute.isVisible()) {
            AttributeManager attributeManager = otherNameAttribute.getParentComponent().getAttributeManager();
            PotentialProducer producer = attributeManager.createPotentialProducer(this, "getCollisionName", String.class);
            producer.setCustomDescription("CollisionSensor:CollisionName");
            producers.add(producer);
        }
        return producers;
    }
    
    @Override
    public SensorEditor getEditor() {
        return new SensorEditor(agent, this);
    }
}
