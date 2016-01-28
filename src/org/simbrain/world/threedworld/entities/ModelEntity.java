package org.simbrain.world.threedworld.entities;

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
import com.jme3.bounding.BoundingBox;
import com.jme3.scene.Node;

public class ModelEntity extends PhysicalEntity {
    public static AttributeType animationProducerAttribute;
    public static AttributeType animationConsumerAttribute;
    
    public static List<AttributeType> getProducerTypes(
            WorkspaceComponent component) {
        animationProducerAttribute = new AttributeType(component, "Agent",
                "getAnimation", String.class, true);
        return Arrays.asList(animationProducerAttribute);
    }
    
    public static List<AttributeType> getConsumerTypes(
            WorkspaceComponent component) {
        animationConsumerAttribute = new AttributeType(component, "Agent",
                "setAnimation", String.class, true);
        return Arrays.asList(animationConsumerAttribute);
    }
    
    public static ModelEntity load(ThreeDEngine engine, String name, String nodeName, String modelName) {
        Node rootNode = (Node)engine.getAssetManager().loadModel(modelName);
        Node node = (Node)rootNode.getChild(nodeName);
        rootNode.detachChild(node);
        node.setName(name);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        return new ModelEntity(engine, node, nodeName, modelName);
    }
    
    private String nodeName;
    private String modelName;
    
    private ModelEntity(ThreeDEngine engine, Node node, String nodeName, String modelName) {
    	super(engine, node);
    	this.nodeName = nodeName;
    	this.modelName = modelName;
    }
    
    public String getNodeName() {
    	return nodeName;
    }
    
    public String getModelName() {
    	return modelName;
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
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = super.getPotentialProducers();
        AttributeManager attributeManager = animationProducerAttribute.getParentComponent().getAttributeManager();
        PotentialProducer producer = attributeManager.createPotentialProducer(this, "getAnimation", String.class);
        producer.setCustomDescription("Entity:Animation");
        producers.add(producer);
        return producers;
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> consumers = super.getPotentialConsumers();
        AttributeManager attributeManager = animationConsumerAttribute.getParentComponent().getAttributeManager();
        PotentialConsumer consumer = attributeManager.createPotentialConsumer(this, "setAnimation", String.class);
        consumer.setCustomDescription("Entity:Animation");
        consumers.add(consumer);
        return consumers;
    }
}
