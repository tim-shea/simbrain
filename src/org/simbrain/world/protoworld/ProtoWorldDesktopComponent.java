package org.simbrain.world.protoworld;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.workspace.component_actions.CloseAction;
import org.simbrain.workspace.component_actions.OpenAction;
import org.simbrain.workspace.component_actions.SaveAction;
import org.simbrain.workspace.component_actions.SaveAsAction;
import org.simbrain.workspace.gui.GuiComponent;
import org.simbrain.world.protoworld.ProtoWorld.ControlMode;
import org.simbrain.world.protoworld.ProtoWorld.ProtoWorldListener;
import org.simbrain.world.protoworld.actions.AddEntityAction;
import org.simbrain.world.protoworld.actions.EditEntityAction;
import org.simbrain.world.protoworld.actions.RemoveEntityAction;
import org.simbrain.world.protoworld.actions.ShowProtoWorldSettingsAction;

import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;

public class ProtoWorldDesktopComponent extends GuiComponent<ProtoWorldComponent> {
    
	private class ShowContextMenuAction implements com.jme3.input.controls.ActionListener {
	    private ProtoWorld world;
	    
	    public ShowContextMenuAction(ProtoWorld world) {
	        this.world = world;
	    }
	    
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                if (!popupMenu.isVisible()) {
                    Vector2f cursorPosition = world.getInputManager().getCursorPosition();
                    popupMenu.show(ProtoWorldDesktopComponent.this, (int)cursorPosition.x,
                            (int)(world.getCanvas().getHeight() - cursorPosition.y));
                }
            }
        }
    }
	
    private JMenuBar menuBar;
	private JPopupMenu popupMenu;
	
	public ProtoWorldDesktopComponent(GenericFrame frame, ProtoWorldComponent workspaceComponent) {
		super(frame, workspaceComponent);
		setLayout(new BorderLayout());
		add(workspaceComponent.getCanvas());
		ProtoWorld world = workspaceComponent.getWorld();
        
        menuBar = new JMenuBar();
        
        JMenu worldMenu = new JMenu("World");
        worldMenu.add(new OpenAction(this));
        worldMenu.add(new SaveAction(this));
        worldMenu.add(new SaveAsAction(this));
        worldMenu.addSeparator();
        worldMenu.add(new ShowProtoWorldSettingsAction(workspaceComponent.getWorld()));
        worldMenu.add(new CloseAction(workspaceComponent));
        menuBar.add(worldMenu);
        
        JMenu entitiesMenu = new JMenu("Entities");
        
        JMenuItem addEntityItem = new JMenuItem("Add Entity");
        ActionListener addEntityAction = new AddEntityAction(world);
        addEntityItem.addActionListener(addEntityAction);
        entitiesMenu.add(addEntityItem);
        
        JMenuItem editEntityItem = new JMenuItem("Edit Entity");
        ActionListener editEntityAction = new EditEntityAction(world);
        editEntityItem.addActionListener(editEntityAction);
        entitiesMenu.add(editEntityItem);
        
        JMenuItem removeEntityItem = new JMenuItem("Remove Entity");
        ActionListener removeEntityAction = new RemoveEntityAction(world);
        removeEntityItem.addActionListener(removeEntityAction);
        entitiesMenu.add(removeEntityItem);
        
        menuBar.add(entitiesMenu);
        
        JMenu simulationMenu = new JMenu("Simulation");
        simulationMenu.add(new JMenuItem("Start"));
        simulationMenu.add(new JMenuItem("Stop"));
        menuBar.add(simulationMenu);
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        
        JToggleButton selectWidget = new JToggleButton();
        ImageIcon icon = ResourceManager.getImageIcon("Arrow.png");
        selectWidget.setIcon(icon);
        selectWidget.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(selectWidget);
        
        JToggleButton transformWidget = new JToggleButton();
        icon = ResourceManager.getImageIcon("ArrowMove.png");
        Image scaledImage = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        icon.setImage(scaledImage);
        transformWidget.setIcon(icon);
        transformWidget.setMargin(new Insets(0, 0, 0, 0));
        menuBar.add(transformWidget);
        
        JToggleButton snapWidget = new JToggleButton();
        icon = ResourceManager.getImageIcon("grid.png");
        snapWidget.setIcon(icon);
        snapWidget.setMargin(new Insets(0, 0, 0, 0));
        snapWidget.setSelected(true);
        menuBar.add(snapWidget);
        
        JToggleButton debugPhysicsWidget = new JToggleButton();
        icon = ResourceManager.getImageIcon("Prefs.png");
        debugPhysicsWidget.setIcon(icon);
        debugPhysicsWidget.setMargin(new Insets(0, 0, 0, 0));
        debugPhysicsWidget.setSelected(false);
        menuBar.add(debugPhysicsWidget);
        
        selectWidget.addActionListener((event) -> {
            if (selectWidget.isSelected()) {
                world.setControlMode(ControlMode.Select);
                transformWidget.setSelected(false);
                snapWidget.setEnabled(false);
            } else {
                world.setControlMode(ControlMode.None);
                snapWidget.setEnabled(false);
            }
        });
        
        transformWidget.addActionListener((event) -> {
            if (transformWidget.isSelected()) {
                world.setControlMode(ControlMode.Transform);
                selectWidget.setSelected(false);
                snapWidget.setEnabled(true);
            } else {
                world.setControlMode(ControlMode.None);
                snapWidget.setEnabled(false);
            }
        });
        
        snapWidget.addActionListener((event) -> {
            world.setSnapped(snapWidget.isSelected()); 
        });
        
        debugPhysicsWidget.addActionListener((event) -> {
            world.setDebugPhysics(debugPhysicsWidget.isSelected());
        });
        
        getParentFrame().setJMenuBar(menuBar);
        
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent arg0) {}
            public void componentMoved(ComponentEvent arg0) {}
            public void componentResized(ComponentEvent arg0) {}
            public void componentShown(ComponentEvent arg0) {}
        });
        
        popupMenu = new JPopupMenu();
        JMenuItem fillerItem = new JMenuItem("Filler");
        popupMenu.add(fillerItem);
        popupMenu.setInvoker(this);
        
        workspaceComponent.getWorld().addListener(new ProtoWorldListener() {
            public void onInit(ProtoWorld world) {
                ShowContextMenuAction action = new ShowContextMenuAction(world);
                world.getInputManager().addListener(action, "Context Click");
                selectWidget.doClick(1);
                world.enqueue(() -> {
                    world.removeListener(this);
                    return null;
                });
            }
            public void onUpdate(ProtoWorld world, float tpf) {}
        });
	}
	
	@Override
	protected void closing() {}
	
}
