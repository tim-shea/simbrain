package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * ImageSource produces BufferedImages periodically, can apply an arbitrary
 * chain of filters to the images produced, and can notify listeners of new
 * images and changes to the image size.
 * @author Tim Shea
 */
public interface ImageSource {
    /**
     * @return Returns whether the source will update the image when updateImage
     * is invoked.
     */
    boolean isEnabled();

    /**
     * @param value Assign whether the source should update the image.
     */
    void setEnabled(boolean value);

    /**
     * Tells the source to update the current image, potentially leading to an onImage
     * even for listeners. Concrete ImageSources must specify when and how to invoke this
     * method if it shouldn't be freely called.
     */
    void updateImage();

    /**
     * @return Returns the current image.
     */
    BufferedImage getCurrentImage();

    /**
     * Returns the unflitered image.
     *
     * @return the unfiltered image.
     */
    BufferedImage getUnfilteredImage();

    /**
     * Add a filter to the chain.
     * @param imageOp The filter to apply to all future images.
     */
    void addFilter(BufferedImageOp imageOp);

    /**
     * Remove a filter from the chain.
     * @param imageOp The filter to exclude from future images.
     */
    void removeFilter(BufferedImageOp imageOp);

    /**
     * Add a listener to be notified of new images and resizes.
     * @param listener The listener to add.
     */
    void addListener(ImageSourceListener listener);

    /**
     * Remove a listener to stop being notified.
     * @param listener The listener to remove.
     */
    void removeListener(ImageSourceListener listener);

    /**
     * @return Returns the width of the images produced by the source.
     */
    int getWidth();

    /**
     * @return Returns the height of the images produced by the source.
     */
    int getHeight();

    /**
     * Assign a new width and height for future images produced by the source.
     * @param width The height to use.
     * @param height The width to use.
     */
    void resize(int width, int height);
}
