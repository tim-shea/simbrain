package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialProducer;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

public interface Sensor {
    void update(float tpf);
    
    void delete();
    
    List<PotentialProducer> getPotentialProducers();
    
    SensorEditor getEditor();
}
