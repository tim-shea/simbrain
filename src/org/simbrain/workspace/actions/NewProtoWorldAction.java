package org.simbrain.workspace.actions;

import java.awt.event.ActionEvent;

import org.simbrain.resource.ResourceManager;
import org.simbrain.workspace.Workspace;
import org.simbrain.world.protoworld.ProtoWorld;
import org.simbrain.world.protoworld.ProtoWorldComponent;

/**
 * Add proto world to workspace.
 */
public class NewProtoWorldAction extends WorkspaceAction {
	private static final long serialVersionUID = 1L;
	
	public NewProtoWorldAction(Workspace workspace) {
		super("Proto World", workspace);
		putValue(SMALL_ICON, ResourceManager.getImageIcon("SwissIcon.png"));
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		ProtoWorldComponent worldComponent = new ProtoWorldComponent("Proto World");
        workspace.addWorkspaceComponent(worldComponent);
        createDefaultWorld(worldComponent.getWorld());
	}
	
	public void createDefaultWorld(ProtoWorld world) {}
	
}
