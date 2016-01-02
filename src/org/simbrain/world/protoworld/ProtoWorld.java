package org.simbrain.world.protoworld;

import java.awt.Canvas;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simbrain.world.protoworld.actions.RotateEntityWidget;
import org.simbrain.world.protoworld.actions.ScaleEntityWidget;
import org.simbrain.world.protoworld.actions.SelectEntityWidget;
import org.simbrain.world.protoworld.actions.TranslateEntityWidget;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.sun.naming.internal.ResourceManager;

public class ProtoWorld extends SimpleApplication {
    public interface ProtoWorldListener {
        void onInit(ProtoWorld world);
        void onUpdate(ProtoWorld world, float tpf);
    }
    
    enum ControlMode {
        None,
        Select,
        Transform
    }
    
    private Canvas canvas;
    private BulletAppState bulletAppState;
    private int width = 640;
    private int height = 480;
    private boolean realTimeMode = true;
    private int frameRate = 30;
    private double ambientLight = 0.3;
    private double directionalLight = 0.7;
    private float fieldOfView = 80;
    private float aspectRatio = 4f / 3;
    private float nearClip = 0.1f;
    private float farClip = 100f;
    private float translateSpeed = 5f;
    private float rotateSpeed = 10f;
    private List<ProtoEntity> entities = new ArrayList<ProtoEntity>();
    private List<ProtoWorldListener> listeners = new ArrayList<ProtoWorldListener>();
    private SelectEntityWidget selectEntityWidget;
    private TranslateEntityWidget translateEntityWidget;
    private RotateEntityWidget rotateEntityWidget;
    private ScaleEntityWidget scaleEntityWidget;
    private ControlMode controlMode;
    
    public ProtoWorld() {
        super();
        applyEngineSettings();
        createCanvas();
        
        JmeCanvasContext context = (JmeCanvasContext)getContext();
        canvas = context.getCanvas();
        getCanvas().setSize(settings.getWidth(), settings.getHeight());
    }
    
    @Override
    public void simpleInitApp() {
        getAssetManager().registerLocator("bin/org/simbrain/assets/", FileLocator.class);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        Node starterWorld = (Node)getAssetManager().loadModel("Scenes/StarterWorld.j3o");
        bulletAppState.getPhysicsSpace().addAll(starterWorld);
        getRootNode().attachChild(starterWorld);
        
        getInputManager().addMapping("Select Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        getInputManager().addMapping("Context Click", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        getInputManager().addMapping("Scroll Click", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        
        selectEntityWidget = new SelectEntityWidget(this);
        translateEntityWidget = new TranslateEntityWidget(this);
        rotateEntityWidget = new RotateEntityWidget(this);
        scaleEntityWidget = new ScaleEntityWidget(this);
        getInputManager().addListener(selectEntityWidget, "Context Click");
        getInputManager().addListener(translateEntityWidget, "Select Click");
        getInputManager().addListener(rotateEntityWidget, "Context Click");
        getInputManager().addListener(scaleEntityWidget, "Scroll Click");
        
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.Gray);
        getRootNode().addLight(ambient);
        
        DirectionalLight directional = new DirectionalLight();
        directional.setColor(ColorRGBA.Gray);
        directional.setDirection(new Vector3f(1, -1, 1));
        getRootNode().addLight(directional);
        
        applyCameraSettings();
        flyCam.setDragToRotate(true);
        getCamera().setLocation(new Vector3f(7, 7, 7));
        getCamera().setRotation(new Quaternion().fromAngles((float)(Math.PI / 4),
                (float)(-3 * Math.PI / 4), 0));
        
        for (ProtoWorldListener listener : listeners)
            listener.onInit(this);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        translateEntityWidget.update(tpf);
        rotateEntityWidget.update(tpf);
        scaleEntityWidget.update(tpf);
        for (ProtoWorldListener listener : listeners)
            listener.onUpdate(this, tpf);
    }
    
