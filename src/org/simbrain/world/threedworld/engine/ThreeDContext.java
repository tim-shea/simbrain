package org.simbrain.world.threedworld.engine;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.TouchInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.awt.AwtMouseInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.renderer.Renderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;

public class ThreeDContext implements JmeContext {
    protected JmeContext actualContext;
    protected AppSettings settings = new AppSettings(true);
    protected SystemListener listener;
    protected ArrayList<ThreeDPanel> panels = new ArrayList<ThreeDPanel>();
    protected ThreeDPanel inputSource;
    
    protected AwtMouseInput mouseInput = new AwtMouseInput() {
        @Override
        public void mousePressed(MouseEvent event) {
            MouseEvent flippedEvent = new MouseEvent(
                    event.getComponent(), event.getID(), event.getWhen(), event.getModifiers(),
                    event.getX(), event.getComponent().getHeight() - event.getY(), 1, false, event.getButton());
            super.mousePressed(flippedEvent);
        }
        
        @Override
        public void mouseReleased(MouseEvent event) {
            MouseEvent flippedEvent = new MouseEvent(
                    event.getComponent(), event.getID(), event.getWhen(), event.getModifiers(),
                    event.getX(), event.getComponent().getHeight() - event.getY(), 1, false, event.getButton());
            super.mouseReleased(flippedEvent);
        }
    };
    protected AwtKeyInput keyInput = new AwtKeyInput();
    
    protected boolean lastThrottleState = false;
    
    private class Listener implements SystemListener {
        public void initialize() {
            initInThread();
        }
        
        public void reshape(int width, int height) {
            throw new IllegalStateException();
        }
        
        public void update() {
            updateInThread();
        }
        
        public void requestClose(boolean esc) {
            // shouldn't happen
            throw new IllegalStateException();
        }
        
        public void gainFocus() {
            // shouldn't happen
            throw new IllegalStateException();
        }
        
        public void loseFocus() {
            // shouldn't happen
            throw new IllegalStateException();
        }
        
        public void handleError(String errorMsg, Throwable t) {
            listener.handleError(errorMsg, t);
        }
        
        public void destroy() {
            destroyInThread();
        }
    }
    
    public void setInputSource(ThreeDPanel panel) {
        if (!panels.contains(panel))
            throw new IllegalArgumentException();
        inputSource = panel;
        mouseInput.setInputSource(panel);
        keyInput.setInputSource(panel);
    }
    
    public Type getType() {
        return Type.OffscreenSurface;
    }
    
    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }
    
    public AppSettings getSettings() {
        return settings;
    }
    
    public Renderer getRenderer() {
        return actualContext.getRenderer();
    }
    
    public MouseInput getMouseInput() {
        return mouseInput;
    }
    
    public KeyInput getKeyInput() {
        return keyInput;
    }
    
    public JoyInput getJoyInput() {
        return null;
    }
    
    public TouchInput getTouchInput() {
        return null;
    }
    
    public Timer getTimer() {
        return actualContext.getTimer();
    }
    
    public boolean isCreated() {
        return actualContext != null && actualContext.isCreated();
    }
    
    public boolean isRenderable() {
        return actualContext != null && actualContext.isRenderable();
    }
    
    public ThreeDContext() {
    }
    
    public ThreeDPanel createPanel() {
        ThreeDPanel panel = new ThreeDPanel();
        panels.add(panel);
        return panel;
    }
    
    private void initInThread() {
        listener.initialize();
    }
    
    private void updateInThread() {
        // Check if throttle required
        boolean needThrottle = true;
        
        for (ThreeDPanel panel : panels) {
            if (panel.isActiveDrawing()) {
                needThrottle = false;
                break;
            }
        }
        
        if (lastThrottleState != needThrottle) {
            lastThrottleState = needThrottle;
            if (lastThrottleState) {
                System.out.println("OGL: Throttling update loop.");
            } else {
                System.out.println("OGL: Ceased throttling update loop.");
            }
        }
        
        if (needThrottle) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        
        listener.update();
    }
    
    private void destroyInThread() {
        listener.destroy();
    }
    
    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
        this.settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        if (actualContext != null) {
            actualContext.setSettings(settings);
        }
    }
    
    public void create(boolean waitFor) {
        if (actualContext != null) {
            throw new IllegalStateException("Already created");
        }
        
        actualContext = JmeSystem.newContext(settings, Type.OffscreenSurface);
        actualContext.setSystemListener(new Listener());
        actualContext.create(waitFor);
    }
    
    public void destroy(boolean waitFor) {
        if (actualContext == null)
            throw new IllegalStateException("Not created");
        
        // destroy parent context
        actualContext.destroy(waitFor);
    }
    
    public void setTitle(String title) {
        // not relevant, ignore
    }
    
    public void setAutoFlushFrames(boolean enabled) {
        // not relevant, ignore
    }
    
    public void restart() {
        // only relevant if changing pixel format.
    }
}
