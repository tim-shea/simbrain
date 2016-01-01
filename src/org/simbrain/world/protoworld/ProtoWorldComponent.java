package org.simbrain.world.protoworld;

import java.awt.Canvas;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.effectors.Speech;
import org.simbrain.world.odorworld.effectors.StraightMovement;
import org.simbrain.world.odorworld.effectors.Turning;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;
import org.simbrain.world.odorworld.sensors.Hearing;
import org.simbrain.world.odorworld.sensors.Sensor;
import org.simbrain.world.odorworld.sensors.SmellSensor;
import org.simbrain.world.odorworld.sensors.TileSensor;

public class ProtoWorldComponent extends WorkspaceComponent {
    
    private ProtoWorld world;
    
    /**
     * Default constructor used when a new proto world is initialized.
     * 
     * @param name
     */
    public ProtoWorldComponent(String name) {
        super(name);
        addAttributeTypes();
        world = new ProtoWorld();
        world.startCanvas();
    }
    
    /**
     * Constructor used during deserialization.
     * 
     * @param name
     * @param world The fully deserialized proto world.
     */
    public ProtoWorldComponent(String name, ProtoWorld world) {
        super(name);
        addAttributeTypes();
        this.world = world;
    }
    
    private void addAttributeTypes() {
        addProducerType(new AttributeType(this, "Location", "getLocationX", double.class, true));
        addProducerType(new AttributeType(this, "Location", "getLocationY", double.class, true));
        addProducerType(new AttributeType(this, "Location", "getLocationZ", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationW", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationX", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationY", double.class, true));
        addProducerType(new AttributeType(this, "Orientation", "getOrientationZ", double.class, true));
        
        addConsumerType(new AttributeType(this, "Location", "setLocationX", double.class, true));
        addConsumerType(new AttributeType(this, "Location", "setLocationY", double.class, true));
        addConsumerType(new AttributeType(this, "Location", "setLocationZ", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationW", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationX", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationY", double.class, true));
        addConsumerType(new AttributeType(this, "Orientation", "setOrientationZ", double.class, true));
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> potentialConsumers = new ArrayList<PotentialConsumer>();
        for (ProtoEntity entity : world.getEntities()) {
            for (AttributeType type : getVisibleConsumerTypes()) {
                PotentialConsumer consumer = getAttributeManager().createPotentialConsumer(entity, type);
                potentialConsumers.add(consumer);
            }
        }
        return potentialConsumers;
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> potentialProducers = new ArrayList<PotentialProducer>();
        for (ProtoEntity entity : world.getEntities()) {
            for (AttributeType type : getVisibleProducerTypes()) {
                PotentialProducer producer = getAttributeManager().createPotentialProducer(entity, type);
                potentialProducers.add(producer);
            }
        }
        return potentialProducers;
    }
    
    public ProtoWorld getWorld() {
        return world;
    }
    
    public Canvas getCanvas() {
        return world.getCanvas();
    }
    
    @Override
    public void save(OutputStream output, String format) {}
    
    @Override
    protected void closing() {
        world.stop();
    }
    
    @Override
    public void update() {
        world.simpleUpdate(0f);
    }
}
