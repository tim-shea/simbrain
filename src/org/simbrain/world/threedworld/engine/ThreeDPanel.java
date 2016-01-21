package org.simbrain.world.threedworld.engine;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import org.simbrain.world.threedworld.engine.ThreeDView.ViewListener;

public class ThreeDPanel extends Canvas {
    private static final long serialVersionUID = 2582113543119990412L;
    
    private class PanelUpdater implements ViewListener {
        @Override
        public void onUpdate(BufferedImage image) {
            if (destroyNeeded) {
                currentView.removeListener(this);
                return;
            }
            checkVisibilityState();
            if (nextView != currentView) {
                currentView.removeListener(this);
                currentView = nextView;
                currentView.addListener(this);
                reshapeNeeded = true;
            }
            if (reshapeNeeded) {
                updateDimensions();
                reshapeNeeded = false;
            } else {
                drawImage(image);
            }
        }
        
        @Override
        public void onResize() {
        	setReshapeNeeded();
        }
    }
    
    private ThreeDView nextView;
    private ThreeDView currentView;
    private BufferStrategy strategy;
    private AffineTransformOp transformOp;
    private boolean hasNativePeer = false;
    private boolean reshapeNeeded = true;
    private boolean destroyNeeded = false;
    private boolean resizeView = false;
    private final Object lock = new Object();
    
    public ThreeDPanel() {
        super();
        setIgnoreRepaint(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                setReshapeNeeded();
            }
        });
    }
    
    public ThreeDView getView() {
        return currentView;
    }
    
    public void setView(ThreeDView value, boolean resize) {
        if (currentView == null) {
            currentView = value;
            currentView.addListener(new PanelUpdater());
        }
        resizeView = resize;
        reshapeNeeded = true;
        nextView = value;
    }
    
    public void setReshapeNeeded() {
        synchronized (lock) {
            reshapeNeeded = true;
        }
    }
    
    private void updateDimensions() {
        synchronized (lock) {
            AffineTransform transform;
            if (resizeView) {
                currentView.resize(getWidth(), getHeight());
                transform = new AffineTransform();
            } else {
                transform = AffineTransform.getScaleInstance(
                        (double)getWidth() / currentView.getWidth(),
                        (double)getHeight() / currentView.getHeight());
            }
            transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        }
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
    
    public boolean checkVisibilityState() {
        if (!hasNativePeer) {
            if (strategy != null) {
                strategy = null;
            }
            return false;
        }
        return isShowing();
    }
    
    public void drawImage(BufferedImage image) {
        synchronized (lock) {
            if (strategy == null) {
                try {
                    createBufferStrategy(1, new BufferCapabilities(
                            new ImageCapabilities(true), new ImageCapabilities(true),
                            BufferCapabilities.FlipContents.UNDEFINED));
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
                strategy = getBufferStrategy();
            }
            do {
                do {
                    Graphics2D graphics2d = (Graphics2D)strategy.getDrawGraphics();
                    if (graphics2d == null)
                        return;
                    graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                    graphics2d.drawImage(image, transformOp, 0, 0);
                    graphics2d.dispose();
                    strategy.show();
                } while (strategy.contentsRestored());
            } while (strategy.contentsLost());
        }
    }
    
    public void destroy() {
        if (currentView == null)
            return;
        else 
            destroyNeeded = true;
    }
}
