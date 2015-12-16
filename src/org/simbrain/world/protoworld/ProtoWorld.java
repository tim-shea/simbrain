package org.simbrain.world.protoworld;

import java.awt.Canvas;

import org.simbrain.world.protoworld.actions.SelectEntityAction;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

public class ProtoWorld extends SimpleApplication {
    interface InputHandler {
        public void selectClick(float xGui, float yGui, Object target);
        public void contextClick(float xGui, float yGui, Object target);
    }
    
    private Canvas canvas;
    private InputHandler inputHandler;
    private boolean realTimeMode = true;
    private int realTimeFrameRate = 60;
    private double ambientLight = 0.3;
    private double directionalLight = 0.7;
    private int fieldOfView = 80;
    private double translateSpeed = 5.0;
    private double rotateSpeed = 10.0;
    private String selectedEntity = null;
    
    public ProtoWorld() {
        super();
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        
        setPauseOnLostFocus(false);
        setSettings(settings);
        createCanvas();
        
        JmeCanvasContext context = (JmeCanvasContext)getContext();
        canvas = context.getCanvas();
        getCanvas().setSize(settings.getWidth(), settings.getHeight());
    }
    
    @Override
    public void simpleInitApp() {
        getInputManager().addMapping("Select Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        getInputManager().addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                Vector2f cursorPosition = inputManager.getCursorPosition();
                inputHandler.selectClick(cursorPosition.x, getCanvas().getHeight() - cursorPosition.y, null);
            }
        }, "Select Click");
        
        getInputManager().addListener(new SelectEntityAction(this), "Select Click");
        
        getInputManager().addMapping("Context Click", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        getInputManager().addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    Vector2f cursorPosition = inputManager.getCursorPosition();
                    inputHandler.contextClick(cursorPosition.x, getCanvas().getHeight() - cursorPosition.y, null);
                }
            }
        }, "Context Click");
        
        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(5);
        flyCam.setRotationSpeed(5);
        getCamera().setLocation(new Vector3f(0, 2, 10));
        
        Grid grid = new Grid(20, 20, 1);
        Geometry geometry = new Geometry("Grid", grid);
        Material material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Green);
        geometry.setMaterial(material);
        geometry.setLocalTranslation(-10, 0, -10);
        getRootNode().attachChild(geometry);
        
        Box box = new Box(1, 1, 1);
        geometry = new Geometry("Box", box);
        material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Blue);
        geometry.setMaterial(material);
        geometry.setLocalTranslation(0, 1, 0);
        getRootNode().attachChild(geometry);
    }
    
    @Override
    public void simpleUpdate(float tpf) {}
    
    public Canvas getCanvas() {
        return canvas;
    }
    
    public void setInputHandler(InputHandler value) {
        inputHandler = value;
    }
    
    public boolean isRealTime() {
        return realTimeMode;
    }
    
    public void setRealTime(boolean value) {
        realTimeMode = value;
    }
    
    public int getRealTimeFrameRate() {
        return realTimeFrameRate;
    }
    
    public void setRealTimeFrameRate(int value) {
        realTimeFrameRate = value;
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
    
    public int getFieldOfView() {
        return fieldOfView;
    }
    
    public void setFieldOfView(int value) {
        fieldOfView = value;
    }
    
    public double getTranslateSpeed() {
        return translateSpeed;
    }
    
    public void setTranslateSpeed(double value) {
        translateSpeed = value;
    }
    
    public double getRotateSpeed() {
        return rotateSpeed;
    }
    
    public void setRotateSpeed(double value) {
        rotateSpeed = value;
    }
    
    public void addEntity() {
        Box box = new Box(1, 1, 1);
        Geometry geometry = new Geometry("Box", box);
        Material material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Blue);
        geometry.setMaterial(material);
        int x = (int)(Math.random() * 20) - 10;
        int z = (int)(Math.random() * 20) - 10;
        geometry.setLocalTranslation(x, 1, z);
        getRootNode().attachChild(geometry);
    }
    
    public void selectEntity(String name) {
        selectedEntity = name;
    }
    
    public boolean isSelected(String name) {
        return selectedEntity == name;
    }
}
