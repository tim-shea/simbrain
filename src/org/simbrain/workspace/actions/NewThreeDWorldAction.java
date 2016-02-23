package org.simbrain.workspace.actions;

import java.awt.event.ActionEvent;

import org.simbrain.resource.ResourceManager;
import org.simbrain.workspace.Workspace;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

/**
 * Add 3d world to workspace.
 */
public class NewThreeDWorldAction extends WorkspaceAction {
	private static final long serialVersionUID = 1L;
	
	public NewThreeDWorldAction(Workspace workspace) {
		super("3D World", workspace);
		putValue(SMALL_ICON, ResourceManager.getImageIcon("World.png"));
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		ThreeDWorldComponent worldComponent = new ThreeDWorldComponent("Proto World");
        workspace.addWorkspaceComponent(worldComponent);
	}
}
