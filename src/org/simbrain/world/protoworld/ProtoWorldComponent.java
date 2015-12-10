package org.simbrain.world.protoworld;

import java.io.OutputStream;

import org.simbrain.workspace.WorkspaceComponent;

public class ProtoWorldComponent extends WorkspaceComponent {
	private ProtoWorld world;
	
	/**
	 * Default constructor used when a new proto world is initialized.
	 * @param name
	 */
	public ProtoWorldComponent(String name) {
		super(name);
		world = new ProtoWorld();
	}
	
	/**
	 * Constructor used during deserialization.
	 * @param name
	 * @param world The fully deserialized proto world.
	 */
	public ProtoWorldComponent(String name, ProtoWorld world) {
		super(name);
		this.world = world;
	}
	
	public ProtoWorld getWorld() { return world; }
	
	@Override
	public void save(OutputStream output, String format) {}
	
	@Override
	protected void closing() {}
	
	@Override
    public void update() {
        world.update(this.getWorkspace().getTime());
    }
	
}
