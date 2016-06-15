package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;

public class CollisionSensor implements Sensor, PhysicsCollisionGroupListener {
    private class CollisionSensorEditor extends SensorEditor {
        private JTextField collisionNameField = new JTextField();

        {
            collisionNameField.setColumns(25);
        }

        private CollisionSensorEditor() {
            super(agent, CollisionSensor.this);
        }

        @Override
        public JComponent layoutFields() {
            JComponent sensorComponent = super.layoutFields();

            getPanel().add(new JLabel("Collision Name"));
            getPanel().add(collisionNameField, "wrap");

            return sensorComponent;
        }

        @Override
        public void readValues() {
            collisionNameField.setText(collisionName);
        }

        @Override
        public void writeValues() { }

        @Override
        public void close() { }
    }

    private static AttributeType collidingAttribute;
    private static AttributeType otherNameAttribute;

    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        collidingAttribute = new AttributeType(component, "CollisionSensor", "Colliding", double.class, true);
        otherNameAttribute = new AttributeType(component, "CollisionSensor", "CollisionName", String.class, true);
        return Arrays.asList(collidingAttribute, otherNameAttribute);
    }

    private Agent agent;
    private PhysicsCollisionObject other;
    private double colliding, nextColliding;
    private String collisionName;

    public CollisionSensor(Agent agent) {
        this.agent = agent;
        agent.addSensor(this);
        agent.getEngine().getPhysicsSpace().addCollisionGroupListener(this, PhysicsCollisionObject.COLLISION_GROUP_02);
    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    public double getColliding() {
        return colliding;
    }

    public String getCollisionName() {
        return collisionName;
    }

    @Override
    public boolean collide(PhysicsCollisionObject objectA, PhysicsCollisionObject objectB) {
        RigidBodyControl thisCollisionObject = agent.getNode().getControl(RigidBodyControl.class);
        if (objectA.equals(thisCollisionObject)) {
            other = objectB;
            nextColliding = objectA.getCollisionGroup();
        } else if (objectB.equals(thisCollisionObject)) {
            other = objectA;
            nextColliding = objectB.getCollisionGroup();
        }
        return true;
    }

    @Override
    public void update(float tpf) {
        colliding = nextColliding;
        nextColliding = PhysicsCollisionObject.COLLISION_GROUP_NONE;
        if (colliding != PhysicsCollisionObject.COLLISION_GROUP_NONE) {
            collisionName = other.toString();
        } else {
            other = null;
            collisionName = "";
        }
    }

    @Override
    public void delete() {
        agent.getEngine().getPhysicsSpace().removeCollisionGroupListener(PhysicsCollisionObject.COLLISION_GROUP_02);
        agent.removeSensor(this);
    }

    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = new ArrayList<PotentialProducer>();
        if (collidingAttribute.isVisible()) {
            AttributeManager attributeManager = collidingAttribute.getParentComponent().getAttributeManager();
            PotentialProducer producer = attributeManager.createPotentialProducer(this, "getColliding",
                    double.class);
            producer.setCustomDescription("CollisionSensor:Colliding");
            producers.add(producer);
        }
        if (otherNameAttribute.isVisible()) {
            AttributeManager attributeManager = otherNameAttribute.getParentComponent().getAttributeManager();
            PotentialProducer producer = attributeManager.createPotentialProducer(this, "getCollisionName",
                    String.class);
            producer.setCustomDescription("CollisionSensor:CollisionName");
            producers.add(producer);
        }
        return producers;
    }

    @Override
    public SensorEditor getEditor() {
        return new CollisionSensorEditor();
    }
}
