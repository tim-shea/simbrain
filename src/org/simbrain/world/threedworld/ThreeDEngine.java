package org.simbrain.world.threedworld;

import java.awt.Canvas;
import java.awt.Dimension;
import java.util.concurrent.Future;

import javax.swing.JPanel;

import com.jme3.app.Application;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioContext;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;

/**
 * <code>ProtoApplication</code> is a modification of jme3 SimpleApplication to
 * give greater control over the update cycle, input mapping, and root node.
 *
 */
public class ThreeDEngine extends Application {
    public enum State {
        Run,
        Render,
        SystemPause,
        UpdateOnce
    }
    
    private Preferences preferences;
    private JPanel panelContainer;
    private AwtPanel panel;
    private BulletAppState bulletAppState;
    private Node rootNode;
    private Node guiNode;
    private AmbientLight ambientLight;
    private DirectionalLight directionalLight;
    private State state = State.Render;
    private float fixedTimeStep = 1 / 60f;
    
    public ThreeDEngine(Preferences preferences) {
        super();
        this.preferences = preferences;
        
        AppSettings settings = new AppSettings(true);
        settings.setWidth(preferences.getWidth());
        settings.setHeight(preferences.getHeight());
        settings.setFrameRate(preferences.getFrameRate());
        settings.setCustomRenderer(AwtPanelsContext.class);
        setSettings(settings);
        start();
        
        AwtPanelsContext context = (AwtPanelsContext)getContext();
        panel = context.createPanel(PaintMode.Accelerated);
        context.setInputSource(panel);
        Dimension size = new Dimension(settings.getWidth(), settings.getHeight());
        panel.setPreferredSize(size);
        setPauseOnLostFocus(false);
        panelContainer = new JPanel();
        panelContainer.add(panel);
        
        bulletAppState = new BulletAppState();
        bulletAppState.setEnabled(false);
        getStateManager().attach(bulletAppState);
    }
    
    public JPanel getPanelContainer() {
    	return panelContainer;
    }
    
    public AwtPanel getPanel() {
        return panel;
    }
    
    public Node getRootNode() {
        return rootNode;
    }
    
    public Node getGuiNode() {
        return guiNode;
    }
    
    public AmbientLight getAmbientLight() {
        return ambientLight;
    }
    
    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State value, boolean wait) {
        Future<Object> future = enqueue(() -> {
            state = value;
            switch (state) {
            case Run:
            case UpdateOnce:
                paused = false;
                bulletAppState.setEnabled(true);
                break;
            case Render:
                paused = false;
                bulletAppState.setEnabled(false);
                break;
            case SystemPause:
                paused = true;
                bulletAppState.setEnabled(false);
                break;
            }
            return null;
        });
        if (wait) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
    
    public float getFixedTimeStep() {
        return fixedTimeStep;
    }
    
    public void setFixedTimeStep(float value) {
        fixedTimeStep = value;
    }
    
    @Override
    public void setSettings(AppSettings settings) {
    	super.setSettings(settings);
    	if (panel != null) {
    		Dimension size = new Dimension(settings.getWidth(), settings.getHeight());
    		panel.setPreferredSize(size);
    	}
    }
    
    private void loadScene(String fileName) {
        if (rootNode != null)
            throw new RuntimeException("Load scene must be called before initialization.");
        rootNode = (Node)getAssetManager().loadModel(fileName);
    }
    
    private void loadGui(String fileName) {
        if (guiNode != null)
            throw new RuntimeException("Load gui must be called before initialization.");
        guiNode = (Node)getAssetManager().loadModel(fileName);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        getAssetManager().registerLocator("simulations/worlds/assets/", FileLocator.class);
        
        if (!preferences.getSceneName().trim().isEmpty())
            loadScene(preferences.getSceneName());
        else
            loadScene("Scenes/GrassyPlane.j3o");
        bulletAppState.getPhysicsSpace().addAll(rootNode);
        viewPort.attachScene(rootNode);
        
        if (!preferences.getGuiName().trim().isEmpty())
            loadGui(preferences.getGuiName());
        else
            guiNode = new Node("Gui");
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        guiViewPort.attachScene(guiNode);
        
        for (Light light : getRootNode().getLocalLightList()) {
            if (light instanceof AmbientLight)
                ambientLight = (AmbientLight)light;
            else if (light instanceof DirectionalLight)
                directionalLight = (DirectionalLight)light;
        }
        
        if (ambientLight == null) {
            ambientLight = new AmbientLight();
            ambientLight.setColor(ColorRGBA.Gray);
            getRootNode().addLight(ambientLight);
        }
        
        if (directionalLight == null) {
            directionalLight = new DirectionalLight();
            directionalLight.setColor(ColorRGBA.Gray);
            directionalLight.setDirection(new Vector3f(1, -1, 1));
            getRootNode().addLight(directionalLight);
        }
        
        panel.attachTo(true, getViewPort());
    }
    
    @Override
    public void update() {
        runQueuedTasks();
        if (paused)
            return;
        
        float tpf;
        if (state == State.UpdateOnce) {
            // TODO: Fix weird single update physics shaking
            tpf = fixedTimeStep;
        } else {
            timer.update();
            tpf = timer.getTimePerFrame();
        }
        
        if (inputEnabled){
            inputManager.update(tpf);
        }
        
        AudioContext.setAudioRenderer(audioRenderer);
        if (audioRenderer != null){
            audioRenderer.update(tpf);
        }
        
        stateManager.update(tpf);
        
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();
        
        stateManager.render(renderManager);
        renderManager.render(tpf, context.isRenderable());
        stateManager.postRender();
        
        if (state == State.UpdateOnce) {
            state = State.Render;
            paused = true;
        }
    }
}
