package org.simbrain.world.threedworld.entities;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.engine.ImageFilters;
import org.simbrain.world.threedworld.engine.ThreeDContext;
import org.simbrain.world.threedworld.engine.ThreeDPanel;
import org.simbrain.world.threedworld.engine.ThreeDView;
import org.simbrain.world.threedworld.engine.ThreeDView.ViewListener;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

public class VisionSensor implements Sensor {
    public static final int MODE_COLOR = 0;
    public static final int MODE_GRAY = 1;
    public static final int MODE_THRESHOLD = 2;
    
    public static AttributeType viewDataAttribute;
    
    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        viewDataAttribute = new AttributeType(component, "VisionSensor", "ViewData", double[].class, true);
        return Arrays.asList(viewDataAttribute);
    }
    
    private Agent agent;
    private Vector3f headOffset = Vector3f.UNIT_Z.clone();
    private Camera camera;
    private ViewPort viewPort;
    private ThreeDView view;
    private double[] viewData;
    private int width = 10;
    private int height = 10;
    private int mode;
    private BufferedImageOp colorFilter;
    
    public VisionSensor(Agent agent) {
        this.agent = agent;
        agent.addSensor(this);
        camera = new Camera(width, height);
        camera.setFrustumPerspective(45f, (float)camera.getWidth() / camera.getHeight(), 1f, 1000f);
        transformCamera();
        viewPort = this.agent.getEngine().getRenderManager().createMainView(this.agent.getName() + "ViewPort", camera);
        viewPort.setClearFlags(true, true, true);
        viewPort.attachScene(this.agent.getEngine().getRootNode());
        view = new ThreeDView(width, height);
        view.attach(false, viewPort);
        setMode(MODE_COLOR);
        viewData = new double[width * height];
        view.addListener(new ViewListener() {
        	@Override
        	public void onUpdate(BufferedImage image) {
	            if (view.isInitialized()) {
	                for (int x = 0; x < width; ++x) {
	                    for (int y = 0; y < height; ++y) {
	                        int value = image.getRGB(x, y) & 0xFF;
	                        viewData[y * width + x] = value / 255.0;
	                    }
	                }
	            }
        	}
        	
        	@Override
        	public void onResize() {}
        });
    }
    
    public Vector3f getHeadOffset() {
        return headOffset;
    }
    
    public void setHeadOffset(Vector3f value) {
        headOffset = value;
        transformCamera();
    }
    
    public Vector3f getSensorLocation() {
        Vector3f offset = getSensorOrientation().mult(headOffset);
        return this.agent.getNode().getWorldTranslation().add(offset);
    }
    
    public Quaternion getSensorOrientation() {
        return this.agent.getNode().getWorldRotation();
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public ViewPort getViewPort() {
        return viewPort;
    }
    
    public ThreeDView getView() {
        return view;
    }
    
    public int getMode() {
        return mode;
    }
    
    public void setMode(int value) {
        if (mode != value) {
            mode = value;
            view.removeFilter(colorFilter);
            switch (mode) {
            case MODE_GRAY:
                colorFilter = ImageFilters.gray();
                break;
            case MODE_THRESHOLD:
                colorFilter = ImageFilters.threshold(0.75f);
                break;
            case MODE_COLOR:
            default:
                colorFilter = ImageFilters.identity();
                break;
            }
            view.addFilter(colorFilter);
        }
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int value) {
        resize(value, height);
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int value) {
        resize(width, value);
    }
    
    public void resize(int width, int height) {
        if (this.width == width && this.height == height)
            return;
        this.width = width;
        this.height = height;
        viewData = new double[width * height];
        camera.resize(width, height, true);
        view.resize(width, height);
    }
    
    public double[] getViewData() {
        return viewData;
    }
    
    public void update(float tpf) {
        transformCamera();
    }
    
    private void transformCamera() {
        camera.setLocation(getSensorLocation());
        camera.setRotation(getSensorOrientation());
    }
    
    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = new ArrayList<PotentialProducer>();
        if (viewDataAttribute.isVisible()) {
            AttributeManager attributeManager = viewDataAttribute.getParentComponent().getAttributeManager();
            PotentialProducer producer = attributeManager.createPotentialProducer(this, "getViewData", double[].class);
            producer.setCustomDescription("VisionSensor:ViewData");
            producers.add(producer);
        }
        return producers;
    }
    
    @Override
    public Editor getEditor() {
        return new SensorEditor(agent, this) {
            private JFormattedTextField widthField = new JFormattedTextField(EditorDialog.integerFormat);
            private JFormattedTextField heightField = new JFormattedTextField(EditorDialog.integerFormat);
            private JFormattedTextField headOffsetXField = new JFormattedTextField(EditorDialog.floatFormat);
            private JFormattedTextField headOffsetYField = new JFormattedTextField(EditorDialog.floatFormat);
            private JFormattedTextField headOffsetZField = new JFormattedTextField(EditorDialog.floatFormat);
            private JComboBox<String> modeComboBox = new JComboBox<String>();
            private ThreeDPanel previewPanel = new ThreeDPanel();
            
            {
                widthField.setColumns(5);
                heightField.setColumns(5);
                headOffsetXField.setColumns(6);
                headOffsetYField.setColumns(6);
                headOffsetZField.setColumns(6);
                modeComboBox.addItem("Color");
                modeComboBox.addItem("Gray");
                modeComboBox.addItem("Threshold");
                previewPanel.setPreferredSize(new Dimension(100, 100));
                previewPanel.setView(getView(), false);
            }
            
            @Override
            public JComponent layoutFields() {
                JComponent sensorComponent = super.layoutFields();
                
                getPanel().add(new JLabel("Resolution"));
                getPanel().add(widthField, "split 2");
                getPanel().add(heightField, "wrap");
                
                getPanel().add(new JLabel("Head Offset"));
                getPanel().add(headOffsetXField, "split 3");
                getPanel().add(headOffsetYField);
                getPanel().add(headOffsetZField, "wrap");
                
                getPanel().add(new JLabel("Mode"));
                getPanel().add(modeComboBox, "wrap");
                
                getPanel().add(previewPanel, "east, gaptop 4px, gapright 4px, gapbottom 4px, gapleft 4px");
                
                return sensorComponent;
            }
            
            @Override
            public void readValues() {
                widthField.setValue(getWidth());
                heightField.setValue(getHeight());
                headOffsetXField.setValue(getHeadOffset().x);
                headOffsetYField.setValue(getHeadOffset().y);
                headOffsetZField.setValue(getHeadOffset().z);
                modeComboBox.setSelectedIndex(mode);
            }
            
            @Override
            public void writeValues() {
                resize(((Number)widthField.getValue()).intValue(),
                        ((Number)heightField.getValue()).intValue());
                headOffset.x = ((Number)headOffsetXField.getValue()).floatValue();
                headOffset.y = ((Number)headOffsetYField.getValue()).floatValue();
                headOffset.z = ((Number)headOffsetZField.getValue()).floatValue();
                setMode(modeComboBox.getSelectedIndex());
            }
            
            @Override
            public void close() {
                previewPanel.destroy();
            }
        };
    }
}