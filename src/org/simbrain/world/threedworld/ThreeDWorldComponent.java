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
import org.simbrain.world.threedworld.actions.ActionManager;
import org.simbrain.world.threedworld.controllers.CameraController;
import org.simbrain.world.threedworld.controllers.SelectionController;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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
        addProducerType(new AttributeType(this, "Location", "getLocationX", double.class, true));
        addProducerType(new AttributeType(this, "Location", "getLocationY", double.class, true));
        addProducerType(new AttributeType(this, "Location", "getLocationZ", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationW", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationX", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationY", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationZ", double.class, true));
        addProducerType(new AttributeType(this, "Sensor:Vision", "getViewData", double[].class, true));
        addConsumerType(new AttributeType(this, "Location", "setLocationX", double.class, true));
        addConsumerType(new AttributeType(this, "Location", "setLocationY", double.class, true));
        addConsumerType(new AttributeType(this, "Location", "setLocationZ", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationW", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationX", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationY", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationZ", double.class, true));
        addConsumerType(new AttributeType(this, "Effector:Walking", "setWalking", double.class, true));
        addConsumerType(new AttributeType(this, "Effector:Turning", "setTurning", double.class, true));
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> potentialConsumers = new ArrayList<PotentialConsumer>();
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Agent) {
                Agent agent = (Agent)entity;
                potentialConsumers.add(getAttributeManager().createPotentialConsumer(
                        agent.getWalkingEffector(), "walkCoupling", double.class));
                potentialConsumers.add(getAttributeManager().createPotentialConsumer(
                        agent.getWalkingEffector(), "turnCoupling", double.class));
            }
            for (AttributeType type : getVisibleConsumerTypes()) {
                if (!type.getTypeName().contains("Effector")) {
                    PotentialConsumer consumer = getAttributeManager().createPotentialConsumer(entity, type);
                    potentialConsumers.add(consumer);
                }
            }
        }
        return potentialConsumers;
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> potentialProducers = new ArrayList<PotentialProducer>();
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Agent) {
                Agent agent = (Agent)entity;
                potentialProducers.add(getAttributeManager().createPotentialProducer(
                        agent.getVisionSensor(), "getViewData", double[].class));
            }
            for (AttributeType type : getVisibleProducerTypes()) {
                if (!type.getTypeName().contains("Sensor")) {
                    PotentialProducer producer = getAttributeManager().createPotentialProducer(entity, type);
                    potentialProducers.add(producer);
                }
            }
        }
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
