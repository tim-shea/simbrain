package org.simbrain.world.threedworld.actions;

import org.simbrain.world.threedworld.Preferences;
import org.simbrain.world.threedworld.ThreeDEngine;
import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import static org.simbrain.world.threedworld.actions.CameraController.Mapping.*;

public class CameraController implements AnalogListener, ActionListener {
    enum Mapping {
        MouseLook,
        YawLeft,
        YawRight,
        PitchUp,
        PitchDown,
        ZoomIn,
        ZoomOut,
        MoveLeft,
        MoveRight,
        MoveForward,
        MoveBackward,
        MoveUp,
        MoveDown;
        
        public boolean isName(String name) {
            return name.equals(toString());
        }
    }
    
    private ThreeDWorld world;
    private Preferences preferences;
    private Camera camera;
    private boolean enabled = false;
    private boolean mouseLookActive = false;
    private Vector3f homeLocation = Vector3f.UNIT_Y.mult(2.5f);
    private float[] homeRotation = new float[] {0, 0, 0};
    private Vector3f yawAxis = Vector3f.UNIT_Y; 
    
    public CameraController(ThreeDWorld world) {
        this.world = world;
        preferences = world.getPreferences();
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public void setCamera(Camera value) {
        camera = value;
        updateCameraFrustum();
    }
    
    private void updateCameraFrustum() {
        camera.setFrustumPerspective(
                preferences.getFieldOfView(),
                preferences.getAspectRatio(),
                preferences.getNearClip(),
                preferences.getFarClip());
    }
    
    public Vector3f getYawAxis() {
        return yawAxis;
    }
    
    public void setYawAxis(Vector3f value) {
        yawAxis = value.clone();
    }
    
    public float getMoveSpeed() {
        return preferences.getMoveSpeed();
    }
    
    public void setMoveSpeed(float value) {
        preferences.setMoveSpeed(value);
    }
    
    public float getRotateSpeed() {
        return preferences.getRotateSpeed();
    }
    
    public void setRotateSpeed(float value) {
        preferences.setRotateSpeed(value);
    }
    
    public float getZoomSpeed() {
        return preferences.getZoomSpeed();
    }
    
    public void setZoomSpeed(float value) {
        preferences.setZoomSpeed(value);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean value) {
        enabled = value;
    }
    
    public boolean isMouseLookActive() {
        return mouseLookActive;
    }
    
    public void setMouseLookActive(boolean value) {
        mouseLookActive = value;
    }
    
    public Vector3f getHomeLocation() {
        return homeLocation;
    }
    
    public void setHomeLocation(Vector3f value) {
        homeLocation = value;
    }
    
    public float[] getHomeRotation() {
        return homeRotation;
    }
    
    public void setHomeRotation(float[] angles) {
        homeRotation = angles;
    }
    
    public void moveCameraHome() {
        getCamera().setLocation(homeLocation);
        getCamera().setRotation(new Quaternion().fromAngles(homeRotation));
    }
    
    public void registerInput() {
        InputManager input = world.getEngine().getInputManager();
        input.addMapping(MouseLook.toString(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        input.addMapping(YawLeft.toString(), new MouseAxisTrigger(MouseInput.AXIS_X, true));
        input.addMapping(YawRight.toString(), new MouseAxisTrigger(MouseInput.AXIS_X, false));
        input.addMapping(PitchUp.toString(), new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        input.addMapping(PitchDown.toString(), new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        input.addMapping(ZoomIn.toString(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        input.addMapping(ZoomOut.toString(), new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        input.addMapping(MoveLeft.toString(), new KeyTrigger(KeyInput.KEY_A));
        input.addMapping(MoveRight.toString(), new KeyTrigger(KeyInput.KEY_D));
        input.addMapping(MoveForward.toString(), new KeyTrigger(KeyInput.KEY_W));
        input.addMapping(MoveBackward.toString(), new KeyTrigger(KeyInput.KEY_S));
        input.addMapping(MoveUp.toString(), new KeyTrigger(KeyInput.KEY_Q));
        input.addMapping(MoveDown.toString(), new KeyTrigger(KeyInput.KEY_Z));
        for (Mapping mapping : Mapping.values())
            input.addListener(this, mapping.toString());
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
    
    protected void moveCamera(float value, Vector3f axis) {
        Vector3f velocity = axis;
        Vector3f location = camera.getLocation().clone();
        velocity.multLocal(value * getMoveSpeed());
        location.addLocal(velocity);
        camera.setLocation(location);
    }
    
    protected void rotateCamera(float value, Vector3f axis) {
        if (!mouseLookActive)
            return;
        Matrix3f matrix = new Matrix3f();
        matrix.fromAngleNormalAxis(getRotateSpeed() * value, axis);
        Vector3f up = camera.getUp();
        Vector3f left = camera.getLeft();
        Vector3f direction = camera.getDirection();
        matrix.mult(up, up);
        matrix.mult(left, left);
        matrix.mult(direction, direction);
        Quaternion orientation = new Quaternion();
        orientation.fromAxes(left, up, direction);
        orientation.normalizeLocal();
        camera.setAxes(orientation);
    }
    
    protected void zoomCamera(float value) {
        float fov = preferences.getFieldOfView() + value * getZoomSpeed();
        fov = FastMath.clamp(fov, 10, 100);
        preferences.setFieldOfView(fov);
        updateCameraFrustum();
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!enabled)
            return;
        if (YawLeft.isName(name)) {
            rotateCamera(value, yawAxis);
        } else if (YawRight.isName(name)) {
            rotateCamera(-value, yawAxis);
        } else if (PitchUp.isName(name)) {
            rotateCamera(-value, camera.getLeft());
        } else if (PitchDown.isName(name)) {
            rotateCamera(value, camera.getLeft());
        } else if (MoveForward.isName(name)) {
            moveCamera(value, camera.getDirection());
        } else if (MoveBackward.isName(name)) {
            moveCamera(-value, camera.getDirection());
        } else if (MoveLeft.isName(name)) {
            moveCamera(value, camera.getLeft());
        } else if (MoveRight.isName(name)) {
            moveCamera(-value, camera.getLeft());
        } else if (MoveUp.isName(name)) {
            moveCamera(value, camera.getUp());
        } else if (MoveDown.isName(name)) {
            moveCamera(-value, camera.getUp());
        } else if (ZoomIn.isName(name)) {
            zoomCamera(-value);
        } else if (ZoomOut.isName(name)) {
            zoomCamera(value);
        }
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!enabled)
            return;
        if (MouseLook.isName(name)) {
            mouseLookActive = isPressed;
        }
    }
}
