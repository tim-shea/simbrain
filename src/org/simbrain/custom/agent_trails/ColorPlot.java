package org.simbrain.custom.agent_trails;

import java.awt.Color;

import org.simbrain.util.Utils;
import org.simbrain.util.projection.DataPointColored;
import org.simbrain.util.projection.Projector;
import org.simbrain.workspace.updater.UpdateAction;

/**
 * Todo.
 */
public class ColorPlot implements UpdateAction {

    /** Reference to simulation object that has all the main variables used. */
    AgentTrails sim;

    // TODO: Think and explain
    double scale = 5;

    Projector projector;

    /**
     * Construct the updater.
     */
    public ColorPlot(AgentTrails sim) {
        super();
        this.sim = sim;
        projector = sim.plot.getProjectionModel().getProjector();
    }

    @Override
    public String getDescription() {
        return "Color projection points";
    }

    @Override
    public String getLongDescription() {
        return "Color projection points";
    }

    @Override
    public void invoke() {
        double[] predictedState = sim.predictionNet.getActivations();

        // // Iterate through points and color them
        System.out.println(projector.getUpstairs().getNumPoints());
        for (int i = 0; i < projector.getUpstairs().getNumPoints(); i++) {
            double[] currentPoint = projector.getUpstairs().getPoint(i)
                    .getVector();
            if (java.util.Arrays.equals(currentPoint,
                    projector.getCurrentPoint().getVector())) {
                ((DataPointColored) projector.getUpstairs().getPoint(i))
                        .setColor(Color.green);
                continue;
            }
            // TODO: Use built in util. Need to make projector 5-d
            double distance = 0;
            for (int j = 0; j < predictedState.length; j++) {
                distance += Math.pow(predictedState[j] - currentPoint[j], 2);
            }
            distance = Math.sqrt(distance);
            float saturation = (float) (1 - distance * scale);
            saturation = saturation > 0 ? saturation : 0;
            // System.out.println(saturation);
            if (saturation > 0) {
                ((DataPointColored) projector.getUpstairs().getPoint(i))
                        .setColor(Color.getHSBColor(
                                Utils.colorToFloat(Color.red), saturation, 1));
            } else {
                ((DataPointColored) projector.getUpstairs().getPoint(i))
                        .setColor(Color.gray);
            }
        }
    }

}
