package org.simbrain.world.threedworld.entities;

import java.util.List;

import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

public interface Effector {
    void update(float tpf);
    
    List<PotentialConsumer> getPotentialConsumers();
    
    Editor getEditor();
}
