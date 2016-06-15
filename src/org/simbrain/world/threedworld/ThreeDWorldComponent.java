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
import org.simbrain.world.threedworld.engine.ThreeDEngineConverter;
import org.simbrain.world.threedworld.entities.Entity;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.BoxEntityXmlConverter;
import org.simbrain.world.threedworld.entities.CollisionSensor;
import org.simbrain.world.threedworld.entities.Effector;
import org.simbrain.world.threedworld.entities.ModelEntity;
import org.simbrain.world.threedworld.entities.ModelEntityXmlConverter;
import org.simbrain.world.threedworld.entities.Sensor;
import org.simbrain.world.threedworld.entities.VisionSensor;
import org.simbrain.world.threedworld.entities.WalkingEffector;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * ThreeDWorldComponent is a workspace component to extract some serialization and attribute
 * management from the ThreeDWorld.
 */
public class ThreeDWorldComponent extends WorkspaceComponent {
    /**
     * @return A newly constructed xstream for serializing a ThreeDWorld.
     */
    public static XStream getXStream() {
        XStream stream = new XStream(new DomDriver());
        stream.registerConverter(new ThreeDEngineConverter());
        stream.registerConverter(new BoxEntityXmlConverter());
        stream.registerConverter(new ModelEntityXmlConverter());
        return stream;
    }

    /**
     * Open a saved ThreeDWorldComponent from an XML input stream.
     * @param input The input stream to read.
     * @param name The name of the new world component.
     * @param format The format of the input stream. Should be xml.
     * @return A deserialized ThreeDWorldComponent with a valid ThreeDWorld.
     */
    public static ThreeDWorldComponent open(InputStream input, String name, String format) {
        ThreeDWorld world = (ThreeDWorld) getXStream().fromXML(input);
        return new ThreeDWorldComponent(name, world);
    }

    private ThreeDWorld world;

    /**
     * Construct a new ThreeDWorldComponent.
     * @param name The name of the new component.
     */
    public ThreeDWorldComponent(String name) {
        super(name);
        world = new ThreeDWorld();
        addAttributeTypes();
    }

    /**
     * Construct a ThreeDWorldComponent with an existing ThreeDWorld.
     * @param name The name of the new component.
     * @param world The world.
     */
    private ThreeDWorldComponent(String name, ThreeDWorld world) {
        super(name);
        this.world = world;
        addAttributeTypes();
    }

    /**
     * @return The ThreeDWorld for this workspace component.
     */
    public ThreeDWorld getWorld() {
        return world;
    }

    /**
     * Setup the attribute types provided by ThreeDWorld.
     */
    private void addAttributeTypes() {
        for (AttributeType type : Entity.getProducerTypes(this)) {
            addProducerType(type);
        }
        for (AttributeType type : ModelEntity.getProducerTypes(this)) {
            addProducerType(type);
        }
        for (AttributeType type : VisionSensor.getProducerTypes(this)) {
            addProducerType(type);
        }
        for (AttributeType type : CollisionSensor.getProducerTypes(this)) {
            addProducerType(type);
        }
        for (AttributeType type : Entity.getConsumerTypes(this)) {
            addConsumerType(type);
        }
        for (AttributeType type : ModelEntity.getConsumerTypes(this)) {
            addConsumerType(type);
        }
        for (AttributeType type : WalkingEffector.getConsumerTypes(this)) {
            addConsumerType(type);
        }
    }

    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> potentialConsumers = new ArrayList<PotentialConsumer>();
        for (Entity entity : world.getEntities()) {
            potentialConsumers.addAll(entity.getPotentialConsumers());
        }
        return potentialConsumers;
    }

    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> potentialProducers = new ArrayList<PotentialProducer>();
        for (Entity entity : world.getEntities()) {
            potentialProducers.addAll(entity.getPotentialProducers());
        }
        return potentialProducers;
    }

    @Override
    public void save(OutputStream output, String format) {
        ThreeDEngine.State previousState = world.getEngine().getState();
        world.getEngine().queueState(ThreeDEngine.State.SystemPause, true);
        getXStream().toXML(world, output);
        world.getEngine().queueState(previousState, false);
    }

    @Override
    public String getKeyFromObject(Object object) {
        if (object instanceof Entity) {
            return ((Entity) object).getName();
        } else if (object instanceof Sensor) {
            String agentName = ((Sensor) object).getAgent().getName();
            String sensorType = object.getClass().getSimpleName();
            return agentName + ":sensor:" + sensorType;
        } else if (object instanceof Effector) {
            String agentName = ((Effector) object).getAgent().getName();
            String effectorType = object.getClass().getSimpleName();
            return agentName + ":effector:" + effectorType;
        }
        return null;
    }

    @Override
    public Object getObjectFromKey(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }
        String[] parsedKey = objectKey.split(":");
        Entity entity = getWorld().getEntity(parsedKey[0]);
        if (parsedKey.length == 1) {
            return entity;
        } else if (entity instanceof Agent) {
            String objectType = parsedKey[1];
            Agent agent = (Agent) entity;
            if ("sensor".equalsIgnoreCase(objectType)) {
                String sensorType = parsedKey[2];
                return agent.getSensor(sensorType);
            } else if ("effector".equalsIgnoreCase(objectType)) {
                String effectorType = parsedKey[2];
                return agent.getEffector(effectorType);
            }
        }
        return null;
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
