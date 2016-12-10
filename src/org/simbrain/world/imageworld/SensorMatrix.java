package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import org.simbrain.util.Producible;
import org.simbrain.world.imageworld.filters.GrayFilter;
import org.simbrain.world.imageworld.filters.IdentityFilter;
import org.simbrain.world.imageworld.filters.ImageFilter;
import org.simbrain.world.imageworld.filters.ThresholdFilter;

/**
 * 
 * A transformation of the base image that can downsample to a certain size and
 * then apply one or more transformation to the dowsampled image. A "matrix" of
 * pixels that can be coupled to.
 * 
 * @author Jeff Yoshimi
 */
public class SensorMatrix {

    /** Name of this matrix. */
    private String name;

    /** Logical width of image. */
    private int width = 50;

    /** Logical height of image. */
    private int height = 50;

    /** The filtered image. */
    private BufferedImage image;

    /** The source for the base image. */
    private final StaticImageSource source;

    /** Transform to rescale image as needed. */
    private BufferedImageOp rescaler;

    /** List of filter types. Used in combo box. */
    public static String[] FILTER_TYPES = { IdentityFilter.NAME,
            GrayFilter.NAME, ThresholdFilter.NAME };

    /** The current image filter. */
    private ImageFilter filter = null;

    /** The vales this matrix produces for couplings. */
    private double[] sensorValues;

    /**
     * Construct the sensor matrix with a specified name.
     *
     * @param name the name of this sensor matrix
     * @param source the source for the image
     */
    public SensorMatrix(final String name, final StaticImageSource source) {
        this.source = source;
        this.image = source.getUnfilteredImage();
        this.name = name;
    }

    /**
     * Construct a sensor matrix.
     *  
     * @param imageSource the source for the image
     */
    public SensorMatrix(StaticImageSource imageSource) {
        this.source = imageSource;
        this.image = source.getUnfilteredImage();
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Set the current filter
     *
     * @param filter the filter to set
     */
    public void setFilter(ImageFilter filter) {
        this.filter = filter;
    }

    /**
     * Rescale the image and apply filters.
     */
    public void applyFilters() {
        // System.out.println("In sensorpanel.updateImage for " +
        // this.getPanelName());
        if ((filter != null) && (source != null)) {
            // Rescale original image to logical size
            rescaler = ImageFilters.scale(
                    (float) width / source.getUnfilteredImage().getWidth(),
                    (float) height / source.getUnfilteredImage().getHeight(),
                    false);
            image = rescaler.filter(source.getUnfilteredImage(), null);
            image = filter.getFilter().filter(image, null);
            updateSensorValues();
        }
    }

    /**
     * Update the sensor matrix values.
     * 
     * Called every time the actual image is changed.    
     * Sets the array that users can couple to via {@link getSensorValues}.
     */
    private void updateSensorValues() {
        sensorValues = new double[width * height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                sensorValues[y * width + x] = filter.getValue(image, x, y);
            }
        }

    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the filtered image associated with this sensor matrix
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * @return Returns an array of doubles for the each pixel
     */
    @Producible(customDescriptionMethod = "getName")
    public double[] getSensorValues() {
        return sensorValues;
    }

    /**
     * Get the current filter associated with this sensor matrix.
     *
     * @return the currentFilter
     */
    public ImageFilter getFilter() {
        return filter;
    }

}
