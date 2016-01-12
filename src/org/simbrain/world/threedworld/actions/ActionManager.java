package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class ActionManager {
    private class NotImplementedAction extends AbstractAction {
        public NotImplementedAction(String name) {
            super(name);
            setEnabled(false);
        }
        
        @Override public void actionPerformed(ActionEvent e) {
            throw new RuntimeException("Action Not Implemented!");
        }
    }
    
    private AbstractAction addEntity;
    private AbstractAction addAgent;
    private AbstractAction addWall;
    private AbstractAction controlAgent;
    private AbstractAction releaseAgent;
    private AbstractAction editEntity;
    private AbstractAction deleteEntity;
    private AbstractAction editFloorTexture;
    private AbstractAction editWorldPreferences;
    private AbstractAction update;
    private AbstractAction run;
    private AbstractAction pause;
    private AbstractAction snapTransforms;
    private AbstractAction debugPhysics;
    
    public ActionManager(ThreeDWorld world) {
        addEntity = new AddEntityAction(world);
        addAgent = new AddAgentAction(world);
        addWall = new NotImplementedAction("Add Wall");
        controlAgent = new ControlAgentAction(world);
        releaseAgent = new ReleaseAgentAction(world);
        editEntity = new EditEntityAction(world);
        editEntity.setEnabled(false);
        deleteEntity = new DeleteEntityAction(world);
        deleteEntity.setEnabled(false);
        editFloorTexture = new NotImplementedAction("Edit Floor");
        editWorldPreferences = new EditWorldPreferencesAction(world);
        update = new UpdateAction(world);
        run = new RunAction(world);
        pause = new PauseAction(world);
        snapTransforms = new SnapTransformsAction(world);
        debugPhysics = new DebugPhysicsAction(world);
    }
    
    public AbstractAction getAddEntity() {
        return addEntity;
    }
    
    public AbstractAction getAddAgent() {
        return addAgent;
    }
    
    public AbstractAction getAddWall() {
        return addWall;
    }
    
    public AbstractAction getControlAgent() {
        return controlAgent;
    }
    
    public AbstractAction getReleaseAgent() {
        return releaseAgent;
    }
    
    public AbstractAction getEditEntity() {
        return editEntity;
    }
    
    public AbstractAction getDeleteEntity() {
        return deleteEntity;
    }
    
    public AbstractAction getEditFloorTexture() {
        return editFloorTexture;
    }
    
    public AbstractAction getEditWorldPreferences() {
        return editWorldPreferences;
    }
    
    public AbstractAction getUpdate() {
        return update;
    }
    
    public AbstractAction getRun() {
        return run;
    }
    
    public AbstractAction getPause() {
        return pause;
    }

    public AbstractAction getSnapTransforms() {
        return snapTransforms;
    }
    
    public AbstractAction getDebugPhysics() {
        return debugPhysics;
    }
}
