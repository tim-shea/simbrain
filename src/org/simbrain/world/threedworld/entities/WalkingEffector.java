package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
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
        agent.getModel().setAnimation(idleAnimName, idleAnimSpeed);
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
        agent.getModel().getEngine().enqueue(() -> {
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
        agent.getModel().getEngine().enqueue(() -> {
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
            agent.getModel().setAnimation(walkAnimName, walkAnimSpeed);
        } else if (isMoving() && !walkingOrTurning) {
            moving = false;
            agent.getModel().setAnimation(idleAnimName, idleAnimSpeed);
        }
    }
    
    @Override
    public void update(float t) {
        checkMoving();
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(0, getTurning() * getTurnSpeed() * t, 0);
        agent.rotate(rotation);
        Vector3f direction = agent.getRotation().mult(Vector3f.UNIT_Z);
        Vector3f offset = direction.mult(getWalking() * getWalkSpeed() * t);
        agent.move(offset);
    }
    
    @Override
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
    
    @Override
    public Editor getEditor() {
        return new EffectorEditor(agent, this) {
        	private JFormattedTextField walkSpeedField = new JFormattedTextField(EditorDialog.floatFormat);
            private JFormattedTextField turnSpeedField = new JFormattedTextField(EditorDialog.floatFormat);
            private JTextField walkAnimNameField = new JTextField(20);
            private JTextField idleAnimNameField = new JTextField(20);
            
            {
                walkSpeedField.setColumns(5);
                turnSpeedField.setColumns(5);
            }
            
            @Override
            public JComponent layoutFields() {
                JComponent effectorComponent = super.layoutFields();
                
                getPanel().add(new JLabel("Walk Speed"));
                getPanel().add(walkSpeedField, "wrap");
                
                getPanel().add(new JLabel("Turn Speed"));
                getPanel().add(turnSpeedField, "wrap");
                
                getPanel().add(new JLabel("Walk Animation"));
                getPanel().add(walkAnimNameField, "wrap");
                
                getPanel().add(new JLabel("Idle Animation"));
                getPanel().add(idleAnimNameField, "wrap");
                
                return effectorComponent;
            }
            
            @Override
            public void readValues() {
                walkSpeedField.setValue(getWalkSpeed());
                turnSpeedField.setValue(getTurnSpeed());
                walkAnimNameField.setText(getWalkAnimName());
                idleAnimNameField.setText(getIdleAnimName());
            }
            
            @Override
            public void writeValues() {
                setWalkSpeed(((Number)walkSpeedField.getValue()).floatValue());
                setTurnSpeed(((Number)turnSpeedField.getValue()).floatValue());
                setWalkAnimName(walkAnimNameField.getText());
                setIdleAnimName(idleAnimNameField.getText());
            }
        };
    }
}