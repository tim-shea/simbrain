package org.simbrain.world.protoworld.actions;

import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.WireBox;

public class SelectEntityWidget extends CursorWidget {
    private ProtoEntity selectedEntity;
    private WireBox selectionWire;
    private Geometry selectionBox;
    
    public SelectEntityWidget(ProtoWorld world) {
        super(world);
        selectionWire = new WireBox();
        selectionWire.setLineWidth(2);
        selectionBox = new Geometry("SelectionBox", selectionWire);
        selectionBox.setQueueBucket(Bucket.Transparent);
        Material selectionMaterial = new Material(world.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        selectionMaterial.setColor("Color", ColorRGBA.Green);
        selectionMaterial.getAdditionalRenderState().setDepthTest(false);
        selectionBox.setMaterial(selectionMaterial);
    }
    
    public ProtoEntity getSelectedEntity() {
        return selectedEntity;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isEnabled() && !isPressed) {
            CollisionResult contact = getCursorContact(null);
            ProtoEntity selection = null;
            if (contact != null) {
                for (ProtoEntity potentialSelection : getWorld().getEntities()) {
                    if (potentialSelection.getGeometry().equals(contact.getGeometry()))
                        selection = potentialSelection;
                }
            }
            apply(tpf, selection);
        }
    }
    
    @Override
    public void apply(float tpf, ProtoEntity entity) {
        if (entity != null) {
            selectedEntity = entity;
            System.out.println("Selected: " + selectedEntity.getName());
            selectionWire.fromBoundingBox((BoundingBox)selectedEntity.getGeometry().getModelBound());
            selectedEntity.getNode().attachChild(selectionBox);
        } else if (selectedEntity != null) {
            selectedEntity.getNode().detachChild(selectionBox);
            selectedEntity = null;
        }
    }
}