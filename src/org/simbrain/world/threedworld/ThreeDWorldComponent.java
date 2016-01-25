package org.simbrain.world.threedworld;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;
import org.simbrain.world.threedworld.entities.VisionSensor;
import org.simbrain.world.threedworld.entities.WalkingEffector;

public class ThreeDWorldComponent extends WorkspaceComponent {
    public static ThreeDWorldComponent open(InputStream input, String name, String format) {
        ThreeDWorld world = ThreeDWorld.deserialize(input, name, format);
        return new ThreeDWorldComponent(name, world);
    }
	
    private ThreeDWorld world;
    
    public ThreeDWorldComponent(String name) {
        super(name);
        world = new ThreeDWorld();
        addAttributeTypes();
    }
    
    public ThreeDWorldComponent(String name, ThreeDWorld world) {
        super(name);
        this.world = world;
        addAttributeTypes();
    }
    
    public ThreeDWorld getWorld() {
        return world;
    }
    
    private void addAttributeTypes() {
        for (AttributeType type : Entity.getProducerTypes(this))
            addProducerType(type);
        for (AttributeType type : Agent.getProducerTypes(this))
            addProducerType(type);
        for (AttributeType type : VisionSensor.getProducerTypes(this))
            addProducerType(type);
        for (AttributeType type : Entity.getConsumerTypes(this))
            addConsumerType(type);
        for (AttributeType type : Agent.getConsumerTypes(this))
            addConsumerType(type);
        for (AttributeType type : WalkingEffector.getConsumerTypes(this))
            addConsumerType(type);
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> potentialConsumers = new ArrayList<PotentialConsumer>();
        for (Entity entity : world.getEntities())
            potentialConsumers.addAll(entity.getPotentialConsumers());
        return potentialConsumers;
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> potentialProducers = new ArrayList<PotentialProducer>();
        for (Entity entity : world.getEntities())
            potentialProducers.addAll(entity.getPotentialProducers());
        return potentialProducers;
    }
    
    @Override
    public void save(OutputStream output, String format) {
        world.getEngine().queueState(ThreeDEngine.State.SystemPause, true);
        ThreeDWorld.getXStream().toXML(world, output);
        world.getEngine().queueState(ThreeDEngine.State.Render, false);
    }
    
    @Override
    protected void closing() {
        world.getEngine().stop(true);
    }
    
    @Override
    public void update() {
        world.getEngine().updateSync();
    }
}
