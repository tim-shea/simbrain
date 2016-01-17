package org.simbrain.world.threedworld.entities;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.util.Arrays;









import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.engine.ThreeDView;









import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

public class Agent extends Entity {
    public class VisionSensor {
        private Vector3f headOffset = Vector3f.UNIT_Z.clone();
        private Camera camera;
        private ViewPort viewPort;
        private ThreeDView view;
        private double[] viewData;
        private ColorConvertOp colorConvertOp;
        private AffineTransformOp scaleOp;
        
        public VisionSensor() {
            camera = new Camera(640, 480);
            camera.setFrustumPerspective(45f, (float)camera.getWidth() / camera.getHeight(), 1f, 1000f);
            camera.setLocation(new Vector3f(0f, 0f, 10f));
            camera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y.clone());
            viewPort = getEngine().getRenderManager().createMainView(getName() + "ViewPort", camera);
            viewPort.setClearFlags(true, true, true);
            viewPort.attachScene(getEngine().getRootNode());
            view = new ThreeDView(640, 480);
            view.attach(false, viewPort);
            viewData = new double[100];
            
            // NOTE: This could be done in the engine to improve performance
            ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            colorConvertOp = new ColorConvertOp(colorSpace, null); 
            AffineTransform transform = AffineTransform.getScaleInstance(10f / 640, -10f / 480);
            transform.translate(0, -480);
            scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
            
            view.addListener((image) -> {
                if (view.isInitialized()) {
                    BufferedImage grayImage = colorConvertOp.filter(image, null);
                    BufferedImage scaledGrayImage = scaleOp.filter(grayImage, null);
                    int width = scaledGrayImage.getWidth();
                    int height = scaledGrayImage.getHeight();
                    for (int x = 0; x < width; ++x) {
                        for (int y = 0; y < height; ++y) {
                            int gray = scaledGrayImage.getRGB(x, y) & 0xFF;
                            viewData[y * width + x] = gray / 255.0;
                        }
                    }
                }
            });
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
        
        public ThreeDView getView() {
            return view;
        }
        
        public double[] getViewData() {
            return viewData;
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
