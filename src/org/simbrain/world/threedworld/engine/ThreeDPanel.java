package org.simbrain.world.threedworld.engine;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.ImageCapabilities;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.simbrain.world.threedworld.engine.ThreeDView.ViewListener;

public class ThreeDPanel extends Canvas {
    private class ComponentListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent event) {
            synchronized (lock) {
                int newWidth = Math.max(getWidth(), 1);
                int newHeight = Math.max(getHeight(), 1);
                if (width != newWidth || height != newHeight) {
                    width = newWidth;
                    height = newHeight;
                    reshapeNeeded = true;
                    System.out.println("EDT: componentResized " + width + ", " + height);
                }
            }
        }
    }
    
    private class PanelUpdater implements ViewListener {
        @Override
        public void postUpdate(BufferedImage image) {
            checkVisibilityState();
            if (nextView != currentView) {
                currentView.removeListener(this);
                currentView = nextView;
                currentView.addListener(this);
                // TODO: Check for resize
            }
            if (reshapeNeeded) {
                reshapeInThread();
                reshapeNeeded = false;
            } else
                drawImage(image);
        }
    }
    
    private ThreeDView nextView;
    private ThreeDView currentView;
    private BufferStrategy strategy;
    private AffineTransformOp transformOp;
    private AtomicBoolean hasNativePeer = new AtomicBoolean(false);
    private AtomicBoolean showing = new AtomicBoolean(false);
    private AtomicBoolean repaintRequest = new AtomicBoolean(false);
    private int width;
    private int height;
    private boolean reshapeNeeded = true;
    private final Object lock = new Object();
    
    public ThreeDPanel(int width, int height) {
        this.width = width;
        this.height = height;
        setIgnoreRepaint(true);
        addComponentListener(new ComponentListener());
    }
    
    public ThreeDView getView() {
        return currentView;
    }
    
    public void setView(ThreeDView value) {
        if (currentView == null) {
            currentView = value;
            currentView.addListener(new PanelUpdater());
        }
        nextView = value;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        synchronized (lock) {
            hasNativePeer.set(true);
            System.out.println("EDT: addNotify");
        }
        requestFocusInWindow();
    }
    
    @Override
    public void removeNotify() {
        synchronized (lock) {
            hasNativePeer.set(false);
            System.out.println("EDT: removeNotify");
        }
        super.removeNotify();
    }
    
    @Override
    public void paint(Graphics graphics) {
        /*
        Graphics2D graphics2d = (Graphics2D)graphics;
        synchronized (lock) {
            graphics2d.drawImage(view.getImage(), transformOp, 0, 0);
        }
        */
    }
    
    public boolean checkVisibilityState() {
        if (!hasNativePeer.get()) {
            if (strategy != null) {
                strategy = null;
                System.out.println("OGL: Not visible. Destroy strategy.");
            }
            return false;
        }
        boolean currentShowing = isShowing();
        if (showing.getAndSet(currentShowing) != currentShowing) {
            if (currentShowing) {
                System.out.println("OGL: Enter showing state.");
            } else {
                System.out.println("OGL: Exit showing state.");
            }
        }
        return currentShowing;
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
                System.out.println("OGL: Visible. Create strategy.");
            }
            do {
                do {
                    Graphics2D graphics2d = (Graphics2D)strategy.getDrawGraphics();
                    if (graphics2d == null) {
                        System.out.println("OGL: DrawGraphics was null.");
                        return;
                    }
                    graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                    graphics2d.drawImage(image, transformOp, 0, 0);
                    graphics2d.dispose();
                    strategy.show();
                } while (strategy.contentsRestored());
            } while (strategy.contentsLost());
        }
    }
    
    public boolean isActiveDrawing() {
        return showing.get();
    }
    
    private void reshapeInThread() {
        currentView.resize(width, height);
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -height);
        transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    }
    
    @Override
    public void invalidate() {
        repaintRequest.set(true);
    }
}
