package org.simbrain.world.imageworld;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.sun.glass.events.KeyEvent;

/**
 * ImagePanel is a resizable canvas for displaying images from an ImageSource.
 * 
 * @author Tim Shea
 */
public class ImagePanel extends JPanel implements ImageSourceListener {
    private static final long serialVersionUID = 2582113543119990412L;

    private ImageSource nextSource;
    private ImageSource currentSource;
    private BufferStrategy strategy;
    private AffineTransformOp transformOp;
    private boolean hasNativePeer = false;
    private boolean reshapeNeeded = true;
    private boolean destroyNeeded = false;
    private boolean resizeSource = false;
    private final Object lock = new Object();
    private boolean sensorView = false;

    /**
     * Construct a new ImagePanel.
     */
    public ImagePanel() {
        super();
        
        // Working example of a key binding.  Now how to give JME access?
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "moo");
        this.getActionMap().put("moo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Moo");
            }
        });

        // setIgnoreRepaint(true);
        // addComponentListener(new ComponentAdapter() {
        // @Override
        // public void componentResized(ComponentEvent event) {
        // setReshapeNeeded();
        // }
        // });
        repaint();
    }

    /**
     * @return Get the source of images for this panel.
     */
    public ImageSource getImageSource() {
        return currentSource;
    }

    /**
     * Assign the source of the images for this panel.
     * 
     * @param value The new source to use.
     * @param resize Whether to resize the source to match the size of the
     *            panel.
     */
    public void setImageSource(ImageSource value, boolean resize) {
        if (currentSource == null) {
            currentSource = value;
            currentSource.addListener(this);
        }
        resizeSource = resize;
        reshapeNeeded = true;
        nextSource = value;
        repaint();
    }

    /**
     * Set a flag indicating the panel has been resized.
     */
    public void setReshapeNeeded() {
        synchronized (lock) {
            reshapeNeeded = true;
        }
        if (currentSource != null) {
            currentSource.updateImage();
        }
    }

    /**
     * @return Returns whether the ImageSource should be resized to match the
     *         ImagePanel.
     */
    public boolean getResizeSource() {
        return resizeSource;
    }

    /**
     * @param value Assigns whether the ImageSource should be resized to match
     *            the ImagePanel.
     */
    public void setResizeSource(boolean value) {
        resizeSource = value;
    }

    /**
     * Update the dimensions of the panel by either resizing the ImageSource or
     * by creating a scaling transformation to display the ImageSource in the
     * panel.
     */
    private void updateDimensions() {
        synchronized (lock) {
            AffineTransform transform;
            if (resizeSource) {
                currentSource.resize(getWidth(), getHeight());
                transform = new AffineTransform();
            } else {
                transform = AffineTransform.getScaleInstance(
                        (double) getWidth() / currentSource.getWidth(),
                        (double) getHeight() / currentSource.getHeight());
            }
            transformOp = new AffineTransformOp(transform,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        }
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        synchronized (lock) {
            hasNativePeer = true;
        }
        requestFocusInWindow();
    }

    @Override
    public void removeNotify() {
        synchronized (lock) {
            hasNativePeer = false;
        }
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(currentSource == null) {
            return;
        }
        if(sensorView) {
            if (currentSource.getCurrentImage() != null) {
                g.drawImage(currentSource.getCurrentImage(), 0, 0, getWidth(),
                        getHeight(), this);
            }
        } else {
            if (currentSource.getUnfilteredImage() != null) {
                g.drawImage(currentSource.getUnfilteredImage(), 0, 0, getWidth(),
                        getHeight(), this);
            }
        }
    }

    // /**
    // * Draw the contents of a BufferedImage to the canvas.
    // *
    // * @param image The image to draw.
    // */
    // public void drawImage(BufferedImage image) {
    // Graphics2D graphics2d = (Graphics2D) strategy.getDrawGraphics();
    // if (graphics2d == null) {
    // return;
    // }
    // graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING,
    // RenderingHints.VALUE_RENDER_SPEED);
    // graphics2d.drawImage(image, transformOp, 0, 0);
    // graphics2d.dispose();
    // strategy.show();
    // updateScreen();
    // synchronized (lock) {
    //
    // if (!hasNativePeer) {
    // if (strategy != null) {
    // strategy = null;
    // }
    // return;
    // }
    // if (strategy == null) {
    // try {
    // createBufferStrategy(1,
    // new BufferCapabilities(new ImageCapabilities(true),
    // new ImageCapabilities(true),
    // BufferCapabilities.FlipContents.UNDEFINED));
    // } catch (AWTException ex) {
    // ex.printStackTrace();
    // }
    // strategy = getBufferStrategy();
    // }
    // do {
    // do {
    // Graphics2D graphics2d = (Graphics2D) strategy
    // .getDrawGraphics();
    // if (graphics2d == null) {
    // return;
    // }
    // graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING,
    // RenderingHints.VALUE_RENDER_SPEED);
    // graphics2d.drawImage(image, transformOp, 0, 0);
    // graphics2d.dispose();
    // strategy.show();
    // } while (strategy.contentsRestored());
    // } while (strategy.contentsLost());
    // }
    // }

    /**
     * Set a flag to destroy this ImagePanel on the next update.
     */
    public void destroy() {
        if (currentSource == null) {
            return;
        } else {
            destroyNeeded = true;
        }
    }

    @Override
    public void onImage(ImageSource source) {
        if (destroyNeeded) {
            currentSource.removeListener(this);
            return;
        }
        if (nextSource != currentSource) {
            currentSource.removeListener(this);
            currentSource = nextSource;
            currentSource.addListener(this);
            reshapeNeeded = true;
        }
        if (!getImageSource().isEnabled()) {
            return;
        }
        if (reshapeNeeded) {
            reshapeNeeded = false;
            updateDimensions();
        }
        repaint();
        // drawImage(source.getCurrentImage());
    }

    @Override
    public void onResize(ImageSource source) {
        if (!resizeSource) {
            setReshapeNeeded();
        }
        onImage(source);
        repaint();
    }

    /**
     * Toggles a view of the filtered image (the "sensor view") and the unfiltered image
     *
     * @param sensorView true for sensorView; false for image view
     */
    public void setSensorView(boolean sensorView) {
        this.sensorView = sensorView;
        repaint();
    }
}
