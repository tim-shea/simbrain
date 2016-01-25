package org.simbrain.world.threedworld;

import java.io.InputStream;
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
import org.simbrain.world.threedworld.engine.ThreeDEngineConverter;
import org.simbrain.world.threedworld.entities.Entity;
import org.simbrain.world.threedworld.entities.EntityXmlConverter;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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
            selectionController.setEnabled(true);
            cameraController.registerInput();
            cameraController.setCamera(application.getCamera());
            cameraController.moveCameraHome();
            cameraController.setEnabled(true);
            agentController.registerInput();
            agentController.setEnabled(false);
            initialized = true;
        }
        
        @Override
        public void setEnabled(boolean value) {
            cameraController.setEnabled(value);
            selectionController.setEnabled(value);
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
            if (engine.getState() == ThreeDEngine.State.Render)
                return;
            for (Entity entity : getEntities())
                entity.update(tpf);
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
    
    public static XStream getXStream() {
        XStream stream = new XStream(new DomDriver());
        stream.registerConverter(new ThreeDEngineConverter());
        stream.registerConverter(new EntityXmlConverter());
        stream.omitField(ThreeDWorld.class, "actions");
        stream.omitField(ThreeDWorld.class, "cameraController");
        stream.omitField(ThreeDWorld.class, "selectionController");
        stream.omitField(ThreeDWorld.class, "agentController");
        return stream;
    }
    
    public static ThreeDWorld deserialize(InputStream input, String name, String format) {
        ThreeDWorld world = (ThreeDWorld)ThreeDWorld.getXStream().fromXML(input);
        world.actions = ActionManager.createActions(world);
        world.engine.getStateManager().attach(world.new StateListener());
        world.cameraController = new CameraController(world);
        world.selectionController = new SelectionController(world);
        world.agentController = new AgentController(world);
        world.getEngine().queueState(ThreeDEngine.State.Render, false);
        return world;
    }
    
    private Preferences preferences;
    private Map<String, AbstractAction> actions;
    private ThreeDEngine engine;
    private CameraController cameraController;
    private SelectionController selectionController;
    private AgentController agentController;
    private List<Entity> entities;
    private AtomicInteger idCounter = new AtomicInteger();
    
    public ThreeDWorld() {
        preferences = new Preferences();
        actions = ActionManager.createActions(this);
        engine = new ThreeDEngine(preferences);
        engine.getStateManager().attach(new StateListener());
        cameraController = new CameraController(this);
        selectionController = new SelectionController(this);
        agentController = new AgentController(this);
        entities = new ArrayList<Entity>();
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
