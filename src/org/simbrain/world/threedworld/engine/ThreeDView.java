package org.simbrain.world.threedworld.engine;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreeDView implements SceneProcessor {
    public interface ViewListener {
        public void onUpdate(BufferedImage image);
        
        public void onResize();
    }
    
    private int width;
    private int height;
    private boolean active;
    private boolean attachAsMain = false;
    private FrameBuffer frameBuffer;
    private ByteBuffer byteBuffer;
    private IntBuffer intBuffer;
    private RenderManager renderManager;
    private BufferedImage source;
    private BufferedImage destination;
    private BufferedImageOp flip;
    private List<BufferedImageOp> imageOps = new ArrayList<BufferedImageOp>();
    private List<ViewPort> viewPorts = new ArrayList<ViewPort>();
    private List<ViewListener> listeners = new CopyOnWriteArrayList<ViewListener>();
    
    public ThreeDView(int width, int height) {
        this.width = width;
        this.height = height;
        active = false;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean value) {
        active = value;
    }
    
    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }
    
    public IntBuffer getBuffer() {
        return intBuffer;
    }
    
    public BufferedImage getImage() {
        return destination;
    }
    
    public void addFilter(BufferedImageOp imageOp) {
        imageOps.add(imageOp);
    }
    
    public void removeFilter(BufferedImageOp imageOp) {
        imageOps.remove(imageOp);
    }
    
    public void addListener(ViewListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ViewListener listener) {
        listeners.remove(listener);
    }
    
    protected void drawFrame() {
        byteBuffer.clear();
        renderManager.getRenderer().readFrameBuffer(frameBuffer, byteBuffer);
        Screenshots.convertScreenShot2(intBuffer, source);
        destination = flip.filter(source, null);
        for (BufferedImageOp imageOp : imageOps) {
            destination = imageOp.filter(destination, null);
        }
    }
    
    public void attach(boolean overrideMainFramebuffer, ViewPort... viewPorts) {
        if (this.viewPorts.size() > 0)
            throw new RuntimeException("ThreeDView already attached to ViewPort");
        this.viewPorts.addAll(Arrays.asList(viewPorts));
        this.viewPorts.get(this.viewPorts.size() - 1).addProcessor(this);
        this.attachAsMain = overrideMainFramebuffer;
    }
    
    @Override
    public void initialize(RenderManager renderManager, ViewPort viewPort) {
        if (this.renderManager == null) {
            this.renderManager = renderManager;
            resize(width, height);
            setActive(true);
        }
    }
    
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        byteBuffer = BufferUtils.ensureLargeEnough(byteBuffer, width * height * 4);
        intBuffer = byteBuffer.asIntBuffer();
        frameBuffer = new FrameBuffer(width, height, 1);
        frameBuffer.setDepthBuffer(Format.Depth);
        frameBuffer.setColorBuffer(Format.RGB8);
        if (attachAsMain) {
            renderManager.getRenderer().setMainFrameBufferOverride(frameBuffer);
        }
        source = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        flip = ImageFilters.flip(height);
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
        for (ViewListener listener : listeners)
        	listener.onResize();
    }
    
    @Override
    public boolean isInitialized() {
        return frameBuffer != null;
    }
    
    @Override
    public void preFrame(float tpf) {}
    
    @Override
    public void postQueue(RenderQueue renderQueue) {}
    
    @Override
    public void postFrame(FrameBuffer out) {
        if (!attachAsMain && out != frameBuffer)
            throw new IllegalStateException("FrameBuffer was changed");
        if (isActive())
            drawFrame();
        for (ViewListener listener : listeners)
            listener.onUpdate(getImage());
    }
    
    @Override
    public void reshape(ViewPort viewPort, int width, int height) {}
    
    @Override
    public void cleanup() {
        active = false;
        if (attachAsMain) {
            renderManager.getRenderer().setMainFrameBufferOverride(null);
        }
        for (ViewPort viewPort : viewPorts) {
            if (!attachAsMain)
                viewPort.setOutputFrameBuffer(null);
            viewPort.getProcessors().remove(this);
        }
    }
}
