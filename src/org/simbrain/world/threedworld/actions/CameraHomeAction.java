package org.simbrain.world.threedworld.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;
import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.math.Vector3f;

public class CameraHomeAction extends AbstractAction {
    private ThreeDWorld world;
    
    public CameraHomeAction(ThreeDWorld world) {
        super("Camera Home");
        this.world = world;
        ImageIcon icon = ResourceManager.getImageIcon("home.png");
        icon.setImage(icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "Move Camera Home");
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            world.getCameraController().moveCameraHome();
            return null;
        });
    }
}
