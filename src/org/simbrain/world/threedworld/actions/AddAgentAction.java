package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;
import org.simbrain.world.threedworld.entities.VisionSensor;
import org.simbrain.world.threedworld.entities.WalkingEffector;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class AddAgentAction extends AbstractAction {
    private ThreeDWorld world;
    
    public AddAgentAction(ThreeDWorld world) {
        super("Add Agent");
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        world.getEngine().enqueue(() -> {
            String fileName = "Models/NewMouse.j3o";
            String modelNode = "Mouse";
            Node node = Entity.loadModel(world.getEngine(), modelNode, fileName);
            Agent agent = new Agent(world.getEngine(), node);
            agent.setName("Mouse" + world.createId());
            VisionSensor sensor = new VisionSensor(agent);
            sensor.setHeadOffset(new Vector3f(0, 1.25f, 2.25f));
            WalkingEffector effector = new WalkingEffector(agent);
            effector.setWalkAnimSpeed(3);
            agent.getBody().setKinematic(true);
            world.getEngine().getRootNode().attachChild(node);
            world.getEntities().add(agent);
            world.getEngine().getPhysicsSpace().add(agent.getBody());
            world.getSelectionController().select(agent);
            if (world.getSelectionController().getCursorContact(null) != null)
                world.getSelectionController().translateToCursor();
            else {
                Vector3f location = Vector3f.ZERO.clone();
                Vector3f offset = Vector3f.UNIT_Y.clone();
                world.getSelectionController().offsetBoundingVolume(location, offset);
                world.getSelectionController().translateSelection(location);
            }
            return null;
        });
    }
}
