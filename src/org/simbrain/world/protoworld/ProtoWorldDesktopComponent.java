package org.simbrain.world.protoworld;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.workspace.gui.GuiComponent;

public class ProtoWorldDesktopComponent extends GuiComponent<ProtoWorldComponent> {
	
	private JPanel worldPanel;
	private JPanel propertyPanel;
	private JMenuBar menu;
	
	public ProtoWorldDesktopComponent(GenericFrame frame, ProtoWorldComponent worldComponent) {
		super(frame, worldComponent);
		setLayout(new BorderLayout());
        worldPanel = new JPanel();
        worldPanel.setPreferredSize(new Dimension(400, 400));
        worldPanel.setSize(new Dimension(400, 400));
        worldPanel.add(worldComponent.getCanvas());
        add("Center", worldPanel);
        propertyPanel = new JPanel();
        propertyPanel.setPreferredSize(new Dimension(200, 400));
        propertyPanel.setSize(new Dimension(200, 400));
        add("East", propertyPanel);
        menu = new JMenuBar();
        menu.add(new JMenu("File"));
        menu.add(new JMenu("Edit"));
        menu.add(new JMenu("Help"));
        getParentFrame().setJMenuBar(menu);
        //worldPanel.setPreferredSize(new Dimension(worldPanel.getWorld().getWidth(),
        //		worldPanel.getWorld().getHeight()));
        //worldPanel.setSize(new Dimension(worldPanel.getWorld().getWidth(),
        //		worldPanel.getWorld().getHeight()));
        getParentFrame().pack();
        
        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent arg0) {}
            public void componentMoved(ComponentEvent arg0) {}
            public void componentResized(ComponentEvent arg0) {
                //worldPanel.getWorld().setWidth(worldPanel.getWidth(), false);
                //worldPanel.getWorld().setHeight(worldPanel.getHeight(), false);
            }
            public void componentShown(ComponentEvent arg0) {}
        });
	}
	
	@Override
	protected void closing() {}
	
}
