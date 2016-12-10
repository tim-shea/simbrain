package org.simbrain.world.imageworld;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;

import org.simbrain.world.imageworld.filters.ThresholdFilter;

/**
 * ImageFilters provides a set of factory methods for creating BufferedImageOps
 * that apply basic image manipulations.
 *
 * @author Tim Shea
 */
public class ImageFilters {

    /**
     * @return Returns an identity (non-op) transformation.
     */
    public static BufferedImageOp identity() {
        AffineTransform transform = new AffineTransform();
        return new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }

    /**
     * @param height The height of the image to flip.
     * @return Returns a vertical flip transformation.
     */
    public static BufferedImageOp flip(int height) {
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -height);
        return new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }

    /**
     * @param x The horizontal scaling factor.
     * @param y The vertical scaling factor.
     * @param smooth Whether to use smooth (vs nearest-neighbor) scaling.
     * @return Returns a scaling operation.
     */
    public static BufferedImageOp scale(float x, float y, boolean smooth) {
        AffineTransform transform = AffineTransform.getScaleInstance(x, y);
        int interpolation = smooth ? AffineTransformOp.TYPE_BILINEAR
                : AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        return new AffineTransformOp(transform, interpolation);
    }

    /**
     * @return Returns a transformation to grayscale colorspace.
     */
    public static BufferedImageOp gray() {
        ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        return new ColorConvertOp(gray, null);
    }

    /**
     * @param thresholdValue The grayscale value to use as the cut-off for white
     *            pixels.
     * @return Returns a colorspace transformation such that grayscale values
     *         above a threshold are returned as white and all other values are
     *         black.
     */
    public static BufferedImageOp threshold(float thresholdValue) {
        return new ThresholdFilter(thresholdValue);
    } 

}
