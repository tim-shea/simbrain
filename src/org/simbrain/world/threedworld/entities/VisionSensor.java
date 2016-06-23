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
import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.imageworld.ImageSource;
import org.simbrain.world.imageworld.ImageSourceListener;
import org.simbrain.world.imageworld.ImageFilters;
import org.simbrain.world.imageworld.ImagePanel;
import org.simbrain.world.threedworld.engine.ThreeDRenderSource;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

public class VisionSensor implements Sensor {
    private class VisionSensorEditor extends SensorEditor {
        private JFormattedTextField widthField = new JFormattedTextField(EditorDialog.integerFormat);
        private JFormattedTextField heightField = new JFormattedTextField(EditorDialog.integerFormat);
        private JFormattedTextField headOffsetXField = new JFormattedTextField(EditorDialog.floatFormat);
        private JFormattedTextField headOffsetYField = new JFormattedTextField(EditorDialog.floatFormat);
        private JFormattedTextField headOffsetZField = new JFormattedTextField(EditorDialog.floatFormat);
        private JComboBox<String> modeComboBox = new JComboBox<String>();
        private ImagePanel previewPanel = new ImagePanel();

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
            previewPanel.setImageSource(getView(), false);
        }

        private VisionSensorEditor() {
            super(agent, VisionSensor.this);
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
            resize(((Number) widthField.getValue()).intValue(), ((Number) heightField.getValue()).intValue());
            headOffset.x = ((Number) headOffsetXField.getValue()).floatValue();
            headOffset.y = ((Number) headOffsetYField.getValue()).floatValue();
            headOffset.z = ((Number) headOffsetZField.getValue()).floatValue();
            setMode(modeComboBox.getSelectedIndex());
        }

        @Override
        public void close() {
            previewPanel.destroy();
        }
    }

    private class ImageUpdater implements ImageSourceListener {
        @Override
        public void onImage(ImageSource source) {
            if (source.isEnabled()) {
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        int color = source.getCurrentImage().getRGB(x, y);
                        int red = (color >>> 16) & 0xFF;
                        int green = (color >>> 8) & 0xFF;
                        int blue = color & 0xFF;
                        dataByte0[y * width + x] = red / 255.0;
                        dataByte1[y * width + x] = green / 255.0;
                        dataByte2[y * width + x] = blue / 255.0;
                    }
                }
            }
        }

        @Override
        public void onResize(ImageSource source) { }
    }

    public static final int MODE_COLOR = 0;
    public static final int MODE_GRAY = 1;
    public static final int MODE_THRESHOLD = 2;

    public static final float FOV = 45;
    public static final float NEAR_CLIP = 0.1f;
    public static final float FAR_CLIP = 100;

    public static AttributeType valueAttribute;
    public static AttributeType redAttribute;
    public static AttributeType greenAttribute;
    public static AttributeType blueAttribute;

    public static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        valueAttribute = new AttributeType(component, "VisionSensor", "Value", double[].class, true);
        redAttribute = new AttributeType(component, "VisionSensor", "Red", double[].class, true);
        greenAttribute = new AttributeType(component, "VisionSensor", "Green", double[].class, true);
        blueAttribute = new AttributeType(component, "VisionSensor", "Blue", double[].class, true);
        return Arrays.asList(valueAttribute, redAttribute, greenAttribute, blueAttribute);
    }

    private Agent agent;
    private Vector3f headOffset = Vector3f.UNIT_Z.clone();
    private int width = 10;
    private int height = 10;
    private int mode;
    private transient Camera camera;
    private transient ViewPort viewPort;
    private transient ThreeDRenderSource view;
    private transient double[] dataByte0;
    private transient double[] dataByte1;
    private transient double[] dataByte2;
    private transient BufferedImageOp colorFilter;

    public VisionSensor(Agent agent) {
        this.agent = agent;
        agent.addSensor(this);
        initializeView();
        setMode(MODE_COLOR);
    }

    private Object readResolve() {
        initializeView();
        applyMode();
        return this;
    }

    private void initializeView() {
        camera = new Camera(width, height);
        float aspect = (float) camera.getWidth() / camera.getHeight();
        camera.setFrustumPerspective(FOV, aspect, NEAR_CLIP, FAR_CLIP);
        transformCamera();
        viewPort = agent.getEngine().getRenderManager().createMainView(agent.getName() + "ViewPort", camera);
        viewPort.setClearFlags(true, true, true);
        viewPort.attachScene(agent.getEngine().getRootNode());
        view = new ThreeDRenderSource(width, height);
        view.attach(false, viewPort);
        view.addListener(new ImageUpdater());
        dataByte0 = new double[width * height];
        dataByte1 = new double[width * height];
        dataByte2 = new double[width * height];
    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    public Vector3f getHeadOffset() {
        return headOffset;
    }

    public void setHeadOffset(Vector3f value) {
        headOffset = value;
        transformCamera();
    }

    public Vector3f getSensorLocation() {
        Vector3f offset = getSensorRotation().mult(headOffset);
        return agent.getPosition().add(offset);
    }

    public Quaternion getSensorRotation() {
        return agent.getRotation();
    }

    public Camera getCamera() {
        return camera;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    public ThreeDRenderSource getView() {
        return view;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int value) {
        if (mode != value) {
            mode = value;
            applyMode();
        }
    }

    private void applyMode() {
        if (colorFilter != null) {
            view.removeFilter(colorFilter);
        }
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
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;
        dataByte0 = new double[width * height];
        dataByte1 = new double[width * height];
        dataByte2 = new double[width * height];
        camera.resize(width, height, true);
        view.resize(width, height);
    }

    public double[] getValue() {
        return dataByte0;
    }

    public double[] getRed() {
        return dataByte0;
    }

    public double[] getGreen() {
        return dataByte1;
    }

    public double[] getBlue() {
        return dataByte2;
    }

    public void update(float tpf) {
        transformCamera();
    }

    private void transformCamera() {
        if (camera != null) {
            camera.setLocation(getSensorLocation());
            camera.setRotation(getSensorRotation());
        }
    }

    public void delete() {
        view.cleanup();
        viewPort.detachScene(agent.getEngine().getRootNode());
        agent.getEngine().getRenderManager().removeMainView(viewPort);
        agent.removeSensor(this);
    }

    @Override
    public List<PotentialProducer> getPotentialProducers() {
        List<PotentialProducer> producers = new ArrayList<PotentialProducer>();
        if (valueAttribute.isVisible()) {
            producers.add(createProducer(valueAttribute));
        }
        if (redAttribute.isVisible()) {
            producers.add(createProducer(redAttribute));
        }
        if (greenAttribute.isVisible()) {
            producers.add(createProducer(greenAttribute));
        }
        if (blueAttribute.isVisible()) {
            producers.add(createProducer(blueAttribute));
        }
        return producers;
    }

    private PotentialProducer createProducer(AttributeType attribute) {
        AttributeManager attributeManager = attribute.getParentComponent().getAttributeManager();
        PotentialProducer producer = attributeManager.createPotentialProducer(this, "get" + attribute.getMethodName(),
                double[].class);
        producer.setCustomDescription("VisionSensor:" + attribute.getMethodName() + "[" + width + "x" + height + "]");
        return producer;
    }

    @Override
    public SensorEditor getEditor() {
        return new VisionSensorEditor();
    }
}