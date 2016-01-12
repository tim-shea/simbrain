package org.simbrain.world.threedworld;

import javax.swing.JPopupMenu;

import org.simbrain.world.threedworld.actions.ActionManager;

import com.jme3.math.Vector2f;

public class ContextMenu {
	private JPopupMenu popupMenu;
    
    public ContextMenu(ActionManager actionManager) {
        popupMenu = new JPopupMenu();
        popupMenu.add(actionManager.getAddEntity());
        popupMenu.add(actionManager.getAddAgent());
        popupMenu.add(actionManager.getAddWall());
        popupMenu.addSeparator();
        popupMenu.add(actionManager.getControlAgent());
        popupMenu.add(actionManager.getReleaseAgent());
        popupMenu.add(actionManager.getEditEntity());
        popupMenu.add(actionManager.getDeleteEntity());
        popupMenu.addSeparator();
        popupMenu.add(actionManager.getEditFloorTexture());
        popupMenu.add(actionManager.getEditWorldPreferences());
    }
    
    public void show(ThreeDEngine engine) {
        Vector2f cursorPosition = engine.getInputManager().getCursorPosition();
	    int x = (int)cursorPosition.x;
	    int y = (int)cursorPosition.y;
	    popupMenu.show(engine.getPanel(), x, y);
    }
    
    public void hide() {
        popupMenu.setVisible(false);
    }
}