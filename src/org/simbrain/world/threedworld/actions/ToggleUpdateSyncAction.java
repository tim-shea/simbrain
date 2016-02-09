package org.simbrain.world.threedworld.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.ThreeDWorld;

public class ToggleUpdateSyncAction extends AbstractAction {
    private static final long serialVersionUID = 2065579357970661L;
    
    private ThreeDWorld world;
    
    public ToggleUpdateSyncAction(ThreeDWorld world) {
        super("Toggle Update Sync");
        this.world = world;
        ImageIcon icon = ResourceManager.getImageIcon("Clock.png");
        icon.setImage(icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "Toggle Update Sync");
    }
    
    @Override public void actionPerformed(ActionEvent event) {
        final boolean sync = ((JToggleButton)event.getSource()).isSelected();
        world.getEngine().enqueue(() -> {
            world.getEngine().setUpdateSync(sync);
        });
    }
}
