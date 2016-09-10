package org.simbrain.custom.rl_sim;

//CHECKSTYLE:OFF
public class TwoCheese extends RL_Sim {

    public TwoCheese(RL_Sim_Main mainSim) {
        super(mainSim);
        cp.addButton("Load", () -> {
            load();
        });
    }

    @Override
    public void load() {

        // Set the new world size
        mainSim.world.setHeight(350);
        mainSim.world.setWidth(350);

        // Set up mouse
        initialMouseLocation_x = 43;
        initialMouseLocation_y = 110;
        initialMouseHeading = 0;
        mainSim.resetMouse();

        // Set up cheese 1
        mainSim.cheese_1.setLocation(215, 29);
        mainSim.cheese_1.getSmellSource().setDispersion(400);
        mainSim.world.addEntity(mainSim.cheese_1);

        // Set up flower
        mainSim.cheese_2.setLocation(215, 215);
        mainSim.cheese_2.getSmellSource().setDispersion(400);
        mainSim.world.addEntity(mainSim.cheese_2);

        // Don't use flower
        mainSim.world.deleteEntity(mainSim.flower);

        // Update the world
        mainSim.world.fireUpdateEvent();
        
        // Update goals
        goalEntities.clear();
        goalEntities.add(mainSim.cheese_1);
        goalEntities.add(mainSim.cheese_2);
    }

}
