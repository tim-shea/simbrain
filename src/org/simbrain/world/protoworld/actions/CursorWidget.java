package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public abstract class CursorWidget implements ActionListener {
    public static float snapToStep(float value, float stepSize) {
        return Math.round(value / stepSize) * stepSize;
    }
    
    public static Vector3f snapToGrid(Vector3f value, float gridSize) {
        return new Vector3f(
                snapToStep(value.x, gridSize),
                snapToStep(value.y, gridSize),
                snapToStep(value.z, gridSize));
    }
    
    private ProtoWorld world;
    private boolean enabled = false;
    private boolean active = false;
    private boolean snapped = true;
    
    public CursorWidget(ProtoWorld world) {
        this.world = world;
    }
    
    public ProtoWorld getWorld() {
        return world;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean value) {
        enabled = value;
        if (!enabled)
            setActive(false);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean value) {
        active = value;
    }
    
    public boolean isSnapped() {
        return snapped;
    }
    
    public void setSnapped(boolean value) {
        snapped = value;
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isEnabled() && isPressed)
            setActive(true);
        else
            setActive(false);
    }
    
    public void update(float tpf) {
        if (isEnabled() && isActive()) {
            ProtoEntity entity = world.getSelectedEntity();
            if (entity != null)
                apply(tpf, entity);
        }
    }
    
    public abstract void apply(float tpf, ProtoEntity entity);
    
    public CollisionResult getCursorContact(Node exclude) {
        Vector2f click2d = world.getInputManager().getCursorPosition();
        Vector3f click3d = world.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f direction = world.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, direction);
        CollisionResults results = new CollisionResults();
        world.getRootNode().collideWith(ray, results);
        for (CollisionResult result : results) {
            if (exclude == null || !exclude.hasChild(result.getGeometry()))
                return result;
        }
        return null;
    }
}