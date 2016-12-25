package org.simbrain.world.imageworld.filters;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;

import org.simbrain.world.imageworld.ImageSource;
import org.simbrain.world.imageworld.ImageSourceAdapter;
import org.simbrain.world.imageworld.ImageSourceListener;

/**
 * ImageFilter decorates an ImageSource with a color and size transform.
 * @author Jeff Yoshimi, Tim Shea
 */
public class ImageFilter extends ImageSourceAdapter implements ImageSourceListener {
    /** @return a BuffereImageOp which does not alter the input image */
    protected static BufferedImageOp getIdentityOp() {
        return new AffineTransformOp(new AffineTransform(),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }

    /** @return a BufferedImageOp which converts the input image to a grayscale colorspace */
    protected static BufferedImageOp getGrayOp() {
        return new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    }

    /**
     * @param x the horizontal scaling factor
     * @param y the vertical scaling factor
     * @param smooth whether the output image should receive bilinear smoothing
     * @return a BufferedImageOp which applies a scaling transform to input images
     */
    protected static BufferedImageOp getScaleOp(float x, float y, boolean smooth) {
        AffineTransform transform = AffineTransform.getScaleInstance(x, y);
        int interpolation = smooth ? AffineTransformOp.TYPE_BILINEAR
                : AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        return new AffineTransformOp(transform, interpolation);
    }

    private final ImageSource wrappedSource;
    private final BufferedImageOp colorOp;
    private final int width;
    private final int height;
    private BufferedImageOp scaleOp;

    /**
     * Construct a new ImageFilter.
     * @param source the ImageSource to be filtered
     * @param colorOp the color filter to apply
     * @param width the width of the output image
     * @param height the height of the output image
     */
    public ImageFilter(ImageSource source, BufferedImageOp colorOp, int width, int height) {
        wrappedSource = source;
        this.colorOp = colorOp;
        this.width = width;
        this.height = height;
        wrappedSource.addListener(this);
    }

    public BufferedImageOp getColorOp() {
        return colorOp;
    }

    /** @return the current unfiltered image */
    public BufferedImage getUnfilteredImage() {
        return wrappedSource.getCurrentImage();
    }

    /** @param value the BufferedImageOp to assign */
    protected void setScaleOp(BufferedImageOp value) {
        scaleOp = value;
    }

    @Override
    public void onImage(ImageSource source) {
        BufferedImage image = scaleOp.filter(source.getCurrentImage(), null);
        image = colorOp.filter(image, null);
        setCurrentImage(image);
    }

    @Override
    public void onResize(ImageSource source) {
        float scaleX = (float) width / source.getWidth();
        float scaleY = (float) height / source.getHeight();
        scaleOp = getScaleOp(scaleX, scaleY, true);
        notifyResize();
    }
}
