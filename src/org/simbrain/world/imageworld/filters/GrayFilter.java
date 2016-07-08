package org.simbrain.world.imageworld.filters;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;

public class GrayFilter extends ImageFilter {
    
    public static final String NAME = "Gray";
    
    public ColorConvertOp filter;
    
    public GrayFilter() {
        ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        filter =  new ColorConvertOp(gray, null);
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public BufferedImageOp getFilter() {
        return filter;
    }

    @Override
    public double getValue(BufferedImage image, int x, int y) {
        return getOverallValue(image,x,y);
    }
  
}