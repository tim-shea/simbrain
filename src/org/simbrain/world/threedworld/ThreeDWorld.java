package org.simbrain.world.threedworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.actions.ActionManager;
import org.simbrain.world.threedworld.controllers.AgentController;
import org.simbrain.world.threedworld.controllers.CameraController;
import org.simbrain.world.threedworld.controllers.SelectionController;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Entity;
import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;

public class ThreeDWorld {
    private class StateListener implements AppState {
        private boolean initialized = false;
        private boolean enabled = false;
        
        @Override
        public boolean isInitialized() {
            return initialized;
        }
        
        @Override
        public void initialize(AppStateManager stateManager, Application application) {
            if (initialized)
                throw new RuntimeException(
                        "ProtoWorldComponent.StateListener cannot be initialized twice");
            setEnabled(true);
            // HACK: selection controller has to be registered before camera controller
            // to intercept the mouse look before it starts, should find a more robust option
            selectionController.registerInput();
            cameraController.registerInput();
            cameraController.setCamera(application.getCamera());
            cameraController.moveCameraHome();
            agentController.registerInput();
            initialized = true;
        }
        
        @Override
        public void setEnabled(boolean value) {
            enabled = value;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
        
        @Override
        public void stateAttached(AppStateManager stateManager) {}
        
        @Override
        public void stateDetached(AppStateManager stateManager) {}
        
        @Override
        public void update(float tpf) {
            if (engine.getState() == ThreeDEngine.State.RunAll) {
                for (Entity entity : getEntities())
                    entity.update(tpf);
            }
        }
        
        @Override
        public void render(RenderManager rm) {}
        
        @Override
        public void postRender() {}
        
        @Override
        public void cleanup() {
            cameraController.unregisterInput();
            selectionController.unregisterInput();
            initialized = false;
        }
    }
    
    private Preferences preferences;
    private ThreeDEngine engine;
    private List<Entity> entities;
    private transient Map<String, AbstractAction> actions;
    private transient CameraController cameraController;
    private transient SelectionController selectionController;
    private transient AgentController agentController;
    private AtomicInteger idCounter;
    
    public ThreeDWorld() {
        preferences = new Preferences();
        actions = ActionManager.createActions(this);
        engine = new ThreeDEngine(preferences);
        engine.getStateManager().attach(new StateListener());
        cameraController = new CameraController(this);
        selectionController = new SelectionController(this);
        agentController = new AgentController(this);
        entities = new ArrayList<Entity>();
        idCounter = new AtomicInteger();
    }
    
    public Object readResolve() {
        engine.getStateManager().attach(new StateListener());
    	actions = ActionManager.createActions(this);
        cameraController = new CameraController(this);
        selectionController = new SelectionController(this);
        agentController = new AgentController(this);
        return this;
    }
    
    public Preferences getPreferences() {
        return preferences;
    }
    
    public AbstractAction getAction(String name) {
        return actions.get(name);
    }
    
    public ThreeDEngine getEngine() {
        return engine;
    }
    
    public CameraController getCameraController() {
        return cameraController;
    }
    
    public SelectionController getSelectionController() {
        return selectionController;
    }
    
    public AgentController getAgentController() {
        return agentController;
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
    
    public void setEntities(List<Entity> value) {
        entities = value;
    }
    
    public String createId() {
        return String.valueOf(idCounter.getAndIncrement());
    }
}
