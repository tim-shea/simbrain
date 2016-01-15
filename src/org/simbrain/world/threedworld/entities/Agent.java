package org.simbrain.world.threedworld.entities;

import java.awt.Dimension;

import org.simbrain.world.threedworld.engine.ThreeDContext;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.engine.ThreeDPanel;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;

public class Agent extends Entity {
    public class VisionSensor {
        private Vector3f headOffset = Vector3f.UNIT_Z;
        private Camera camera;
        private ViewPort viewPort;
        private ThreeDPanel panel;
        
        public VisionSensor() {
            camera = new Camera(640, 480);
            camera.setFrustumPerspective(45f, (float)camera.getWidth() / camera.getHeight(), 1f, 1000f);
            camera.setLocation(new Vector3f(0f, 0f, 10f));
            camera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
            viewPort = getEngine().getRenderManager().createMainView(getName() + "ViewPort", camera);
            viewPort.setClearFlags(true, true, true);
            viewPort.attachScene(getEngine().getRootNode());
            ThreeDContext context = (ThreeDContext)getEngine().getContext();
            panel = context.createPanel();
            Dimension size = new Dimension(640, 480);
            panel.setPreferredSize(size);
            panel.attachTo(false, viewPort);
        }
        
        public Vector3f getHeadOffset() {
            return headOffset;
        }
        
        public void setHeadOffset(Vector3f value) {
            headOffset = value;
        }
        
        public Vector3f getSensorLocation() {
            Vector3f offset = getSensorOrientation().mult(headOffset);
            return getNode().getWorldTranslation().add(offset);
        }
        
        public Quaternion getSensorOrientation() {
            return getNode().getWorldRotation();
        }
        
        public Camera getCamera() {
            return camera;
        }
        
        public ViewPort getViewPort() {
            return viewPort;
        }
        
        public ThreeDPanel getPanel() {
            return panel;
        }
        
        public double[] getImage() {
            // TODO: Reimplement AwtPanels :(
            return new double[] {0, 0, 0, 0, 0};
        }
        
        public void update(float tpf) {
            if (camera == null)
                return;
            camera.setLocation(getSensorLocation());
            camera.setRotation(getSensorOrientation());
        }
    }
    
    public class WalkingEffector {
        private float walkSpeed = 6;
        private float turnSpeed = 3;
        private float walking = 0;
        private float turning = 0;
        
        public float getWalkSpeed() {
            return walkSpeed;
        }
        
        public void setWalkSpeed(float value) {
            walkSpeed = value;
        }
        
        public float getTurnSpeed() {
            return turnSpeed;
        }
        
        public void setTurnSpeed(float value) {
            turnSpeed = value;
        }
        
        public float getWalking() {
            return walking;
        }
        
        public void setWalking(float value) {
            walking = value;
        }
        
        public void walkCoupling(double value) {
            getEngine().enqueue(() -> {
                setWalking((float)value);
                return null;
            });
        }
        
        public float getTurning() {
            return turning;
        }
        
        public void setTurning(float value) {
            turning = value;
        }
        
        public void turnCoupling(double value) {
            getEngine().enqueue(() -> {
                setTurning((float)value);
                return null;
            });
        }
        
        public void update(float tpf) {
            AnimControl animator = getNode().getControl(AnimControl.class);
            if (animator != null) {
                if (animator.getNumChannels() == 0)
                    animator.createChannel();
                AnimChannel walkChannel = animator.getChannel(0);
                String currentAnim = walkChannel.getAnimationName();
                if (Math.abs(getWalking()) > 0.01 || Math.abs(getTurning()) > 0.01) {
                    if (currentAnim == null || !currentAnim.equals("Walk"))
                        animator.getChannel(0).setAnim("Walk");
                } else {
                    if (currentAnim == null || !currentAnim.equals("Idle"))
                        animator.getChannel(0).setAnim("Idle");
                }
            }
            getNode().rotate(0, getTurning() * turnSpeed * tpf, 0);
            float[] rotation = getNode().getLocalRotation().toAngles(null);
            float x = FastMath.sin(rotation[1]);
            float z = FastMath.cos(rotation[1]);
            Vector3f offset = new Vector3f(x, 0, z).multLocal(getWalking() * walkSpeed * tpf);
            getNode().move(offset);
        }
    }
    
    private VisionSensor sensor;
    private WalkingEffector effector;
    
    public Agent() {
        super();
        sensor = new VisionSensor();
        effector = new WalkingEffector();
    }
    
    public Agent(ThreeDEngine engine, Node node) {
        super(engine, node);
        sensor = new VisionSensor();
        effector = new WalkingEffector();
    }
    
    public VisionSensor getVisionSensor() {
        return sensor;
    }
    
    public WalkingEffector getWalkingEffector() {
        return effector;
    }
}
