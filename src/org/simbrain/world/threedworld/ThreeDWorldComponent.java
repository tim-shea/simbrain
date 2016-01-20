package org.simbrain.world.threedworld;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.util.SFileChooser;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;
import org.simbrain.world.threedworld.entities.VisionSensor;
import org.simbrain.world.threedworld.entities.WalkingEffector;

import com.jme3.export.binary.BinaryExporter;

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
        if (world.getPreferences().getSceneName().isEmpty()) {
            SFileChooser chooser = new SFileChooser("simulations/worlds/assets/Scenes", "Save Scene");
            chooser.addExtension("JME3 Scene Graph", "j3o");
            File sceneFile = chooser.showSaveDialog();
            world.getPreferences().setSceneName("Scenes/" + sceneFile.getName());
        }
        if (world.getPreferences().getGuiName().isEmpty()) {
            SFileChooser chooser = new SFileChooser("simulations/worlds/assets/Interface", "Save GUI");
            chooser.addExtension("JME3 Scene Graph", "j3o");
            File guiFile = chooser.showSaveDialog();
            world.getPreferences().setGuiName("Interface/" + guiFile.getName());
        }
        ThreeDWorld.getXStream().toXML(world, output);
        BinaryExporter exporter = BinaryExporter.getInstance();
        try {
            exporter.save(world.getEngine().getRootNode(),
                    new File("simulations/worlds/assets/", world.getPreferences().getSceneName()));
            exporter.save(world.getEngine().getGuiNode(),
                    new File("simulations/worlds/assets/", world.getPreferences().getGuiName()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to save ThreeDWorld", e);
        }
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
