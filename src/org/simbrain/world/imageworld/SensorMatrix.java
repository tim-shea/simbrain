package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import org.simbrain.util.Producible;
import org.simbrain.util.propertyeditor.ComboBoxWrapper;

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

    // TODO: Allow the matrix to contain an editable list of filters
    // private List<BufferedImageOp> filters = new ArrayList<BufferedImageOp>();

    /** List of filter types. */
    private String[] filterTypes = { "None", "Gray", "Threshold" };

    /** The current filter. */
    private String currentFilterName = "None";

    /** The current filter. */
    private BufferedImageOp currentFilter = null;

    /** The vales this matrix produces for couplings. */
    private double[] sensorValues;

    /**
     * Construct the sensor matrix.
     *
     * @param name the name of this sensor matrix
     * @param source the source for the image
     */
    public SensorMatrix(final String name, final StaticImageSource source) {
        super();
        this.source = source;
        this.image = source.getUnfilteredImage();
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Temp for property editor.
     */
    public ComboBoxWrapper getFilter() {
        return new ComboBoxWrapper() {
            public Object getCurrentObject() {
                return currentFilterName;
            }

            public Object[] getObjects() {
                return filterTypes;
            }
        };
    }

    /**
     * Temp for property editor.
     */
    public void setFilter(ComboBoxWrapper filter) {
        currentFilterName = (String) filter.getCurrentObject();
        setFilter((String) filter.getCurrentObject());
    }

    /**
     * Set the current filter
     *
     * @param filterName
     */
    public void setFilter(String filterName) {
        currentFilterName = filterName;
        if (filterName.equalsIgnoreCase("Threshold")) {
            // TODO: Set threshold
            currentFilter = ImageFilters.threshold(.5f);
        } else if (filterName.equalsIgnoreCase("Gray")) {
            currentFilter = ImageFilters.gray();
        } else if (filterName.equalsIgnoreCase("None")) {
            currentFilter = ImageFilters.identity();
        } else {
            currentFilter = null;
            // Exception?
        }
        if (currentFilter != null) {
            applyFilters();
        }
    }

    /**
     * Rescale the image and apply filters.
     */
    public void applyFilters() {
        // System.out.println("In sensorpanel.updateImage for " +
        // this.getPanelName());
        if ((currentFilter != null) && (source != null)) {
            // Rescale original image to logical size
            rescaler = ImageFilters.scale(
                    (float) width / source.getUnfilteredImage().getWidth(),
                    (float) height / source.getUnfilteredImage().getHeight(),
                    false);
            image = rescaler.filter(source.getUnfilteredImage(), null);
            image = currentFilter.filter(image, null);
            updateSensorValues();
        }
    }

    /**
     * Update the sensor matrix values.
     * 
     * TODO: Compute sensor value based on filter type
     */
    private void updateSensorValues() {
        sensorValues = new double[width * height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int color = image.getRGB(x, y);
                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = color & 0xFF;
                sensorValues[y * width + x] = (red * 0.2126f + green * 0.7152f
                        + blue * 0.0722f) / 255;
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
    @Producible
    public double[] getSensorValues() {
        return sensorValues;
    }

}
