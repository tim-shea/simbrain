package org.simbrain.world.imageworld;

import org.simbrain.util.Producible;

/**
 * ImageCoupling encapsulates the adapter code needed to provide RGB and value
 * couplings to the image data produced by an ImageSource.
 * 
 * @author Tim Shea
 */
public class ImageCoupling implements ImageSourceListener {
    private int width;
    private int height;
    private double[] valueImage;
    private double[] redImage;
    private double[] greenImage;
    private double[] blueImage;

    /**
     * Construct a new ImageCoupling.
     *
     * @param source The ImageSource to provide producers for.
     */
    public ImageCoupling(ImageSource source) {
        source.addListener(this);
        onResize(source);
    }

    /**
     * @return Returns an array of doubles for the value (brightness) of each pixel in the image.
     */
    @Producible
    public double[] getValueImage() {
        return valueImage;
    }

    /**
     * @return Returns an array of doubles for the red component of each pixel in the image.
     */
    @Producible
    public double[] getRedImage() {
        return redImage;
    }

    /**
     * @return Returns an array of doubles for the green component of each pixel in the image.
     */
    @Producible
    public double[] getGreenImage() {
        return greenImage;
    }

    /**
     * @return Returns an array of doubles for the blue component of each pixel in the image.
     */
    @Producible
    public double[] getBlueImage() {
        return blueImage;
    }

    @Override
    public void onImage(ImageSource source) {
        if (source.isEnabled()) {
            for (int x = 0; x < source.getWidth(); ++x) {
                for (int y = 0; y < source.getHeight(); ++y) {
                    int color = source.getCurrentImage().getRGB(x, y);
                    int red = (color >>> 16) & 0xFF;
                    int green = (color >>> 8) & 0xFF;
                    int blue = color & 0xFF;
                    valueImage[y * width + x] = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
                    redImage[y * width + x] = red / 255.0;
                    greenImage[y * width + x] = green / 255.0;
                    blueImage[y * width + x] = blue / 255.0;
                }
            }
        }
    }

    @Override
    public void onResize(ImageSource source) {
        width = source.getWidth();
        height = source.getHeight();
        valueImage = new double[width * height];
        redImage = new double[width * height];
        greenImage = new double[width * height];
        blueImage = new double[width * height];
    }
}
