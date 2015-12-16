package org.simbrain.world.protoworld;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.workspace.component_actions.CloseAction;
import org.simbrain.workspace.component_actions.OpenAction;
import org.simbrain.workspace.component_actions.SaveAction;
import org.simbrain.workspace.component_actions.SaveAsAction;
import org.simbrain.workspace.gui.GuiComponent;
import org.simbrain.world.protoworld.ProtoWorld.InputHandler;
import org.simbrain.world.protoworld.actions.AddEntityAction;
import org.simbrain.world.protoworld.actions.EditEntityAction;
import org.simbrain.world.protoworld.actions.ShowProtoWorldPrefsAction;

public class ProtoWorldDesktopComponent extends GuiComponent<ProtoWorldComponent> {
	
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
        worldMenu.add(new ShowProtoWorldPrefsAction(workspaceComponent));
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
        
        entitiesMenu.add(new JMenuItem("Remove Entity"));
        
        menuBar.add(entitiesMenu);
        
        JMenu simulationMenu = new JMenu("Simulation");
        simulationMenu.add(new JMenuItem("Start"));
        simulationMenu.add(new JMenuItem("Stop"));
        menuBar.add(simulationMenu);
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        
        getParentFrame().setJMenuBar(menuBar);
        
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent arg0) {}
            public void componentMoved(ComponentEvent arg0) {}
            public void componentResized(ComponentEvent arg0) {}
            public void componentShown(ComponentEvent arg0) {}
        });
        
        popupMenu = new JPopupMenu();
        addEntityItem = new JMenuItem("Add Entity");
        addEntityItem.addActionListener(addEntityAction);
        popupMenu.add(addEntityItem);
        
        editEntityItem = new JMenuItem("Edit Entity");
        editEntityItem.addActionListener(editEntityAction);
        popupMenu.add(editEntityItem);
        
        popupMenu.add(new JMenuItem("Remove Entity"));
        popupMenu.setInvoker(this);
        
        workspaceComponent.getWorld().setInputHandler(new InputHandler() {
            @Override
            public void selectClick(float xGui, float yGui, Object target) {}
            
            @Override
            public void contextClick(float xGui, float yGui, Object target) {
                popupMenu.show(ProtoWorldDesktopComponent.this, (int)xGui, (int)yGui);
            }
        });
	}
	
	@Override
	protected void closing() {}
	
}
