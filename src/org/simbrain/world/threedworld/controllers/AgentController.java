package org.simbrain.world.threedworld.controllers;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.Agent;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.system.awt.AwtPanelsContext;

import static org.simbrain.world.threedworld.controllers.AgentController.Mapping.*;

public class AgentController implements ActionListener {
    enum Mapping {
        TurnLeft,
        TurnRight,
        WalkForward,
        WalkBackward;
        
        public boolean isName(String name) {
            return name.equals(toString());
        }
    }
    
    private ThreeDWorld world;
    private Agent agent;
    private boolean enabled;
    
    public AgentController(ThreeDWorld world) {
        this.world = world;
    }
    
    public void registerInput() {
        InputManager input = world.getEngine().getInputManager();
        input.addMapping(TurnLeft.toString(), new KeyTrigger(KeyInput.KEY_A));
        input.addMapping(TurnRight.toString(), new KeyTrigger(KeyInput.KEY_D));
        input.addMapping(WalkForward.toString(), new KeyTrigger(KeyInput.KEY_W));
        input.addMapping(WalkBackward.toString(), new KeyTrigger(KeyInput.KEY_S));
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
    
    public Agent getAgent() {
        return agent;
    }
    
    public void control(Agent agent) {
        this.agent = agent;
        agent.getBody().setKinematic(true);
        setEnabled(true);
        world.getSelectionController().setEnabled(false);
        world.getCameraController().setEnabled(false);
        world.getAction("Control Agent").setEnabled(false);
        world.getAction("Release Agent").setEnabled(true);
    }
    
    public void release() {
        agent = null;
        setEnabled(false);
        world.getSelectionController().setEnabled(true);
        world.getCameraController().setEnabled(true);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean value) {
        enabled = value;
        if (agent == null)
            return;
        ThreeDEngine engine = world.getEngine();
        /*if (enabled) {
            engine.enqueue(() -> {
                engine.getPanelContainer().remove(engine.getPanel());
                engine.getPanelContainer().add(agent.getVisionSensor().getPanel());
                ((AwtPanelsContext)engine.getContext()).setInputSource(agent.getVisionSensor().getPanel());
                return null;
            });
        } else {
            engine.enqueue(() -> {
                engine.getPanelContainer().remove(agent.getVisionSensor().getPanel());
                engine.getPanelContainer().add(engine.getPanel());
                ((AwtPanelsContext)engine.getContext()).setInputSource(engine.getPanel());
                return null;
            });
        }*/
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!enabled || agent == null)
            return;
        agent.getBody().activate();
        if (TurnLeft.isName(name)) {
            agent.getWalkingEffector().setTurning(isPressed ? 1 : 0);
        } else if (TurnRight.isName(name)) {
            agent.getWalkingEffector().setTurning(isPressed ? -1 : 0);
        } else if (WalkForward.isName(name)) {
            agent.getWalkingEffector().setWalking(isPressed ? 1 : 0);
        } else if (WalkBackward.isName(name)) {
            agent.getWalkingEffector().setWalking(isPressed ? -1 : 0);
        }
    }
}
