package org.simbrain.world.threedworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.actions.ActionManager;
import org.simbrain.world.threedworld.controllers.AgentController;
import org.simbrain.world.threedworld.controllers.CameraController;
import org.simbrain.world.threedworld.controllers.ClipboardController;
import org.simbrain.world.threedworld.controllers.SelectionController;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Entity;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

/**
 * ThreeDWorld is a container for the engine, entities, and controllers needed
 * for the simbrain 3d environment.
 */
public class ThreeDWorld implements AppState {
    /**
     * WorldListener receives notifications when a ThreeDWorld is initialized or updated.
     */
    public interface WorldListener {
        /** @param world The world which has been initialized. */
        void onWorldInitialize(ThreeDWorld world);
        /** @param world The world which has been updated. */
        void onWorldUpdate(ThreeDWorld world);
    }

    private transient boolean initialized;
    private ThreeDEngine engine;
    private String scene;
    private List<Entity> entities;
    private CameraController cameraController;
    private transient SelectionController selectionController;
    private transient AgentController agentController;
    private transient ClipboardController clipboardController;
    private transient List<WorldListener> listeners;
    private transient Map<String, AbstractAction> actions;
    private transient ContextMenu contextMenu;
    private AtomicInteger idCounter;

    /**
     * Construct a new default ThreeDWorld().
     */
    public ThreeDWorld() {
        initialized = false;
        engine = new ThreeDEngine();
        engine.getStateManager().attach(this);
        cameraController = new CameraController(this);
        selectionController = new SelectionController(this);
        agentController = new AgentController(this);
        clipboardController = new ClipboardController(this);
        listeners = new ArrayList<WorldListener>();
        scene = "Scenes/GrassyPlain.j3o";
        entities = new ArrayList<Entity>();
        actions = ActionManager.createActions(this);
        contextMenu = new ContextMenu(this);
        idCounter = new AtomicInteger();
    }

    /**
     * @return A deserialized ThreeDWorld.
     */
    public Object readResolve() {
        initialized = false;
        engine.getStateManager().attach(this);
        //cameraController = new CameraController(this);
        selectionController = new SelectionController(this);
        agentController = new AgentController(this);
        clipboardController = new ClipboardController(this);
        listeners = new ArrayList<WorldListener>();
        actions = ActionManager.createActions(this);
        contextMenu = new ContextMenu(this);
        return this;
    }

    /**
     * @param name The name of the action.
     * @return An AWT action, if it exists.
     */
    public AbstractAction getAction(String name) {
        return actions.get(name);
    }

    /**
     * @return The ThreeDEngine which is rendering this world.
     */
    public ThreeDEngine getEngine() {
        return engine;
    }

    /**
     * @return The controller for the editor camera.
     */
    public CameraController getCameraController() {
        return cameraController;
    }

    /**
     * @return The controller for selected entities.
     */
    public SelectionController getSelectionController() {
        return selectionController;
    }

    /**
     * @return The controller for active agents.
     */
    public AgentController getAgentController() {
        return agentController;
    }

    /**
     * @return The controller for the ThreeDWorld clipboard.
     */
    public ClipboardController getClipboardController() {
        return clipboardController;
    }

    /**
     * Add a listener to this ThreeDWorld to receive initialize and update notifications.
     * @param listener The listener to notify.
     */
    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from this ThreeDWorld.
     * @param listener The listener to remove.
     */
    public void removeListener(WorldListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return The ContextMenu containing GUI actions for the world.
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * @return The name of the current scene.
     */
    public String getScene() {
        return scene;
    }

    /**
     * @param value The name of the scene to load.
     */
    public void loadScene(String value) {
        scene = value;
        Node newRoot = (Node) engine.getAssetManager().loadModel(scene);
        Node oldRoot = (Node) engine.getRootNode();
        for (Entity entity : entities) {
            oldRoot.detachChild(entity.getNode());
            newRoot.attachChild(entity.getNode());
        }
        engine.setRootNode(newRoot);
    }

    /**
     * @return The current list of entities in the world.
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @return A new unique identifier for an entity.
     */
    public String createId() {
        return String.valueOf(idCounter.getAndIncrement());
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application application) {
        if (initialized) {
            throw new RuntimeException("ThreeDWorld cannot be initialized twice");
        }
        // HACK: selection controller has to be registered before camera controller
        // to intercept the mouse look before it starts, should find a more robust option
        selectionController.registerInput();
        cameraController.registerInput();
        cameraController.setCamera(application.getCamera());
        cameraController.moveCameraHome();
        agentController.registerInput();
        engine.setRootNode((Node) engine.getAssetManager().loadModel(scene));
        for (WorldListener listener : listeners) {
            listener.onWorldInitialize(ThreeDWorld.this);
        }
        initialized = true;
    }

    @Override
    public void setEnabled(boolean value) { }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) { }

    @Override
    public void stateDetached(AppStateManager stateManager) { }

    @Override
    public void update(float tpf) {
        if (engine.getState() == ThreeDEngine.State.RunAll) {
            for (Entity entity : getEntities()) {
                entity.update(tpf);
            }
            for (WorldListener listener : listeners) {
                listener.onWorldUpdate(ThreeDWorld.this);
            }
        }
    }

    @Override
    public void render(RenderManager rm) { }

    @Override
    public void postRender() { }

    @Override
    public void cleanup() {
        cameraController.unregisterInput();
        selectionController.unregisterInput();
        initialized = false;
    }
}
