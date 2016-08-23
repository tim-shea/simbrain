package org.simbrain.util.projection;

import java.awt.Color;

import org.simbrain.util.Utils;
import org.simbrain.util.math.SimbrainMath;

/**
 * Utility for coloring points of a dataset using a "halo".
 */
public class Halo {

    // TODO: This should be coordinated with DataColorManager. But
    // this method overwrites all datapoints so I'm not sure the best approach.

    /**
     * "Radius" of the halo. Outside this distance (in the high dimensional
     * vector space) points are colored gray. Inside they are colored red.
     */
    static float haloRadius = .2f;

    /**
     * Maximum saturation.
     */
    static float maxSaturation = 1f;

    /**
     * Minimum saturation. If set to 0 points saturate to white.
     */
    static float minSaturation = .2f;

    /**
     * Color the points in the plot according to how close they are to a
     * provided target value. A "halo" of red is created around the target
     * point.
     *
     * The current point in the dataset is colored green.
     *
     * @param proj the projector whose points should be colored
     * @param target target value around which the halo is created.
     */
    public static void makeHalo(Projector proj, double[] target) {
        for (int i = 0; i < proj.getUpstairs().getNumPoints(); i++) {

            // Color the current point green
            double[] point = proj.getUpstairs().getPoint(i).getVector();
            if (java.util.Arrays.equals(point,
                    proj.getCurrentPoint().getVector())) {
                ((DataPointColored) proj.getUpstairs().getPoint(i))
                        .setColor(Color.green);
                continue;
            }

            // Color the target point red and a halo of saturated red around it,
            // scaled from max to min saturation.  Outside of the radius color
            // points gray.
            double distance = SimbrainMath.distance(target, point);
            if (distance < haloRadius) {
                float slope = -(maxSaturation - minSaturation) / haloRadius;
                float saturation = (float) (distance * slope + 1);
                ((DataPointColored) proj.getUpstairs().getPoint(i)).setColor(
                        Color.getHSBColor(Utils.colorToFloat(Color.red),
                                saturation, 1));
            } else {
                ((DataPointColored) proj.getUpstairs().getPoint(i))
                        .setColor(Color.gray);
            }

        }
    }
}
