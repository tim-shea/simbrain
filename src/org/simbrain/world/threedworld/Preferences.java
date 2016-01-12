package org.simbrain.world.threedworld;

import com.jme3.math.ColorRGBA;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Preferences {
    private String sceneName = "";
    private String guiName = "";
    private int width = 640;
    private int height = 480;
    private boolean realTimeMode = true;
    private int frameRate = 30;
    private int ambientLight = ColorRGBA.White.asIntRGBA();
    private int directionalLight = ColorRGBA.White.asIntRGBA();
    private float fieldOfView = 80;
    private float nearClip = 0.1f;
    private float farClip = 100f;
    private float moveSpeed = 5f;
    private float rotateSpeed = 10f;
    private float zoomSpeed = 1f;
    
    public String getSceneName() {
        return sceneName;
    }
    
    public void setSceneName(String value) {
        sceneName = value;
    }
    
    public String getGuiName() {
        return guiName;
    }
    
    public void setGuiName(String value) {
        guiName = value;
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
    
    public int getAmbientLight() {
        return ambientLight;
    }
    
    public void setAmbientLight(int value) {
        ambientLight = value;
    }
    
    public int getDirectionalLight() {
        return directionalLight;
    }
    
    public void setDirectionalLight(int value) {
        directionalLight = value;
    }
    
    public float getFieldOfView() {
        return fieldOfView;
    }
    
    public void setFieldOfView(float value) {
        fieldOfView = value;
    }
    
    public float getAspectRatio() {
        return (float)width / height;
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
    
    public float getMoveSpeed() {
        return moveSpeed;
    }
    
    public void setMoveSpeed(float value) {
        moveSpeed = value;
    }
    
    public float getRotateSpeed() {
        return rotateSpeed;
    }
    
    public void setRotateSpeed(float value) {
        rotateSpeed = value;
    }
    
    public float getZoomSpeed() {
        return zoomSpeed;
    }
    
    public void setZoomSpeed(float value) {
        zoomSpeed = value;
    }
    
    public void updateSettings(ThreeDWorld world) {
        width = world.getEngine().getPanel().getWidth();
        height = world.getEngine().getPanel().getHeight();
        ambientLight = world.getEngine().getAmbientLight().getColor().asIntRGBA();
        directionalLight = world.getEngine().getDirectionalLight().getColor().asIntRGBA();
    }
}
