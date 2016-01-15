package org.simbrain.world.threedworld.engine;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreeDPanel extends Canvas implements SceneProcessor {
    private boolean attachAsMain = false;
    
    private BufferedImage image;
    private FrameBuffer frameBuffer;
    private ByteBuffer byteBuffer;
    private IntBuffer intBuffer;
    private RenderManager renderManager;
    private ArrayList<ViewPort> viewPorts = new ArrayList<ViewPort>();
    
    private BufferStrategy strategy;
    private AffineTransformOp transformOp;
    private AtomicBoolean hasNativePeer = new AtomicBoolean(false);
    private AtomicBoolean showing = new AtomicBoolean(false);
    private AtomicBoolean repaintRequest = new AtomicBoolean(false);
    
    private int newWidth = 1;
    private int newHeight = 1;
    private AtomicBoolean reshapeNeeded = new AtomicBoolean(false);
    private final Object lock = new Object();
    
    public ThreeDPanel() {
        setIgnoreRepaint(true);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                synchronized (lock) {
                    int newWidth2 = Math.max(getWidth(), 1);
                    int newHeight2 = Math.max(getHeight(), 1);
                    if (newWidth != newWidth2 || newHeight != newHeight2) {
                        newWidth = newWidth2;
                        newHeight = newHeight2;
                        reshapeNeeded.set(true);
                        System.out.println("EDT: componentResized " + newWidth + ", " + newHeight);
                    }
                }
            }
        });
    }
    
    @Override public void addNotify() {
        super.addNotify();
        synchronized (lock) {
            hasNativePeer.set(true);
            System.out.println("EDT: addNotify");
        }
        requestFocusInWindow();
    }
    
    @Override public void removeNotify() {
        synchronized (lock) {
            hasNativePeer.set(false);
            System.out.println("EDT: removeNotify");
        }
        super.removeNotify();
    }
    
    @Override public void paint(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D)graphics;
        synchronized (lock) {
            graphics2d.drawImage(image, transformOp, 0, 0);
        }
    }
    
    public boolean checkVisibilityState() {
        if (!hasNativePeer.get()) {
            if (strategy != null) {
                // strategy.dispose();
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
    
    public void repaintInThread() {
        // Convert screenshot
        byteBuffer.clear();
        renderManager.getRenderer().readFrameBuffer(frameBuffer, byteBuffer);
        synchronized (lock) {
            // All operations on img must be synchronized as it is accessed from EDT
            Screenshots.convertScreenShot2(intBuffer, image);
            repaint();
        }
    }
    
    public void drawFrameInThread() {
        // Convert screenshot
        byteBuffer.clear();
        renderManager.getRenderer().readFrameBuffer(frameBuffer, byteBuffer);
        Screenshots.convertScreenShot2(intBuffer, image);
        synchronized (lock) {
            // All operations on strategy should be synchronized (?)
            if (strategy == null) {
                try {
                    createBufferStrategy(1, new BufferCapabilities(new ImageCapabilities(true), new ImageCapabilities(true),
                            BufferCapabilities.FlipContents.UNDEFINED));
                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
                strategy = getBufferStrategy();
                System.out.println("OGL: Visible. Create strategy.");
            }
            // Draw screenshot
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
    
    public void attachTo(boolean overrideMainFramebuffer, ViewPort... viewPorts) {
        if (this.viewPorts.size() > 0) {
            for (ViewPort viewPort : this.viewPorts) {
                viewPort.setOutputFrameBuffer(null);
            }
            this.viewPorts.get(this.viewPorts.size() - 1).removeProcessor(this);
        }
        this.viewPorts.addAll(Arrays.asList(viewPorts));
        this.viewPorts.get(this.viewPorts.size() - 1).addProcessor(this);
        this.attachAsMain = overrideMainFramebuffer;
    }
    
    public void initialize(RenderManager renderManager, ViewPort viewPort) {
        if (this.renderManager == null) {
            // First time called in OGL thread
            this.renderManager = renderManager;
            reshapeInThread(1, 1);
        }
    }
    
    private void reshapeInThread(int width, int height) {
        byteBuffer = BufferUtils.ensureLargeEnough(byteBuffer, width * height * 4);
        intBuffer = byteBuffer.asIntBuffer();
        frameBuffer = new FrameBuffer(width, height, 1);
        frameBuffer.setDepthBuffer(Format.Depth);
        frameBuffer.setColorBuffer(Format.RGB8);
        if (attachAsMain) {
            renderManager.getRenderer().setMainFrameBufferOverride(frameBuffer);
        }
        synchronized (lock) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        // synchronized (lock){
        // img = (BufferedImage)
        // getGraphicsConfiguration().createCompatibleImage(width, height);
        // }
        AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
        transform.translate(0, -image.getHeight());
        transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        for (ViewPort viewPort : viewPorts) {
            if (!attachAsMain) {
                viewPort.setOutputFrameBuffer(frameBuffer);
            }
            viewPort.getCamera().resize(width, height, true);
            // NOTE: Hack alert. This is done ONLY for custom framebuffers.
            // Main framebuffer should use RenderManager.notifyReshape().
            for (SceneProcessor sceneProcessor : viewPort.getProcessors()) {
                sceneProcessor.reshape(viewPort, width, height);
            }
        }
    }
    
    public boolean isInitialized() {
        return frameBuffer != null;
    }
    
    public void preFrame(float tpf) {
    }
    
    public void postQueue(RenderQueue renderQueue) {
    }
    
    @Override public void invalidate() {
        // For "PaintMode.OnDemand" only.
        repaintRequest.set(true);
    }
    
    public void postFrame(FrameBuffer out) {
        if (!attachAsMain && out != frameBuffer) {
            throw new IllegalStateException("Why did you change the output framebuffer?");
        }
        if (reshapeNeeded.getAndSet(false)) {
            reshapeInThread(newWidth, newHeight);
        } else {
            if (!checkVisibilityState()) {
                return;
            }
            drawFrameInThread();
        }
    }
    
    public void reshape(ViewPort viwePort, int width, int height) {}
    
    public void cleanup() {}
}
