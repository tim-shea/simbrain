package org.simbrain.world.imageworld;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import javax.swing.JPanel;

/**
 * Hold the image and rescale as parent is resized
 *
 * @author Jeff Yoshimi
 */
public class ImageWorldPanel extends JPanel {

    /** The image to display. */
    private BufferedImage displayImage;
    
    /** The original unscaled image.*/ 
    private BufferedImage baseImage;

    /** The scaling transform to use when resizing panel. */
    private BufferedImageOp scalePanel;

    /**
     * Construct the panel.
     *
     * @param image the image to display
     */
    public ImageWorldPanel(BufferedImage image) {
        super();
        this.displayImage = image;
        this.baseImage = image;
        this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint the panel image, which is the scaled logical image
        super.paintComponent(g);
        g.drawImage(displayImage, 0, 0, getWidth(), getHeight(), this);
    }

    /**
     * Update what image is displayed in the panel.
     *
     * @param newImage the new image.
     * @param height  the height to use for the new image
     * @param width the width to use for the new image
     */
    public void updateImage(BufferedImage newImage, int width, int height) {
        this.baseImage = newImage;
        rescale(width, height);
    }

    /**
     * Resize the visible image to a specified width and height, probably based
     * on a window resize.
     *
     * @param width new width
     * @param height new height
     */
    public void rescale(int width, int height) {
        this.setSize(width, height);
        scalePanel = ImageFilters.scale((float) width / baseImage.getWidth(),
                (float) height  / baseImage.getHeight(), false);
        this.displayImage = scalePanel.filter(baseImage, null);
        repaint();
    }

}
