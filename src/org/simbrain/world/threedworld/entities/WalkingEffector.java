package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.WorkspaceComponent;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class WalkingEffector implements Effector {
    public static AttributeType walkAttribute;
    public static AttributeType turnAttribute;
    
    public static List<AttributeType> getConsumerTypes(WorkspaceComponent component) {
        walkAttribute = new AttributeType(component, "WalkingEffector", "Walk", double.class, true);
        turnAttribute = new AttributeType(component, "WalkingEffector", "Turn", double.class, true);
        return Arrays.asList(walkAttribute, turnAttribute);
    }
    
    private Agent agent;
    private float walkSpeed = 3;
    private float turnSpeed = 3;
    private String walkAnimName = "Walk";
    private String idleAnimName = "Idle";
    private float walkAnimSpeed = 1;
    private float idleAnimSpeed = 1;
    private float walking = 0;
    private float turning = 0;
    private boolean moving = false;
    
    public WalkingEffector(Agent agent) {
        this.agent = agent;
        agent.addEffector(this);
        agent.setAnimation(idleAnimName, idleAnimSpeed);
    }
    
    public float getWalkSpeed() {
        return walkSpeed;
    }
    
    public void setWalkSpeed(float value) {
        walkSpeed = value;
    }
    
    public float getTurnSpeed() {
        return turnSpeed;
    }
    
    public void setTurnSpeed(float value) {
        turnSpeed = value;
    }
    
    public String getWalkAnimName() {
        return walkAnimName;
    }
    
    public void setWalkAnimName(String value) {
        walkAnimName = value;
    }
    
    public String getIdleAnimName() {
        return idleAnimName;
    }
    
    public void setIdleAnimName(String value) {
        idleAnimName = value;
    }
    
    public float getWalkAnimSpeed() {
        return walkAnimSpeed;
    }
    
    public void setWalkAnimSpeed(float value) {
        walkAnimSpeed = value;
    }
    
    public float getIdleAnimSpeed() {
        return idleAnimSpeed;
    }
    
    public void setIdleAnimSpeed(float value) {
        idleAnimSpeed = value;
    }
    
    public float getWalking() {
        return walking;
    }
    
    public boolean isWalking() {
        return FastMath.abs(walking) > 0.01;
    }
    
    public void setWalking(float value) {
        walking = value;
    }
    
    public void queueWalking(double value) {
        agent.getEngine().enqueue(() -> {
            setWalking((float)value);
            return null;
        });
    }
    
    public float getTurning() {
        return turning;
    }
    
    public boolean isTurning() {
        return FastMath.abs(turning) > 0.01;
    }
    
    public void setTurning(float value) {
        turning = value;
    }
    
    public void queueTurning(double value) {
        agent.getEngine().enqueue(() -> {
            setTurning((float)value);
            return null;
        });
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    private void checkMoving() {
        boolean walkingOrTurning = isWalking() || isTurning();
        if (!isMoving() && walkingOrTurning) {
            moving = true;
            this.agent.setAnimation(walkAnimName, walkAnimSpeed);
        } else if (isMoving() && !walkingOrTurning) {
            moving = false;
            this.agent.setAnimation(idleAnimName, idleAnimSpeed);
        }
    }
    
    public void update(float tpf) {
        checkMoving();
        this.agent.getNode().rotate(0, getTurning() * turnSpeed * tpf, 0);
        Vector3f direction = this.agent.getNode().getLocalRotation().mult(Vector3f.UNIT_Z);
        Vector3f translation = direction.mult(getWalking() * walkSpeed * tpf);
        this.agent.getNode().move(translation);
    }
    
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> consumers = new ArrayList<PotentialConsumer>();
        if (walkAttribute.isVisible()) {
            AttributeManager attributeManager = walkAttribute.getParentComponent().getAttributeManager();
            PotentialConsumer consumer = attributeManager.createPotentialConsumer(this, "queueWalking", double.class);
            consumer.setCustomDescription("WalkingEffector:Walk");
            consumers.add(consumer);
        }
        if (turnAttribute.isVisible()) {
            AttributeManager attributeManager = turnAttribute.getParentComponent().getAttributeManager();
            PotentialConsumer consumer = attributeManager.createPotentialConsumer(this, "queueTurning", double.class);
            consumer.setCustomDescription("WalkingEffector:Turn");
            consumers.add(consumer);
        }
        return consumers;
    }
}