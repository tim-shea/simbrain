package org.simbrain.world.threedworld.entities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

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
    
    private static Node loadModel(ThreeDEngine engine, String fileName) {
        Node rootNode = (Node)engine.getAssetManager().loadModel(fileName);
        Node modelNode = (Node)rootNode.getChild("ModelNode");
        modelNode.setModelBound(new BoundingBox());
        modelNode.updateModelBound();
        rootNode.detachChild(modelNode);
        return modelNode;
    }
    
    public static ModelEntity load(ThreeDEngine engine, String name, String fileName) {
        Node node = loadModel(engine, fileName);
        node.setName(name);
        return new ModelEntity(engine, node, fileName);
    }
    
    private String fileName;
    
    private ModelEntity(ThreeDEngine engine, Node node, String fileName) {
    	super(engine, node);
    	this.fileName = fileName;
    }
    
    public String getFileName() {
    	return fileName;
    }
    
    public void reload(String fileName) {
        Node node = loadModel(getEngine(), fileName);
        node.setName(getName());
        setNode(node);
        this.fileName = fileName;
    }
    
    public AnimControl getAnimator() {
        return getNode().getControl(AnimControl.class);
    }
    
    public Collection<String> getAnimations() {
        if (getAnimator() != null)
            return getAnimator().getAnimationNames();
        else
            return Collections.emptyList();
    }
    
    public boolean hasAnimation(String name) {
        return getAnimations().contains(name);
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
        if (hasAnimation(name)) {
            AnimChannel animation;
            if (getAnimator().getNumChannels() == 0)
                animation = getAnimator().createChannel();
            else
                animation = getAnimator().getChannel(0);
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
    
    @Override
    public Editor getEditor() {
        return new ModelEditor(this);
    }
}
