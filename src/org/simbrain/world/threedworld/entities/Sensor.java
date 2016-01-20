package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialProducer;

public interface Sensor {
    void update(float tpf);
    
    List<PotentialProducer> getPotentialProducers();
}
