package org.simbrain.world.threedworld.controllers;

import java.util.ArrayList;
import java.util.List;

import org.simbrain.world.threedworld.ContextMenu;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.actions.EntityEditor;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireBox;

import static org.simbrain.world.threedworld.controllers.SelectionController.Mapping.*;

public class SelectionController implements ActionListener, AnalogListener {
    enum Mapping {
        Select,
        Context,
        Scroll,
        Transform,
        Append,
        MoveCursor;
        
        public boolean isName(String name) {
            return name.equals(toString()); 
        }
    }
    
    public static long DOUBLE_CLICK_MSEC = 400;
    public static float MINIMUM_DRAG_LENGTH = 5;
    
    public static float snapToGrid(float value, float gridSize) {
        return Math.round(value / gridSize) * gridSize;
    }
    
    public static Vector3f snapToGrid(Vector3f value, float gridSize) {
        return new Vector3f(
                snapToGrid(value.x, gridSize),
                snapToGrid(value.y, gridSize),
                snapToGrid(value.z, gridSize));
    }
    
    private ThreeDWorld world;
    private List<Entity> selection;
    private boolean enabled = false;
    private boolean appendSelection = false;
    private boolean transformActive = false;
    private boolean translateActive = false;
    private boolean rotateActive = false;
    private boolean scaleActive = false;
    private boolean snapTransformations = true;
    private long selectReleaseTime = 0;
    private Vector3f rotationAxis = Vector3f.UNIT_Y;
    private float scaleDivisor = 1;
    private EntityEditor entityEditor;
    private ContextMenu contextMenu;
    
    public SelectionController(ThreeDWorld world) {
        this.world = world;
        selection = new ArrayList<Entity>();
        entityEditor = new EntityEditor();
        contextMenu = new ContextMenu(world);
    }
    