    public void addListener(ProtoWorldListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ProtoWorldListener listener) {
        listeners.remove(listener);
    }
    
    public void applyEngineSettings() {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFrameRate(frameRate);
        setPauseOnLostFocus(false);
        setSettings(settings);
    }
    
    public void applyCameraSettings() {
        getCamera().setFrustumPerspective(fieldOfView, aspectRatio, nearClip, farClip);
        flyCam.setMoveSpeed(translateSpeed);
        flyCam.setRotationSpeed(rotateSpeed);
    }
    
    public void setControlMode(ControlMode value) {
        controlMode = value;
        if (controlMode == ControlMode.None) {
            flyCam.setRotationSpeed(0);
            selectEntityWidget.setEnabled(false);
            translateEntityWidget.setEnabled(false);
            rotateEntityWidget.setEnabled(false);
            scaleEntityWidget.setEnabled(false);
        } else if (controlMode == ControlMode.Select) {
            flyCam.setRotationSpeed(rotateSpeed);
            selectEntityWidget.setEnabled(true);
            translateEntityWidget.setEnabled(false);
            rotateEntityWidget.setEnabled(false);
            scaleEntityWidget.setEnabled(false);
        } else if (controlMode == ControlMode.Transform) {
            flyCam.setRotationSpeed(0);
            selectEntityWidget.setEnabled(false);
            translateEntityWidget.setEnabled(true);
            rotateEntityWidget.setEnabled(true);
            scaleEntityWidget.setEnabled(true);
        }
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    public PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
    
    public boolean isDebugPhysics() {
        return bulletAppState.isDebugEnabled();
    }
    
    public void setDebugPhysics(boolean value) {
        bulletAppState.setDebugEnabled(value);
    }
    
    public List<ProtoEntity> getEntities() {
        return entities;
    }
    
    public ProtoEntity getSelectedEntity() {
        return selectEntityWidget.getSelectedEntity();
    }
    
    public void setSelectedEntity(ProtoEntity value) {
        selectEntityWidget.apply(0, value);
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int value) {
        width = value;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int value) {
        height = value;
    }
    
    public boolean isRealTime() {
        return realTimeMode;
    }
    
    public void setRealTime(boolean value) {
        realTimeMode = value;
    }
    
    public int getFrameRate() {
        return frameRate;
    }
    
    public void setFrameRate(int value) {
        frameRate = value;
    }
    
    public double getAmbientLight() {
        return ambientLight;
    }
    
    public void setAmbientLight(double value) {
        ambientLight = value;
    }
    
    public double getDirectionalLight() {
        return directionalLight;
    }
    
    public void setDirectionalLight(double value) {
        directionalLight = value;
    }
    
    public float getFieldOfView() {
        return fieldOfView;
    }
    
    public void setFieldOfView(float value) {
        fieldOfView = value;
    }
    
    public float getAspectRatio() {
        return aspectRatio;
    }
    
    public void setAspectRatio(float value) {
        aspectRatio = value;
    }
    
    public float getNearClip() {
        return nearClip;
    }
    
    public void setNearClip(float value) {
        nearClip = value;
    }
    
    public float getFarClip() {
        return farClip;
    }
    
    public void setFarClip(float value) {
        farClip = value;
    }
    
    public float getTranslateSpeed() {
        return translateSpeed;
    }
    
    public void setTranslateSpeed(float value) {
        translateSpeed = value;
    }
    
    public float getRotateSpeed() {
        return rotateSpeed;
    }
    
    public void setRotateSpeed(float value) {
        rotateSpeed = value;
    }
    
    public boolean isSnapped() {
        return translateEntityWidget.isSnapped() ||
                rotateEntityWidget.isSnapped() ||
                scaleEntityWidget.isSnapped();
    }
    
    public void setSnapped(boolean value) {
        translateEntityWidget.setSnapped(value);
        rotateEntityWidget.setSnapped(value);
        scaleEntityWidget.setSnapped(value);
    }
}
