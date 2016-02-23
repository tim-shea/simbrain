package org.simbrain.world.threedworld;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerListModel;
import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.util.widgets.ToggleButton;
import org.simbrain.workspace.component_actions.CloseAction;
import org.simbrain.workspace.component_actions.OpenAction;
import org.simbrain.workspace.component_actions.SaveAction;
import org.simbrain.workspace.component_actions.SaveAsAction;
import org.simbrain.workspace.gui.GuiComponent;
import org.simbrain.world.threedworld.entities.EditorDialog;

public class ThreeDDesktopComponent extends GuiComponent<ThreeDWorldComponent> {
    private static final long serialVersionUID = 8711925427252261845L;
    
	public ThreeDDesktopComponent(GenericFrame frame, ThreeDWorldComponent component) {
		super(frame, component);
		setLayout(new BorderLayout());
		
        frame.setJMenuBar(createMenus(component));
        add(createToolBars(component), BorderLayout.NORTH);
        Component panel = component.getWorld().getEngine().getPanel();
        add(panel, BorderLayout.CENTER);
        frame.setBounds(100, 100, 100, 100);
        EditorDialog.setOwner(this);
	}
	
	private JMenuBar createMenus(ThreeDWorldComponent component) {
	    JMenuBar menuBar = new JMenuBar();
        
        JMenu worldMenu = new JMenu("World");
        worldMenu.add(new OpenAction(this));
        worldMenu.add(new SaveAction(this));
        worldMenu.add(new SaveAsAction(this));
        worldMenu.addSeparator();
        worldMenu.add(component.getWorld().getAction("Edit Camera Controller"));
        worldMenu.add(new CloseAction(component));
        menuBar.add(worldMenu);
        
        JMenu entitiesMenu = new JMenu("Entities");
        entitiesMenu.add(component.getWorld().getAction("Add Entity"));
        entitiesMenu.add(component.getWorld().getAction("Add Block"));
        entitiesMenu.add(component.getWorld().getAction("Add Agent"));
        entitiesMenu.add(component.getWorld().getAction("Add Mouse"));
        entitiesMenu.addSeparator();
        entitiesMenu.add(component.getWorld().getAction("Select All"));
        entitiesMenu.add(component.getWorld().getAction("Edit Entity"));
        entitiesMenu.add(component.getWorld().getAction("Copy Selection"));
        entitiesMenu.add(component.getWorld().getAction("Paste Selection"));
        entitiesMenu.add(component.getWorld().getAction("Delete Selection"));
        entitiesMenu.addSeparator();
        entitiesMenu.add(component.getWorld().getAction("Control Agent"));
        entitiesMenu.add(component.getWorld().getAction("Release Agent"));
        menuBar.add(entitiesMenu);
        
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        return menuBar;
	}
	
	private Component createToolBars(ThreeDWorldComponent component) {
	    JPanel toolPanel = new JPanel(new BorderLayout());
        JToolBar runToolbar = new JToolBar();
        runToolbar.add(createToggleButton(component.getWorld().getAction("Toggle Update Sync"), true));
        runToolbar.add(createToggleButton(component.getWorld().getAction("Toggle Run"), true));
        
        JToolBar editToolbar = new JToolBar();
        
        editToolbar.add(new ToggleButton(Arrays.asList(
                component.getWorld().getAction("Control Agent"),
                component.getWorld().getAction("Release Agent"))));
        editToolbar.add(component.getWorld().getAction("Camera Home"));
        editToolbar.add(createToggleButton(component.getWorld().getAction("Snap Transforms"), true));
        SpinnerListModel rotationAxisModel = new SpinnerListModel(Arrays.asList("X Axis", "Y Axis", "Z Axis", "Camera"));
        rotationAxisModel.setValue(component.getWorld().getSelectionController().getRotationAxis());
        JSpinner rotationAxisSpinner = new JSpinner(rotationAxisModel);
        ((JSpinner.DefaultEditor)rotationAxisSpinner.getEditor()).getTextField().setColumns(4);;
        rotationAxisSpinner.addChangeListener((event) -> {
            component.getWorld().getSelectionController().setRotationAxis((String)rotationAxisModel.getValue());
        });
        editToolbar.add(rotationAxisSpinner);
        
        FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
        flow.setHgap(0);
        flow.setVgap(0);
        JPanel internalToolbar = new JPanel(flow);
        internalToolbar.add(runToolbar);
        internalToolbar.add(editToolbar);
        
        toolPanel.add("Center", internalToolbar);
        return toolPanel;
	}
    
    public JToggleButton createToggleButton(AbstractAction action, boolean selected) {
        JToggleButton button = new JToggleButton(action);
        button.setHideActionText(true);
        button.setFocusPainted(false);
        button.setSelected(selected);
        return button;
    }
	
	@Override
	protected void closing() {}
	
}
