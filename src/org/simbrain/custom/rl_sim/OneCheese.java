package org.simbrain.custom.rl_sim;

//CHECKSTYLE:OFF
public class OneCheese extends RL_Sim {

    public OneCheese(RL_Sim_Main mainSim) {
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
        initialMouseLocation_x = 45;
        initialMouseLocation_y = 45;
        initialMouseHeading = 315;
        mainSim.resetMouse();

        // Set up cheese 1
        mainSim.cheese_1.setLocation(218, 196);
        mainSim.cheese_1.getSmellSource().setDispersion(400);
        mainSim.world.addEntity(mainSim.cheese_1);

        // Don't use flower or second cheese
        mainSim.world.deleteEntity(mainSim.flower);
        mainSim.world.deleteEntity(mainSim.cheese_2);

        // Update the world
        mainSim.world.fireUpdateEvent();

        // Update goals
        goalEntities.clear();
        goalEntities.add(mainSim.cheese_1);
    }

}
