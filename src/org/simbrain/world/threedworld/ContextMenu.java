package org.simbrain.world.threedworld;

import java.awt.Point;

import javax.swing.JPopupMenu;

import org.simbrain.world.threedworld.engine.ThreeDEngine;

public class ContextMenu {
	private JPopupMenu popupMenu;
    
    public ContextMenu(ThreeDWorld world) {
        popupMenu = new JPopupMenu();
        popupMenu.add(world.getAction("Add Entity"));
        popupMenu.add(world.getAction("Add Block"));
        popupMenu.add(world.getAction("Add Agent"));
        popupMenu.add(world.getAction("Add Mouse"));
        popupMenu.addSeparator();
        popupMenu.add(world.getAction("Copy Selection"));
        popupMenu.add(world.getAction("Paste Selection"));
        popupMenu.add(world.getAction("Delete Selection"));
        popupMenu.add(world.getAction("Edit Entity"));
        popupMenu.add(world.getAction("Control Agent"));
        popupMenu.add(world.getAction("Release Agent"));
        popupMenu.addSeparator();
        popupMenu.add(world.getAction("Load Scene"));
        popupMenu.add(world.getAction("Edit Camera Controller"));
    }
    
    public void show(ThreeDEngine engine) {
	    Point position = engine.getPanel().getMousePosition();
	    if (position == null)
	        position = new Point(0, 0);
	    popupMenu.show(engine.getPanel(), position.x, position.y);
    }
    
    public void hide() {
        popupMenu.setVisible(false);
    }
}
