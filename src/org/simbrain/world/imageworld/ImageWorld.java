package org.simbrain.world.imageworld;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.StandardDialog;
import org.simbrain.util.propertyeditor.gui.ReflectivePropertyEditor;

/**
 * Imageworld contains the "logical" contents of this component, the image, and
 * a series of sensor matrices that can be used to convert the image into
 * numbers.
 * 
 * This is what should be serialized.
 */
public class ImageWorld {

    /** The main image source. */
    private StaticImageSource imageSource;

    /** List of sensor matrices associated with this world. */
    private final List<SensorMatrix> sensorMatrices = new ArrayList<SensorMatrix>();

    /** Current (that is, visible) sensor matrix. */
    private SensorMatrix currentSensorMatrix;

    /** Holder for the current image or sensor view. */
    private ImageWorldPanel imagePanel;
    
    /** List of world listener. */
    private transient List<WorldListener> listeners = new ArrayList<WorldListener>();

    /**
     * Construct the image world.
     */
    public ImageWorld() {

        // Load default image
        imageSource = new StaticImageSource(20, 20);
        imageSource.loadImage(ResourceManager.getImageIcon("bobcat.jpg"));
        imagePanel = new ImageWorldPanel(imageSource.getUnfilteredImage());

        // Load default sensor matrices
        SensorMatrix none = new SensorMatrix("Unfiltered", imageSource);
        none.setWidth(imageSource.getUnfilteredImage().getWidth());
        none.setHeight(imageSource.getUnfilteredImage().getHeight());
        none.setFilter("None");
        sensorMatrices.add(none);

        SensorMatrix gray75x75 = new SensorMatrix("Gray 75x75",
                imageSource);
        gray75x75.setWidth(75);
        gray75x75.setHeight(75);
        gray75x75.setFilter("Gray");
        sensorMatrices.add(gray75x75);
        
        SensorMatrix gray200x200 = new SensorMatrix("Gray 200x200",
                imageSource);
        gray200x200.setWidth(200);
        gray200x200.setHeight(200);
        gray200x200.setFilter("Gray");
        sensorMatrices.add(gray200x200);


        SensorMatrix threshold100x100 = new SensorMatrix("Threshold 100x100",
                imageSource);
        threshold100x100.setWidth(100);
        threshold100x100.setHeight(100);
        threshold100x100.setFilter("Threshold");
        sensorMatrices.add(threshold100x100);

        SensorMatrix threshold200x200 = new SensorMatrix("Threshold 200x200",
                imageSource);
        threshold200x200.setWidth(200);
        threshold200x200.setHeight(200);
        threshold200x200.setFilter("Threshold");
        sensorMatrices.add(threshold200x200);

        currentSensorMatrix = sensorMatrices.get(0);
    }

    /**
     * Load image from specified filename
     *
     * @param fileName path to image
     */
    public void loadImage(String fileName) {
        imageSource.loadImage(fileName);
        BufferedImage newImage = imageSource.getUnfilteredImage();
        imagePanel.updateImage(newImage, newImage.getWidth(),
                newImage.getHeight());
        imagePanel.revalidate();
    }

    /**
     * Add a new sensor panel.
     */
    public void addSensorMatrix() {
        String nameString = JOptionPane.showInputDialog("Sensor Matrix Name:",
                "");
        if (nameString == null) {
            return;
        }
        if (nameString.equals("")) {
            return;
        }
        sensorMatrices.add(new SensorMatrix(nameString, imageSource));

        fireSensorMatricesUpdated();

    }

    /**
     * Remove the indicated sensor matrix.
     */
    public void removeSensorMatrix(SensorMatrix sm) {
        // Can't remove the default "none" matrix which just shows the
        // unfiltered image
        if (sm.getName().equalsIgnoreCase("Unfiltered")) {
            return;
        }
        int dialogResult = JOptionPane.showConfirmDialog(
                null, "Are you sure you want to delete sensor pane \""
                        + sm.getName() + "\" ?",
                "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            sensorMatrices.remove(sm);
        }

        fireSensorMatricesUpdated();
    }

    /**
     * Edit the provided sensor matrix.
     *
     * @param sm matrix to edit
     */
    public void editSensorMatrix(SensorMatrix sm) {

        // TODO: Move this to a separate dialog class

        // Can't edit the unfiltered matrix
        if (sm.getName().equalsIgnoreCase("Unfiltered")) {
            JOptionPane.showMessageDialog(null, "\"Null\" sensor matrix cannot be edited.");
            return;
        }

        JPanel sensorEditor = new JPanel();
        sensorEditor.setLayout(new BoxLayout(sensorEditor, BoxLayout.Y_AXIS));
        ReflectivePropertyEditor editor = new ReflectivePropertyEditor();
        editor.setUseSuperclass(false);
        editor.setObject(sm);
        sensorEditor.add(editor);
        // sensorEditor.add(new JSeparator());

        StandardDialog dialog = new StandardDialog() {
            @Override
            protected void closeDialogOk() {
                super.closeDialogOk();
                editor.commit();
                sm.applyFilters();
                imagePanel.updateImage(currentSensorMatrix.getImage(),
                        imagePanel.getWidth(), imagePanel.getHeight());
                fireSensorMatrixUpdated(sm);
            }
        };
        dialog.setContentPane(sensorEditor);
        dialog.setTitle("Edit " + sm.getName());
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }

    /**
     * Return the image panel.
     *
     * @return the image panel
     */
    public ImageWorldPanel getImagePanel() {
        return imagePanel;
    }

    /**
     * @return Returns the image source for this ImageWorld.
     */
    public StaticImageSource getImageSource() {
        return imageSource;
    }

    /**
     * @return the currentSensorPanel
     */
    public SensorMatrix getCurrentSensorMatrix() {
        return currentSensorMatrix;
    }

    /**
     * @param currentSensorPanel the currentSensorPanel to set
     */
    public void setCurrentSensorPanel(SensorMatrix currentSensorPanel) {
        if (currentSensorPanel != null) {
            this.currentSensorMatrix = currentSensorPanel;
            currentSensorPanel.applyFilters();
            imagePanel.updateImage(currentSensorPanel.getImage(),
                    imagePanel.getWidth(), imagePanel.getHeight());
        }
    }

    /**
     * @return the list of sensor matrices
     */
    public List<SensorMatrix> getSensorMatrices() {
        return sensorMatrices;
    }

    /**
     * Add a world listener.
     *
     * @param listener the listener to add.
     */
    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire sensor matrices update event.
     */
    public void fireSensorMatricesUpdated() {
        for (WorldListener listener : listeners) {
            listener.sensorMatricesUpdated();
        }
    }

    /**
     * Specific sensor matrix updated.
     */
    public void fireSensorMatrixUpdated(SensorMatrix matrix) {
        for (WorldListener listener : listeners) {
            listener.sensorMatrixUpdated(matrix);
        }
    }

    /**
     * WorldListener receives notifications when the list of sensor matrices is
     * changed.
     */
    public interface WorldListener {

        /** Called when list of sensor matrices changed. */
        void sensorMatricesUpdated();

        /** Called when a specific matrix is changed. */
        void sensorMatrixUpdated(SensorMatrix changed);

    }

}
