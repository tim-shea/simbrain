package org.simbrain.custom.rl_sim;

import org.simbrain.world.odorworld.entities.OdorWorldEntity;

//CHECKSTYLE:OFF
public class CheeseFlower extends RL_Sim {

    public CheeseFlower(RL_Sim_Main mainSim) {
        super(mainSim);

        // Move past cheese alone
        cp.addButton("Load", () -> {
            load();
        });

        // Move past cheese alone
        cp.addButton("Cheese", () -> {
            singleTrail("cheese");
        });

        // Mouse past flower alone
        cp.addButton("Flower", () -> {
            singleTrail("flower");
        });
    }

    @Override
    public void load() {

        // Set the new world size
        mainSim.world.setHeight(250);
        mainSim.world.setWidth(700);

        // Set up mouse
        initialMouseLocation_x = 43;
        initialMouseLocation_y = 110;
        initialMouseHeading = 0;
        mainSim.resetMouse();

        // Set up cheese 1
        mainSim.cheese_1.setLocation(351, 29);
        mainSim.cheese_1.getSmellSource().setDispersion(350);
        mainSim.world.addEntity(mainSim.cheese_1);

        // Set up flower
        mainSim.flower.setLocation(351,215);
        mainSim.flower.getSmellSource().setDispersion(350);
        mainSim.world.addEntity(mainSim.flower);

        // Don't use cheese 2
        mainSim.world.deleteEntity(mainSim.cheese_2);

        // Update the world
        mainSim.world.fireUpdateEvent();

        // Update goals
        goalEntities.clear();
        goalEntities.add(mainSim.cheese_1);
        goalEntities.add(mainSim.flower);

    }

    /**
     * Make the mouse do a single pass by a specific object.
     *
     * @param object the object to pass by.
     */
    private void singleTrail(String object) {

        //TODO: Possibly promote this so that other sims can use it?

        // Don't do the RL updates while running this.
        mainSim.removeCustomAction();

        OdorWorldEntity objectToPass;
        OdorWorldEntity otherObject;

        if (object.equalsIgnoreCase("cheese")) {
            objectToPass = mainSim.cheese_1;
            otherObject = mainSim.flower;
        } else {
            objectToPass = mainSim.flower;
            otherObject = mainSim.cheese_1;
        }

        mainSim.network.clearActivations();

        // Remove other entity to get rid of interference
        mainSim.world.deleteEntity(otherObject);

        // Get mouse in position
        mainSim.mouse.setCenterLocation(
                (float) (objectToPass.getCenterX()
                        - objectToPass.getSmellSource().getDispersion()),
                (float) objectToPass.getCenterY());

        // Run past the object
        mainSim.mouse.setVelocityX(5);
        mainSim.mouse.setHeading(0);
        mainSim.iterate(100);

        // Clean up the mess I've made
        mainSim.mouse.setVelocityX(0);
        mainSim.resetMouse();
        mainSim.addCustomAction();
        mainSim.world.addEntity(otherObject);
    }

}
