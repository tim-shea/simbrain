package org.simbrain.world.imageworld;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Display images from an ImageSource.
 * @author Jeff Yoshimi, Tim Shea
 */
public class ImagePanel extends JPanel implements ImageSourceListener {
    private static final long serialVersionUID = 1L;

    private BufferedImage currentImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), this);
    }

    @Override
    public void onImage(ImageSource source) {
        currentImage = source.getCurrentImage();
        repaint();
    }

    @Override
    public void onResize(ImageSource source) {
        repaint();
    }
}
