package org.simbrain.world.protoworld;

import java.awt.Canvas;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

public class ProtoWorld {
    
    private SimpleApplication application;
    private Canvas canvas;
    
    public ProtoWorld() {
        AppSettings settings = new AppSettings(true);
        settings.setWidth(400);
        settings.setHeight(400);
        
        application = new SimpleApplication() {
            @Override
            public void simpleInitApp() {
                flyCam.setDragToRotate(true);
            }
        };
        
        application.setPauseOnLostFocus(false);
        application.setSettings(settings);
        application.createCanvas();
        
        JmeCanvasContext context = (JmeCanvasContext) application.getContext();
        canvas = context.getCanvas();
        canvas.setSize(settings.getWidth(), settings.getHeight());
        
        Box box = new Box(1, 1, 1);
        Geometry geometry = new Geometry("Box", box);
        Material material = new Material(application.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Blue);
        geometry.setMaterial(material);
        application.getRootNode().attachChild(geometry);
    }
    
    public void update(int time) {}
    
    public Application getApplication() {
        return application;
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
}
