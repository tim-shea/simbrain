package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.List;

import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Agent implements Entity {
    private ModelEntity model;
    private List<Sensor> sensors = new ArrayList<Sensor>();
    private List<Effector> effectors = new ArrayList<Effector>();
    
    public Agent(ModelEntity model) {
        this.model = model;
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
    
    public List<Sensor> getSensors() {
        return sensors;
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
    
    public List<Effector> getEffectors() {
        return effectors;
    }
    
    public ThreeDEngine getEngine() {
        return model.getEngine();
    }
    
    public ModelEntity getModel() {
        return model;
    }
    
    public void setModel(ModelEntity value) {
        model = value;
        model.getBody().setKinematic(true);
    }
    
    @Override
    public String getName() {
        return model.getName();
    }
    
    @Override
    public void setName(String value) {
        model.setName(value);
    }
    
    @Override
    public Node getNode() {
        return model.getNode();
    }
    
    @Override
    public Vector3f getPosition() {
        return model.getPosition();
    }
    
    @Override
    public void setPosition(Vector3f value) {
        model.setPosition(value);
    }
    
    @Override
    public void queuePosition(Vector3f value) {
        model.queuePosition(value);
    }
    
    @Override
    public void move(Vector3f offset) {
        model.move(offset);
    }
    
    @Override
    public Quaternion getRotation() {
        return model.getRotation();
    }
    
    @Override
    public void setRotation(Quaternion value) {
        model.setRotation(value);
    }
    
    @Override
    public void queueRotation(Quaternion value) {
        model.queueRotation(value);
    }
    
    @Override
    public void rotate(Quaternion rotation) {
        model.rotate(rotation);
    }
    
    @Override
    public BoundingVolume getBounds() {
        return model.getBounds();
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = model.getPotentialProducers();
        for (Sensor sensor : sensors)
            producers.addAll(sensor.getPotentialProducers());
        return producers;
    }
    
    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> consumers = model.getPotentialConsumers();
        for (Effector effector : effectors)
            consumers.addAll(effector.getPotentialConsumers());
        return consumers;
    }
    
    @Override
    public void update(float t) {
        model.update(t);
        for (Sensor sensor : sensors)
            sensor.update(t);
        for (Effector effector : effectors)
            effector.update(t);
    }
    
    @Override
    public void delete() {
        model.delete();
    }
    
    @Override
    public Editor getEditor() {
        return new AgentEditor(this);
    }
}