    public void registerInput() {
        InputManager input = world.getEngine().getInputManager();
        input.addMapping(Select.toString(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        input.addMapping(Context.toString(), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        input.addMapping(Scroll.toString(), new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        input.addMapping(Transform.toString(), new KeyTrigger(KeyInput.KEY_LCONTROL));
        input.addMapping(Append.toString(), new KeyTrigger(KeyInput.KEY_LSHIFT));
        input.addMapping(MoveCursor.toString(),
                new MouseAxisTrigger(MouseInput.AXIS_X, false),
                new MouseAxisTrigger(MouseInput.AXIS_X, true),
                new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        input.addListener(this, Append.toString());
        input.addListener(this, Transform.toString());
        input.addListener(this, Select.toString());
        input.addListener(this, Context.toString());
        input.addListener(this, Scroll.toString());
        input.addListener(this, MoveCursor.toString());
    }
    
    public void unregisterInput() {
        InputManager input = world.getEngine().getInputManager();
        if (input == null)
            return;
        for (Mapping mapping : Mapping.values()) {
            if (input.hasMapping(mapping.toString())) {
                input.deleteMapping(mapping.toString());
            }
        }
        input.removeListener(this);
    }
    
    public ThreeDWorld getWorld() {
        return world;
    }
    
    public boolean hasSelection() {
        return !selection.isEmpty();
    }
    
    public List<Entity> getSelection() {
        return selection;
    }
    
    public void clearSelection() {
        for (Entity entity : selection) {
            entity.getNode().detachChildNamed("SelectionBox");
        }
        selection.clear();
        world.getAction("Edit Entity").setEnabled(false);
        world.getAction("Delete Entity").setEnabled(false);
    }
    
    public void select(Entity entity) {
        if (!appendSelection)
            clearSelection();
        if (entity != null) {
            addEntityToSelection(entity);
            world.getAction("Edit Entity").setEnabled(true);
            world.getAction("Delete Entity").setEnabled(true);
        }
    }
    
    public void addEntityToSelection(Entity entity) {
        selection.add(entity);
        // TODO: This should be the geometry model bound and the box is centered incorrectly
        BoundingBox bounds = (BoundingBox)entity.getNode().getWorldBound();
        WireBox selectionWire = new WireBox();
        selectionWire.fromBoundingBox(bounds);
        selectionWire.setLineWidth(2);
        Geometry selectionBox = new Geometry("SelectionBox", selectionWire);
        Vector3f boundsOffset = bounds.getCenter().subtract(entity.getNode().getLocalTranslation());
        selectionBox.setLocalTranslation(boundsOffset);
        selectionBox.setQueueBucket(Bucket.Transparent);
        Material selectionMaterial = new Material(world.getEngine().getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        selectionMaterial.setColor("Color", ColorRGBA.Green);
        selectionMaterial.getAdditionalRenderState().setDepthTest(false);
        selectionBox.setMaterial(selectionMaterial);
        entity.getNode().attachChild(selectionBox);
    }
    
    public Entity getSelectedEntity() {
        return selection.get(0);
    }
    
    public Vector3f getSelectionLocation() {
        if (hasSelection())
            return getSelectedEntity().getLocation();
        else
            return Vector3f.ZERO;
    }
    
    public Quaternion getSelectionOrientation() {
        if (hasSelection())
            return getSelectedEntity().getOrientation();
        else
            return Quaternion.IDENTITY;
    }
    
    public float getSelectionScale() {
        if (hasSelection())
            return getSelectedEntity().getScale();
        else
            return 1;
    }
    
    public BoundingVolume getSelectionBounds() {
        if (hasSelection()) {
            return getSelectedEntity().getNode().getWorldBound().clone();
        } else
            return null;
    }
    
    public void translateSelection(Vector3f location) {
        if (hasSelection())
            getSelectedEntity().setLocation(location);
    }
    
    public void rotateSelection(Quaternion orientation) {
        if (hasSelection())
            getSelectedEntity().setOrientation(orientation);
    }
    
    public void scaleSelection(float scalar) {
        if (hasSelection())
            getSelectedEntity().setScale(scalar);
    }
    
    public void editSelection() {
        if (hasSelection())
            entityEditor.showEditor(getSelectedEntity());
    }
    
    public void deleteSelection() {
        if (hasSelection()) {
            Entity entity = getSelectedEntity();
            world.getEngine().getRootNode().detachChild(entity.getNode());
            world.getEngine().getPhysicsSpace().removeAll(entity.getNode());
            world.getEntities().remove(entity);
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean value) {
        enabled = value;
    }
    
    public boolean isAppendSelection() {
        return appendSelection;
    }
    
    public void setAppendSelection(boolean value) {
        appendSelection = value;
    }
    
    public boolean isTransformActive() {
        return transformActive;
    }
    
    public void setTransformActive(boolean value) {
        transformActive = value;
        translateActive = translateActive && value;
        rotateActive = rotateActive && value;
        scaleActive = scaleActive && value;
    }
    
    public boolean isTranslateActive() {
        return translateActive;
    }
    
    public void setTranslateActive(boolean value) {
        translateActive = value;
    }
    
    public boolean isRotateActive() {
        return rotateActive;
    }
    
    public void setRotateActive(boolean value) {
        rotateActive = value;
    }
    
    public boolean isScaleActive() {
        return scaleActive;
    }
    
    public void setScaleActive(boolean value) {
        scaleActive = value;
    }
    
    public boolean getSnapTransformations() {
        return snapTransformations;
    }
    
    public void setSnapTransformations(boolean value) {
        snapTransformations = value;
    }
    
    public Vector3f getRotationAxis() {
        return rotationAxis;
    }
    
    public void setRotationAxis(Vector3f value) {
        rotationAxis = value;
    }
    
    @Override public void onAction(String name, boolean isPressed, float tpf) {
        if (!isEnabled())
            return;
        if (isPressed && (Transform.isName(name) || Append.isName(name) ||
            Select.isName(name) || Context.isName(name) || Scroll.isName(name)))
            contextMenu.hide();
        if (Transform.isName(name))
            setTransformActive(isPressed);
        else if (Append.isName(name))
            setAppendSelection(isPressed);
        else if (Select.isName(name))
            onSelectAction(isPressed);
        else if (Context.isName(name))
            onContextAction(isPressed);
        else if (Scroll.isName(name))
            onScrollAction(isPressed);
        if (isTransformActive() || isTranslateActive() || isRotateActive() || isScaleActive()) {
            if (world.getCameraController() != null)
                world.getCameraController().setMouseLookActive(false);
        }
    }
    
    private void onSelectAction(boolean isPressed) {
        if (isTransformActive()) {
            setTranslateActive(isPressed);
        } else {
            Entity entity = getCursorEntity();
            if (getSelection().contains(entity)) {
                if (!isPressed) {
                    long time = System.currentTimeMillis();
                    if (time - selectReleaseTime < DOUBLE_CLICK_MSEC)
                        entityEditor.showEditor(entity);
                    selectReleaseTime = System.currentTimeMillis();
                }
                setTranslateActive(isPressed);
            } else {
                select(entity);
            }
        }
    }
    
    private void onContextAction(boolean isPressed) {
        if (isTransformActive()) {
            setRotateActive(isPressed);
        } else if (!isPressed) {
            contextMenu.show(world.getEngine());
        }
    }
    
    private void onScrollAction(boolean isPressed) {
        if (isTransformActive()) {
            if (isPressed)
                updateScaleDivisor();
            setScaleActive(isPressed);
        }
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!enabled)
            return;
        if (MoveCursor.isName(name)) {
            if (translateActive) {
                translateToCursor();
            } else if (rotateActive)
                lookAtCursor();
            else if (scaleActive)
                scaleToCursor();
        }
    }
    
    public void translateToCursor() {
        if (!hasSelection())
            return;
        CollisionResult contact = getCursorContact(getSelectedEntity().getNode());
        if (contact != null) {
            Vector3f location = contact.getContactPoint();
            if (getSnapTransformations())
                location = snapToGrid(location, 1);
            float overlap = 1;
            BoundingVolume bounds = getSelectionBounds();
            CollisionResults results = new CollisionResults();
            new Ray(bounds.getCenter(), contact.getContactNormal()).collideWith(bounds, results);
            CollisionResult result = results.getCollision(1);
            if (result != null)
                overlap = result.getDistance();
            location.addLocal(contact.getContactNormal().mult(overlap));
            Vector3f boundsOffset = bounds.getCenter().subtract(getSelectionLocation());
            location.subtractLocal(boundsOffset);
            translateSelection(location);
        }
    }
    
    public void lookAtCursor() {
        if (!hasSelection())
            return;
        CollisionResult contact = getCursorContact(getSelectedEntity().getNode());
        if (contact != null) {
            // Get the offset to the cursor contact
            Vector3f target = contact.getContactPoint().subtract(
                    getSelectedEntity().getLocation());
            // Subtract the component of the target vector along the rotation axis
            target = target.subtract(rotationAxis.mult(target.dot(rotationAxis)));
            // Generate the orientation
            Quaternion rotation = getSelectedEntity().getOrientation();
            rotation.lookAt(target, rotationAxis);
            if (getSnapTransformations()) {
                float[] angles = rotation.toAngles(null);
                angles[0] = snapToGrid(angles[0], (float)Math.PI / 8);
                angles[1] = snapToGrid(angles[1], (float)Math.PI / 8);
                angles[2] = snapToGrid(angles[2], (float)Math.PI / 8);
                rotation.fromAngles(angles);
            }
            rotateSelection(rotation);
        }
    }
    
    public void scaleToCursor() {
        if (!hasSelection())
            return;
        CollisionResult contact = getCursorContact(null);
        if (contact != null) {
            float distanceToEntity = contact.getContactPoint().distance(getSelectedEntity().getLocation());
            float scalar = distanceToEntity / scaleDivisor;
            if (getSnapTransformations())
                scalar = snapToGrid(scalar, 0.1f);
            scaleSelection(scalar);
        }
    }
    
    private void updateScaleDivisor() {
        if (!hasSelection())
            return;
        CollisionResult contact = getCursorContact(null);
        if (contact != null) {
            float distance = contact.getContactPoint().distance(getSelectedEntity().getLocation());
            scaleDivisor = distance / getSelectedEntity().getScale();
        }
    }
    
    public Entity getCursorEntity() {
        CollisionResult contact = getCursorContact(null);
        Entity cursorEntity = null;
        if (contact != null) {
            String name = contact.getGeometry().getParent().getName();
            for (Entity entity : world.getEntities())
                if (name.contains(entity.getName()))
                    cursorEntity = entity;
        }
        return cursorEntity;
    }
    
    public CollisionResult getCursorContact(Node exclude) {
        ThreeDEngine engine = world.getEngine();
        Vector2f click2d = engine.getInputManager().getCursorPosition();
        Vector3f click3d = engine.getCamera().getWorldCoordinates(click2d, 0f);
        Vector3f direction = engine.getCamera().getWorldCoordinates(click2d, 1f);
        direction.subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, direction);
        CollisionResults results = new CollisionResults();
        engine.getRootNode().collideWith(ray, results);
        for (CollisionResult result : results) {
            if (exclude == null || !exclude.hasChild(result.getGeometry()))
                return result;
        }
        return null;
    }
}
