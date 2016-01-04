package org.simbrain.world.protoworld;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.FlyByCamera;

public class ProtoCamAppState extends AbstractAppState {
    private Application app;
    private FlyByCamera flyCam;
    
    public ProtoCamAppState() {}    
    
    /**
     *  This is called by SimpleApplication during initialize().
     */
    public void setCamera( FlyByCamera cam ) {
        this.flyCam = cam;
    }
    
    public FlyByCamera getCamera() {
        return flyCam;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = app;
        
        if (app.getInputManager() != null) {
            if (flyCam == null) {
                flyCam = new FlyByCamera(app.getCamera());
            }
            flyCam.registerWithInput(app.getInputManager());            
        }               
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        flyCam.setEnabled(enabled);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        if (app.getInputManager() != null) {        
            flyCam.unregisterInput();
        }        
    }
}
