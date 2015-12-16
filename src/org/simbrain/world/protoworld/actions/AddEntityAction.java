package org.simbrain.world.protoworld.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.simbrain.world.protoworld.ProtoWorld;

public class AddEntityAction implements ActionListener {
    private ProtoWorld world;
    
    public AddEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.addEntity();
    }
}
