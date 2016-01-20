package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ThreeDEngine;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.scene.Node;

public class Agent extends Entity {
    public static AttributeType animationProducerAttribute;
    public static AttributeType animationConsumerAttribute;
    
    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        animationProducerAttribute = new AttributeType(component, "Agent", "getAnimation", String.class, true);
        return Arrays.asList(animationProducerAttribute);
    }
    
    public static List<AttributeType> getConsumerTypes(WorkspaceComponent component) {
        animationConsumerAttribute = new AttributeType(component, "Agent", "setAnimation", String.class, true);
        return Arrays.asList(animationConsumerAttribute);
    }
    
    private List<Sensor> sensors = new ArrayList<Sensor>();
    private List<Effector> effectors = new ArrayList<Effector>();
    
    public Agent() {
        super();
    }
    
    public Agent(ThreeDEngine engine, Node node) {
        super(engine, node);
    }
    
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }
    
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }
    
    public <T extends Sensor> T getSensor(Class<T> sensorType) {
        for (Sensor sensor : sensors)
            if (sensorType.isAssignableFrom(sensor.getClass()))
                return (T)sensor;
        return null;
    }
    
    public void addEffector(Effector effector) {
        effectors.add(effector);
    }
    
    public void removeEffector(Effector effector) {
        effectors.remove(effector);
    }
    
    public <T extends Effector> T getEffector(Class<T> effectorType) {
        for (Effector effector : effectors)
            if (effectorType.isAssignableFrom(effector.getClass()))
                return (T)effector;
        return null;
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = super.getPotentialProducers();
        AttributeManager attributeManager = animationProducerAttribute.getParentComponent().getAttributeManager();
        producers.add(attributeManager.createPotentialProducer(this, "getAnimation", String.class));
        for (Sensor sensor : sensors)
            producers.addAll(sensor.getPotentialProducers());
        return producers;
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> consumers = super.getPotentialConsumers();
        AttributeManager attributeManager = animationConsumerAttribute.getParentComponent().getAttributeManager();
        consumers.add(attributeManager.createPotentialConsumer(this, "setAnimation", String.class));
        for (Effector effector : effectors)
            consumers.addAll(effector.getPotentialConsumers());
        return consumers;
    }
    
    public AnimControl getAnimator() {
        return getNode().getControl(AnimControl.class);
    }
    
    public String getAnimation() {
        AnimControl animator = getAnimator();
        if (animator == null || animator.getNumChannels() == 0)
            return null;
        else {
            AnimChannel animation = animator.getChannel(0);
            return animation.getAnimationName();
        }
    }
    
    public void setAnimation(String name) {
        setAnimation(name, 1);
    }
    
    public void setAnimation(String name, float speed) {
        AnimControl animator = getAnimator();
        if (animator != null && animator.getAnimationNames().contains(name)) {
            AnimChannel animation;
            if (animator.getNumChannels() == 0)
                animation = animator.createChannel();
            else
                animation = animator.getChannel(0);
            if (animation.getAnimationName() != name)
                animation.setAnim(name);
            if (animation.getSpeed() != speed)
                animation.setSpeed(speed);
        }
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
        for (Sensor sensor : sensors)
            sensor.update(tpf);
        for (Effector effector : effectors)
            effector.update(tpf);
    }
}
