package org.simbrain.world.imageworld.filters;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

public class ThresholdFilter extends ImageFilter implements  BufferedImageOp {

    public static final String NAME = "Threshold";

    /** Default threshold value. */
    public final static float DEFAULT_THRESHOLD = .5f;

    /** The threshold value. */
    private float threshold = DEFAULT_THRESHOLD;

    /**
     * Construct a threshold filter with a specified threshold.
     *
     * @param thresholdValue threshold value
     */
    public ThresholdFilter(float thresholdValue) {
        this.threshold = thresholdValue;
    }
    
    /**
     * Construct a threshold filter.
     */
    public ThresholdFilter() {
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if (dest == null) {
            dest = createCompatibleDestImage(src, null);
        }
        for (int x = 0; x < src.getWidth(); ++x) {
            for (int y = 0; y < src.getHeight(); ++y) {
                float luminance = getLuminance(src.getRGB(x, y));
                dest.setRGB(x, y, luminance >= threshold ? 0x00FFFFFF : 0);
            }
        }
        return dest;
    }

    private float getLuminance(int color) {
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = (color >>> 0) & 0xFF;
        return (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return null;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src,
            ColorModel destCM) {
        return new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        return null;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    public float getThreshold() {
        return threshold;
    }
    
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public BufferedImageOp getFilter() {
        return this;
    }

    @Override
    public double getValue(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        // TODO: A better way to do this?
        if (rgb == 0x00FFFFFF) {
            return 1;
        } else {
            return 0;
        }
    }
}