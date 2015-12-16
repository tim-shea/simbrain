package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class SelectEntityAction implements ActionListener {
    
    private ProtoWorld world;
    
    public SelectEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        CollisionResults results = new CollisionResults();
        
        Vector2f click2d = world.getInputManager().getCursorPosition();
        Vector3f click3d = world.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f direction = world.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
        
        Ray ray = new Ray(click3d, direction);
        world.getRootNode().collideWith(ray, results);
        
        if (results.size() > 0) {
            Geometry target = results.getClosestCollision().getGeometry();
            if (!world.isSelected(target.getName()))
                world.selectEntity(target.getName());
        }
    }
}