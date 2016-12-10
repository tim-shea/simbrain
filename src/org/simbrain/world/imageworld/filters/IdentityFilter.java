package org.simbrain.world.imageworld.filters;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

//TOOD: 
public class IdentityFilter extends ImageFilter {

    public static final String NAME = "None";

    private AffineTransformOp filter;
    
    public IdentityFilter() {
        AffineTransform transform = new AffineTransform();
        filter = new AffineTransformOp(transform,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public BufferedImageOp getFilter() {
        return filter;
    }

    //TODO: Allow user to select red, green, or blue to couple to   

    @Override
    public double getValue(BufferedImage image, int x, int y) {
        return this.getOverallValue(image, x, y);
    }

}