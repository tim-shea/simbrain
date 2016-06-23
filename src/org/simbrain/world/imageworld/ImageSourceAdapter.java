package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ImageSourceAdapter provides basic size and filter management for an ImageSource class.
 * @author Tim Shea
 */
public abstract class ImageSourceAdapter implements ImageSource {
    private int width;
    private int height;
    private boolean enabled;
    private BufferedImage currentImage;
    private List<BufferedImageOp> filters = new ArrayList<BufferedImageOp>();
    private List<ImageSourceListener> listeners = new CopyOnWriteArrayList<ImageSourceListener>();

    /**
     * Construct a new ImageSourceAdapter and don't initialize the current image.
     */
    public ImageSourceAdapter() { }

    /**
     * Construct a new ImageSourceAdapater and initialize the current image to the specified size.
     * @param width The width of the source.
     * @param height The height of the source.
     */
    public ImageSourceAdapter(int width, int height) {
        enabled = false;
        resize(width, height);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void updateImage() {
        if (isEnabled()) {
            applyFilters();
            notifyUpdate();
        }
    }

    /**
     * Apply the stack of filters to the current image.
     */
    protected void applyFilters() {
        for (BufferedImageOp imageOp : filters) {
            currentImage = imageOp.filter(currentImage, null);
        }
    }

    /**
     * Notify ImageSourceListeners that an update has occurred.
     */
    protected void notifyUpdate() {
        for (ImageSourceListener listener : listeners) {
            listener.onImage(this);
        }
    }

    @Override
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    /**
     * @param value The image to assign to the current image.
     */
    protected void setCurrentImage(BufferedImage value) {
        currentImage = value;
    }

    @Override
    public void addFilter(BufferedImageOp imageOp) {
        filters.add(imageOp);
    }

    @Override
    public void removeFilter(BufferedImageOp imageOp) {
        filters.remove(imageOp);
    }

    @Override
    public void addListener(ImageSourceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ImageSourceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Assign a width to the source. This does not invoke a resize event and should only
     * be used by child classes.
     * @param value The width of the images produced in pixels.
     */
    protected void setWidth(int value) {
        width = value;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Assign a height to the source. This does not invoke a resize event and should only
     * be used by child classes.
     * @param value The height of the images produced in pixels.
     */
    protected void setHeight(int value) {
        height = value;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        currentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        notifyResize();
    }

    /**
     * Notify ImageSourceListeners that a resize has occurred.
     */
    protected void notifyResize() {
        for (ImageSourceListener listener : listeners) {
            listener.onResize(this);
        }
    }
}
