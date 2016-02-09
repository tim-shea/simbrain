package org.simbrain.world.threedworld.engine;

import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.simbrain.world.threedworld.Preferences;

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

/**
 * <code>ProtoApplication</code> is a modification of jme3 SimpleApplication to
 * give greater control over the update cycle, input mapping, and root node.
 *
 */
public class ThreeDEngine extends Application {
    public enum State {
        RunAll,
        RenderOnly,
        SystemPause
    }
    
    private Preferences preferences;
    private ThreeDContext context;
    private ThreeDPanel panel;
    private ThreeDView view;
    private BulletAppState bulletAppState;
    private Node rootNode;
    private Node guiNode;
    private AmbientLight ambientLight;
    private DirectionalLight directionalLight;
    private State state;
    private boolean updateSync;
    private float fixedTimeStep = 1 / 60f;
    
    public ThreeDEngine() {
        this(new Preferences());
    }
    
    public ThreeDEngine(Preferences preferences) {
        super();
        this.preferences = preferences;
        
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(preferences.getFrameRate());
        settings.setCustomRenderer(ThreeDContext.class);
        setSettings(settings);
        start();
        
        context = (ThreeDContext)getContext();
        panel = context.createPanel();
        panel.setPreferredSize(new Dimension(settings.getWidth(), settings.getHeight()));
        context.setInputSource(panel);
        setPauseOnLostFocus(false);
        
        bulletAppState = new BulletAppState();
        bulletAppState.setEnabled(false);
        getStateManager().attach(bulletAppState);
    }
    
    public ThreeDPanel getPanel() {
        return panel;
    }
    
    public ThreeDView getMainView() {
        return view;
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
    
    public void queueState(State value, boolean wait) {
        enqueue(() -> {
            setState(value);
        }, wait);
    }
    
    protected void setState(State value) {
        state = value;
        switch (state) {
        case RunAll:
            timer.reset();
            paused = false;
            bulletAppState.setEnabled(true);
            break;
        case RenderOnly:
            paused = false;
            bulletAppState.setEnabled(false);
            break;
        case SystemPause:
            paused = true;
            bulletAppState.setEnabled(false);
            break;
        }
    }
    
    public boolean getUpdateSync() {
        return updateSync;
    }
    
    public void setUpdateSync(boolean value) {
        updateSync = value;
    }
    
    public void enqueue(Runnable runnable) {
        enqueue(runnable, false);
    }
    
    public void enqueue(Runnable runnable, boolean wait) {
        Future<Object> future = enqueue(() -> {
            runnable.run();
            return null;
        });
        if (wait) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Unable to complete run in thread", e);
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
        
        getAssetManager().registerLocator("bin/org/simbrain/resource/ThreeDAssets/assets/", FileLocator.class);
        
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
        
        view = new ThreeDView(getCamera().getWidth(), getCamera().getHeight());
        view.attach(true, getViewPort());
        panel.setView(view, true);
        
        updateSync = false;
        setState(State.RunAll);
    }
    
    @Override
    public void update() {
        runQueuedTasks();
        if (paused)
            return;
        
        float tpf;
        if (updateSync) {
            tpf = fixedTimeStep;
        } else if (state == State.RenderOnly) {
            tpf = 0;
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
        
        if (updateSync)
            paused = true;
    }
    
    public void updateSync() {
        if (!updateSync)
            return;
        while (!paused) {
            enqueue(() -> {}, true);
        }
        paused = false;
    }
}
