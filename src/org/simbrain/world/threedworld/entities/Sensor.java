package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialProducer;

public interface Sensor {
    Agent getAgent();

    void update(float tpf);

    void delete();

    List<PotentialProducer> getPotentialProducers();

    SensorEditor getEditor();
}
