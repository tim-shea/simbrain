package org.simbrain.world.threedworld.engine;

import java.awt.Component;
import java.awt.event.MouseEvent;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.TouchInput;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.awt.AwtMouseInput;
import com.jme3.renderer.Renderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;

public class ThreeDContext implements JmeContext {
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
        
        public void requestClose(boolean escapeIsPressed) {
            throw new IllegalStateException();
        }
        
        public void gainFocus() {
            throw new IllegalStateException();
        }
        
        public void loseFocus() {
            throw new IllegalStateException();
        }
        
        public void handleError(String message, Throwable throwable) {
            listener.handleError(message, throwable);
        }
        
        public void destroy() {
            destroyInThread();
        }
    }
    
    private JmeContext actualContext;
    private AppSettings settings = new AppSettings(true);
    private SystemListener listener;
    private ThreeDPanel panel;
    private AwtMouseInput mouseInput = new AwtMouseInput() {
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
    private AwtKeyInput keyInput = new AwtKeyInput();
    private boolean lastThrottleState = false;
    
    public ThreeDContext() {}
    
    public void setInputSource(Component component) {
        mouseInput.setInputSource(component);
        keyInput.setInputSource(component);
    }
    
    @Override
    public Type getType() {
        return Type.OffscreenSurface;
    }
    
    @Override
    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }
    
    @Override
    public AppSettings getSettings() {
        return settings;
    }
    
    @Override
    public Renderer getRenderer() {
        return actualContext.getRenderer();
    }
    
    @Override
    public MouseInput getMouseInput() {
        return mouseInput;
    }
    
    @Override
    public KeyInput getKeyInput() {
        return keyInput;
    }
    
    @Override
    public JoyInput getJoyInput() {
        return null;
    }
    
    @Override
    public TouchInput getTouchInput() {
        return null;
    }
    
    @Override
    public Timer getTimer() {
        return actualContext.getTimer();
    }
    
    @Override
    public boolean isCreated() {
        return actualContext != null && actualContext.isCreated();
    }
    
    @Override
    public boolean isRenderable() {
        return actualContext != null && actualContext.isRenderable();
    }
    
    public ThreeDPanel createPanel(int width, int height) {
        panel = new ThreeDPanel(width, height);
        return panel;
    }
    
    private void initInThread() {
        listener.initialize();
    }
    
    private void updateInThread() {
        boolean needThrottle = !panel.isActiveDrawing();
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
        actualContext.destroy(waitFor);
    }
    
    public void setTitle(String title) {}
    
    public void setAutoFlushFrames(boolean enabled) {}
    
    public void restart() {}
}
