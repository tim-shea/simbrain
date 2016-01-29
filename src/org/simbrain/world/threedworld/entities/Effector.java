package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialConsumer;

public interface Effector {
    void update(float tpf);
    
    void delete();
    
    List<PotentialConsumer> getPotentialConsumers();
    
    EffectorEditor getEditor();
}
