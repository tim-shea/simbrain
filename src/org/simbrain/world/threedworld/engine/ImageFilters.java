package org.simbrain.world.threedworld.engine;

import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;

public class ImageFilters {
    public static BufferedImageOp identity() {
        AffineTransform transform = new AffineTransform();
        return new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }
    
    public static BufferedImageOp flip(int height) {
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -height);
        return new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }
    
    public static BufferedImageOp scale(float x, float y, boolean smooth) {
        AffineTransform transform = AffineTransform.getScaleInstance(x, y);
        int interpolation = smooth ? AffineTransformOp.TYPE_BILINEAR :
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        return new AffineTransformOp(transform, interpolation);
    }
    
    public static BufferedImageOp gray() {
        ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        return new ColorConvertOp(gray, null);
    }
    
    public static BufferedImageOp threshold(float thresholdValue) {
        return new BufferedImageOp() {
            @Override public BufferedImage filter(BufferedImage src, BufferedImage dest) {
                if (dest == null) {
                    dest = createCompatibleDestImage(src, null);
                }
                for (int x = 0; x < src.getWidth(); ++x) {
                    for (int y = 0; y < src.getHeight(); ++y) {
                        float luminance = getLuminance(src.getRGB(x, y));
                        dest.setRGB(x, y, luminance >= thresholdValue ? 0x00FFFFFF : 0);
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
            
            @Override public Rectangle2D getBounds2D(BufferedImage src) {
                return null;
            }
            
            @Override public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
                return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            }
            
            @Override public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
                return null;
            }
            
            @Override public RenderingHints getRenderingHints() {
                return null;
            }
        };
    }
}
