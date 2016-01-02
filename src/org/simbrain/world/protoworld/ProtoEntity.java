package org.simbrain.world.protoworld;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
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

public class ProtoEntity {
    public static ProtoEntity createBox(ProtoWorld world, String name, AssetManager assetManager, PhysicsSpace space, float x, float y, float z) {
        Box box = new Box(x, y, z);
        TangentBinormalGenerator.generate(box);
        Geometry geometry = new Geometry(name, box);
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
        
        Node node = new Node();
        node.attachChild(geometry);
        node.addControl(body);
        
        return new ProtoEntity(world, node, geometry, body);
    }
    
    /*public static ProtoEntity createSphere(ProtoWorld world, String name, AssetManager assetManager, PhysicsSpace space) {
        Sphere sphere = new Sphere(16, 16, 0.25f);
        TangentBinormalGenerator.generate(sphere);
        Geometry geometry = new Geometry(name, sphere);
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setBoolean("HighQuality", true);
        material.setColor("Ambient", new ColorRGBA(1, 1, 0.8f, 1));
        material.setColor("Diffuse", new ColorRGBA(1, 1, 0.9f, 0.4f));
        material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geometry.setMaterial(material);
        geometry.setQueueBucket(Bucket.Transparent);
        
        CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(geometry);
        RigidBodyControl body = new RigidBodyControl(shape, 0);
        geometry.addControl(body);
        space.add(body);
        
        return new ProtoEntity(world, geometry, body);
    }*/
    
    private ProtoWorld world;
    private Node node;
    private Geometry geometry;
    private RigidBodyControl body;
    
    public ProtoEntity(ProtoWorld world, Node node, Geometry geometry, RigidBodyControl body) {
        this.world = world;
        this.node = node;
        this.geometry = geometry;
        this.body = body;
    }
    
    public ProtoWorld getWorld() {
        return world;
    }
    
    public Node getNode() {
        return node;
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
    
    public RigidBodyControl getBody() {
        return body;
    }
    
    public String getName() {
        return geometry.getName();
    }
    
    public void setName(String value) {
        geometry.setName(value);
    }
    
    public double getLocationX() {
        return body.getPhysicsLocation().x;
    }
    
    public void setLocationX(double value) {
        Vector3f location = body.getPhysicsLocation();
        location.x = (float)value;
        setLocation(location);
    }
    
    public double getLocationY() {
        return body.getPhysicsLocation().y;
    }
    
    public void setLocationY(double value) {
        Vector3f location = body.getPhysicsLocation();
        location.y = (float)value;
        setLocation(location);
    }
    
    public double getLocationZ() {
        return body.getPhysicsLocation().z;
    }
    
    public void setLocationZ(double value) {
        Vector3f location = body.getPhysicsLocation();
        location.z = (float)value;
        setLocation(location);
    }
    
    public Vector3f getLocation() {
        return body.getPhysicsLocation();
    }
    
    public void setLocation(float x, float y, float z) {
        setLocation(new Vector3f(x, y, z));
    }
    
    public void setLocation(final Vector3f location) {
        world.enqueue(() -> {
            body.setPhysicsLocation(location);
            body.activate();
            return null;
        });
    }
    
    public double getOrientationW() {
        return body.getPhysicsRotation().getW();
    }
    
    public void setOrientationW(double value) {
        Quaternion orientation = body.getPhysicsRotation();
        orientation.set(orientation.getW(), (float)value, orientation.getY(), orientation.getZ());
        setOrientation(orientation);
    }
    
    public double getOrientationX() {
        return body.getPhysicsRotation().getX();
    }
    
    public void setOrientationX(double value) {
        Quaternion orientation = body.getPhysicsRotation();
        orientation.set((float)value, orientation.getX(), orientation.getY(), orientation.getZ());
        setOrientation(orientation);
    }
    
    public double getOrientationY() {
        return body.getPhysicsRotation().getY();
    }
    
    public void setOrientationY(double value) {
        Quaternion orientation = body.getPhysicsRotation();
        orientation.set(orientation.getW(), orientation.getX(), (float)value, orientation.getZ());
        setOrientation(orientation);
    }
    
    public double getOrientationZ() {
        return body.getPhysicsRotation().getZ();
    }
    
    public void setOrientationZ(double value) {
        Quaternion orientation = body.getPhysicsRotation();
        orientation.set(orientation.getW(), orientation.getX(), orientation.getY(), (float)value);
        setOrientation(orientation);
    }
    
    public Quaternion getOrientation() {
        return body.getPhysicsRotation();
    }
    
    public void setOrientation(float w, float x, float y, float z) {
        setOrientation(new Quaternion(x, y, z, w));
    }
    
    public void setOrientation(final Quaternion orientation) {
        world.enqueue(() -> {
            body.setPhysicsRotation(orientation);
            body.activate();
            return null;
        });
    }
    
    public double getScale() {
        return geometry.getLocalScale().x;
    }
    
    public void setScale(final double scalar) {
        world.enqueue(() -> {
            geometry.setLocalScale((float)scalar);
            CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(geometry);
            body.setCollisionShape(shape);
            body.activate();
            return null;
        });
    }
}
