package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.ThreeDWorldComponent;

public class ActionManager {
    public static Map<String, AbstractAction> createActions(ThreeDWorld world) {
        Map<String, AbstractAction> actions = new HashMap<String, AbstractAction>();
        putAction(actions, new AddEntityAction(world));
        putAction(actions, new AddAgentAction(world));
        putAction(actions, new NotImplementedAction("Add Wall"));
        putAction(actions, new ControlAgentAction(world));
        putAction(actions, new ReleaseAgentAction(world));
        putAction(actions, new EditEntityAction(world, false));
        putAction(actions, new DeleteEntityAction(world, false));
        putAction(actions, new NotImplementedAction("Edit Floor"));
        putAction(actions, new EditWorldPreferencesAction(world));
        putAction(actions, new UpdateAction(world));
        putAction(actions, new RunAction(world));
        putAction(actions, new PauseAction(world));
        putAction(actions, new SnapTransformsAction(world));
        putAction(actions, new DebugPhysicsAction(world));
        putAction(actions, new CameraHomeAction(world));
        return actions;
    }
    
    private static void putAction(Map<String, AbstractAction> actions, AbstractAction action) {
        actions.put((String)action.getValue(AbstractAction.NAME), action);
    }
    
    private static class NotImplementedAction extends AbstractAction {
        public NotImplementedAction(String name) {
            super(name);
            setEnabled(false);
        }
        
        @Override public void actionPerformed(ActionEvent e) {
            throw new RuntimeException("Action Not Implemented!");
        }
    }
}
