package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialProducer;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

/**
 * Sensor is an object which can sense some aspect of the ThreeDWorld for an Agent.
 * @author Tim Shea
 */
public interface Sensor {
    Agent getAgent();

    void update(float tpf);

    void delete();

    List<PotentialProducer> getPotentialProducers(ThreeDWorldComponent worldComponent);

    SensorEditor getEditor();
}
