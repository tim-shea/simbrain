package org.simbrain.world.threedworld.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.world.threedworld.ThreeDWorld;
import org.simbrain.world.threedworld.entities.Agent;
import org.simbrain.world.threedworld.entities.Entity;

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
            Node node = Entity.loadModel(world.getEngine(), "Mouse", "Models/Mouse.j3o");
            node.getControl(RigidBodyControl.class).setKinematic(true);
            Agent agent = new Agent(world.getEngine(), "Mouse", node);
            agent.getVisionSensor().setHeadOffset(new Vector3f(0, 1.25f, 2.25f));
            world.getEngine().getRootNode().attachChild(node);
            world.getEntities().add(agent);
            world.getEngine().getPhysicsSpace().add(agent.getBody());
            return null;
        });
    }
}
