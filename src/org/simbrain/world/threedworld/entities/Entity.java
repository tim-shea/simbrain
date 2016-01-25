package org.simbrain.world.threedworld.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

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
    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getProducerType(component),
                EntityOrientationAdapter.getProducerType(component));
    }
    
    public static List<AttributeType> getConsumerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getConsumerType(component),
                EntityOrientationAdapter.getConsumerType(component));
    }
    
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
        engine.getRootNode().attachChild(node);
        engine.getPhysicsSpace().add(getBody());
    }
    
    public RigidBodyControl getBody() {
        return node.getControl(RigidBodyControl.class);
    }
    
    public Vector3f getLocation() {
        return getNode().getWorldTranslation();
    }
    
    public void setLocation(float x, float y, float z) {
        queueLocation(new Vector3f(x, y, z));
    }
    
    public void setLocation(Vector3f value) {
        if (getBody().isKinematic())
            getNode().setLocalTranslation(value);
        else {
            getBody().setPhysicsLocation(value);
            getBody().activate();
        }
        update(0);
    }
    
    public void queueLocation(Vector3f location) {
        engine.enqueue(() -> {
            setLocation(location);
            return null;
        });
    }
    
    public Quaternion getOrientation() {
        return getNode().getWorldRotation();
    }
    
    public void setOrientation(float w, float x, float y, float z) {
        setOrientation(new Quaternion(x, y, z, w));
    }
    
    public void setOrientation(Quaternion value) {
        if (getBody().isKinematic()) {
            getNode().setLocalRotation(value);
        } else {
            getBody().setPhysicsRotation(value);
            getBody().activate();
        }
        update(0);
    }
    
    public void queueOrientation(Quaternion orientation) {
        engine.enqueue(() -> {
            setOrientation(orientation);
            return null;
        });
    }
    
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = new ArrayList<PotentialProducer>();
        producers.addAll(new EntityLocationAdapter(this).getPotentialProducers());
        producers.addAll(new EntityOrientationAdapter(this).getPotentialProducers());
        return producers;
    }
    
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> consumers = new ArrayList<PotentialConsumer>();
        consumers.addAll(new EntityLocationAdapter(this).getPotentialConsumers());
        consumers.addAll(new EntityOrientationAdapter(this).getPotentialConsumers());
        return consumers;
    }
    
    public void update(float tpf) {}
    
    public Editor getEditor() {
        return new EntityEditor(this);
    }
}
