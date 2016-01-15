package org.simbrain.world.threedworld.entities;

import org.simbrain.world.threedworld.engine.ThreeDEngine;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.util.TangentBinormalGenerator;

public class Entity {
    public static Entity createBox(ThreeDEngine engine, String name, float x, float y, float z) {
        Box box = new Box(x, y, z);
        TangentBinormalGenerator.generate(box);
        Geometry geometry = new Geometry(name + "Geometry", box);
        AssetManager assetManager = engine.getAssetManager();
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/seamless_tile_blue_d.png"));
        material.setTexture("NormalMap", assetManager.loadTexture("Textures/seamless_tile_n.png"));
        material.setBoolean("UseMaterialColors", true);
        material.setBoolean("HighQuality", true);
        material.setColor("Ambient", ColorRGBA.White);
        material.setColor("Diffuse", ColorRGBA.White);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 64f);
        geometry.setMaterial(material);
        
        CollisionShape shape = CollisionShapeFactory.createBoxShape(geometry);
        RigidBodyControl body = new RigidBodyControl(shape, 1);
        
        BoundingBox bound = new BoundingBox();
        geometry.setModelBound(bound);
        geometry.updateModelBound();
        
        Node node = new Node(name);
        Entity entity = new Entity(engine, node);
        node.attachChild(geometry);
        node.addControl(body);
        
        return entity;
    }
    
    public static Node loadModel(ThreeDEngine engine, String modelName, String fileName) {
        Node scene = (Node)engine.getAssetManager().loadModel(fileName);
        Node modelNode = (Node)scene.getChild(modelName);
        scene.detachChild(modelNode);
        modelNode.setModelBound(new BoundingBox());
        modelNode.updateModelBound();
        CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(modelNode);
        RigidBodyControl body = new RigidBodyControl(shape, 1);
        modelNode.addControl(body);
        return modelNode;
    }
    
    private ThreeDEngine engine;
    private String name;
    private Node node;
    
    public Entity() {}
    
    public Entity(ThreeDEngine engine, Node node) {
        this.engine = engine;
        this.name = node.getName();
        this.node = node;
    }
    
    public ThreeDEngine getEngine() {
        return engine;
    }
    
    public void setEngine(ThreeDEngine value) {
        engine = value;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String value) {
        name = value;
        if (node != null)
            node.setName(name);
    }
    
    public Node getNode() {
        return node;
    }
    
    public void setNode(Node value) {
        node = value;
        node.setName(name);
    }
    
    public RigidBodyControl getBody() {
        return node.getControl(RigidBodyControl.class);
    }
    
    public float getLocationX() {
        return getBody().getPhysicsLocation().x;
    }
    
    public void setLocationX(float value) {
        Vector3f location = getBody().getPhysicsLocation();
        location.x = value;
        setLocation(location);
    }
    
    public float getLocationY() {
        return getBody().getPhysicsLocation().y;
    }
    
    public void setLocationY(float value) {
        Vector3f location = getBody().getPhysicsLocation();
        location.y = value;
        setLocation(location);
    }
    
    public float getLocationZ() {
        return getBody().getPhysicsLocation().z;
    }
    
    public void setLocationZ(float value) {
        Vector3f location = getBody().getPhysicsLocation();
        location.z = value;
        setLocation(location);
    }
    
    public Vector3f getLocation() {
        return getNode().getWorldTranslation();
    }
    
    public void setLocation(float x, float y, float z) {
        setLocation(new Vector3f(x, y, z));
    }
    
    public void setLocation(final Vector3f location) {
        engine.enqueue(() -> {
            if (getBody().isKinematic())
                getNode().setLocalTranslation(location);
            else {
                getBody().setPhysicsLocation(location);
                getBody().activate();
            }
            return null;
        });
    }
    
    public float getOrientationW() {
        return getBody().getPhysicsRotation().getW();
    }
    
    public void setOrientationW(float value) {
        Quaternion orientation = getBody().getPhysicsRotation();
        orientation.set(orientation.getW(), value, orientation.getY(), orientation.getZ());
        setOrientation(orientation);
    }
    
    public float getOrientationX() {
        return getBody().getPhysicsRotation().getX();
    }
    
    public void setOrientationX(float value) {
        Quaternion orientation = getBody().getPhysicsRotation();
        orientation.set(value, orientation.getX(), orientation.getY(), orientation.getZ());
        setOrientation(orientation);
    }
    
    public float getOrientationY() {
        return getBody().getPhysicsRotation().getY();
    }
    
    public void setOrientationY(float value) {
        Quaternion orientation = getBody().getPhysicsRotation();
        orientation.set(orientation.getW(), orientation.getX(), value, orientation.getZ());
        setOrientation(orientation);
    }
    
    public float getOrientationZ() {
        return getBody().getPhysicsRotation().getZ();
    }
    
    public void setOrientationZ(float value) {
        Quaternion orientation = getBody().getPhysicsRotation();
        orientation.set(orientation.getW(), orientation.getX(), orientation.getY(), value);
        setOrientation(orientation);
    }
    
    public Quaternion getOrientation() {
        return getNode().getWorldRotation();
    }
    
    public void setOrientation(float w, float x, float y, float z) {
        setOrientation(new Quaternion(x, y, z, w));
    }
    
    public void setOrientation(final Quaternion orientation) {
        engine.enqueue(() -> {
            if (getBody().isKinematic()) {
                getNode().setLocalRotation(orientation);
            } else {
                getBody().setPhysicsRotation(orientation);
                getBody().activate();
            }
            return null;
        });
    }
    
    public float getScale() {
        return getNode().getWorldScale().x;
    }
    
    public void setScale(final float scalar) {
        engine.enqueue(() -> {
            node.setLocalScale(scalar);
            getBody().activate();
            return null;
        });
    }
}
