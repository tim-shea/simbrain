package org.simbrain.world.imageworld.filters;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Filter for a sensor matrix.  
 * 
 * TODO: If this is kept, it should be cohered with ImageFilters.java
 *  
 * @author Jeff Yoshimi
 */
public abstract class ImageFilter {
   
    // Name used to identify the filter type.
    public abstract String getName();
    
    /** Get the actual filter associated with this object. */
    public abstract BufferedImageOp getFilter();
    
    /** Get the value associated with some location in an image. */
    public abstract double getValue(BufferedImage image, int x, int y); 
    
    public double getOverallValue(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        return (getRed(color) * 0.2126f
                + getGreen(color) * 0.7152f + getBlue(color) * 0.0722f) / 255;
    }
    
    public double getRedValue(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        return getRed(color) / 255;
    }
    
    public double getGreenValue(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        return getGreen(color) / 255;
    }
    
    public double getBlueValue(BufferedImage image, int x, int y) {
        int color = image.getRGB(x, y);
        return getBlue(color) / 255;
    }

    private int getRed(int color) {
        return (color >>> 16) & 0xFF;
    }
    private int getGreen(int color) {
        return (color >>> 8) & 0xFF;
    }
    private int getBlue(int color) {
        return (color & 0xFF);
    }

}
