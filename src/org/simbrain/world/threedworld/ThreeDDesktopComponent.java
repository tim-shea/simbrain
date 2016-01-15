package org.simbrain.world.threedworld;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.util.widgets.ToggleButton;
import org.simbrain.workspace.component_actions.CloseAction;
import org.simbrain.workspace.component_actions.OpenAction;
import org.simbrain.workspace.component_actions.SaveAction;
import org.simbrain.workspace.component_actions.SaveAsAction;
import org.simbrain.workspace.gui.GuiComponent;

public class ThreeDDesktopComponent extends GuiComponent<ThreeDWorldComponent> {
    private static final long serialVersionUID = 8711925427252261845L;
    
	public ThreeDDesktopComponent(GenericFrame frame, ThreeDWorldComponent component) {
		super(frame, component);
		setLayout(new BorderLayout());
		add(component.getWorld().getEngine().getPanel());
		
        createMenus(component);
        createToolBars(component);
        
        // TODO: Figure out what to do here
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent arg0) {}
            public void componentMoved(ComponentEvent arg0) {}
            public void componentResized(ComponentEvent arg0) {}
            public void componentShown(ComponentEvent arg0) {}
        });
	}
	
	private void createMenus(ThreeDWorldComponent component) {
	    JMenuBar menuBar = new JMenuBar();
        
        JMenu worldMenu = new JMenu("World");
        worldMenu.add(new OpenAction(this));
        worldMenu.add(new SaveAction(this));
        worldMenu.add(new SaveAsAction(this));
        worldMenu.addSeparator();
        worldMenu.add(component.getWorld().getAction("Edit World Preferences"));
        worldMenu.add(new CloseAction(component));
        menuBar.add(worldMenu);
        
        JMenu entitiesMenu = new JMenu("Entities");
        entitiesMenu.add(component.getWorld().getAction("Add Entity"));
        entitiesMenu.add(component.getWorld().getAction("Add Agent"));
        entitiesMenu.add(component.getWorld().getAction("Add Wall"));
        entitiesMenu.addSeparator();
        entitiesMenu.add(component.getWorld().getAction("Control Agent"));
        entitiesMenu.add(component.getWorld().getAction("Release Agent"));
        entitiesMenu.add(component.getWorld().getAction("Edit Entity"));
        entitiesMenu.add(component.getWorld().getAction("Delete Entity"));
        menuBar.add(entitiesMenu);
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        getParentFrame().setJMenuBar(menuBar);
	}
	
	private void createToolBars(ThreeDWorldComponent component) {
	    JPanel toolPanel = new JPanel(new BorderLayout());
        JToolBar runToolbar = new JToolBar();
        runToolbar.add(component.getWorld().getAction("Update"));
        runToolbar.add(new ToggleButton(Arrays.asList(
                component.getWorld().getAction("Run"),
                component.getWorld().getAction("Pause"))));
        
        JToolBar editToolbar = new JToolBar();
        
        editToolbar.add(createToggleButton(component.getWorld().getAction("Control Agent")));
        editToolbar.add(component.getWorld().getAction("Camera Home"));
        editToolbar.add(createToggleButton(component.getWorld().getAction("Snap Transforms")));
        editToolbar.add(createToggleButton(component.getWorld().getAction("Debug Physics")));
        
        FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
        flow.setHgap(0);
        flow.setVgap(0);
        JPanel internalToolbar = new JPanel(flow);
        internalToolbar.add(runToolbar);
        internalToolbar.add(editToolbar);
        
        toolPanel.add("Center", internalToolbar);
        add("North", toolPanel);
	}
    
    public JToggleButton createToggleButton(AbstractAction action) {
        JToggleButton button = new JToggleButton(action);
        button.setHideActionText(true);
        button.setFocusPainted(false);
        return button;
    }
	
	@Override
	protected void closing() {}
	
}
