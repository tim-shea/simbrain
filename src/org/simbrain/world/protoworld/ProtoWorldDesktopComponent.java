package org.simbrain.world.protoworld;

import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.workspace.gui.GuiComponent;

public class ProtoWorldDesktopComponent extends GuiComponent<ProtoWorldComponent> {
	
	public ProtoWorldDesktopComponent(GenericFrame frame, ProtoWorldComponent workspaceComponent) {
		super(frame, workspaceComponent);
	}
	
	@Override
	protected void closing() {}
	
}
