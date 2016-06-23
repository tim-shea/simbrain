package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * StaticImageSource allows static images (JPG, BMP, PNG) to be loaded
 * and filtered using the ImageSource interface.
 * @author Tim Shea
 */
public class StaticImageSource extends ImageSourceAdapter {
    private String filename;
    private BufferedImage originalImage;
    private BufferedImageOp scale;

    /**
     * Construct a new StaticImageSource.
     * @param width The width of the new source.
     * @param height The height of the new source.
     */
    public StaticImageSource(int width, int height) {
        setWidth(width);
        setHeight(height);
        setEnabled(true);
        loadImage("");
    }

    /**
     * @return Get the name of the currently loaded image file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Load an image from a file and update the current image.
     * @param filename The file to load.
     */
    public void loadImage(String filename) {
        this.filename = filename;
        if (filename == null || filename.isEmpty()) {
            originalImage = new BufferedImage(
                    getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        } else {
            try {
                originalImage = ImageIO.read(new File(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recalculateScale();
        updateImage();
    }

    /**
     * Update the scaling transform from the original image size to the
     * output size.
     */
    private void recalculateScale() {
        removeFilter(scale);
        if (originalImage.getWidth() == getWidth() && originalImage.getHeight() == getHeight()) {
            scale = ImageFilters.identity();
        } else {
            scale = ImageFilters.scale(
                    (float) getWidth() / originalImage.getWidth(),
                    (float) getHeight() / originalImage.getHeight(),
                    false);
        }
        addFilter(scale);
    }

    @Override
    public void updateImage() {
        if (isEnabled()) {
            setCurrentImage(originalImage);
            super.updateImage();
        }
    }

    @Override
    public void resize(int width, int height) {
        setWidth(width);
        setHeight(height);
        recalculateScale();
        setCurrentImage(originalImage);
        applyFilters();
        notifyResize();
    }
}
