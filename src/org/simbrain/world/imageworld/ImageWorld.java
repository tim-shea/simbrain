package org.simbrain.world.imageworld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.imageworld.dialogs.SensorMatrixDialog;
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * ImageWorld contains the "logical" contents of this component, the image, and
 * a series of sensor matrices that can be used to convert the image into
 * numbers.
 */
public class ImageWorld {
    /**
     * WorldListener receives notifications when the list of sensor matrices is
     * changed.
     */
    public interface WorldListener {
        /** Called when list of sensor matrices changed. */
        void sensorMatricesUpdated();
    }

    /** The main image source. */
    private StaticImageSource imageSource;

    /** List of sensor matrices associated with this world. */
    private final List<SensorMatrix> sensorMatrices = new ArrayList<SensorMatrix>();

    /** Currently selected sensor matrix. */
    private SensorMatrix currentSensorMatrix;

    /** GUI container for the current image or sensor view. */
    private ImagePanel imagePanel;

    /** List of world listener. */
    private transient List<WorldListener> listeners = new ArrayList<WorldListener>();

    /**
     * Construct the image world.
     */
    public ImageWorld() {
        // Load default image
        imageSource = new StaticImageSource();
        imageSource.loadImage(ResourceManager.getImageIcon("bobcat.jpg"));
        imagePanel = new ImagePanel();

        // Load default sensor matrices
        SensorMatrix unfiltered = new SensorMatrix("Unfiltered", imageSource);
        sensorMatrices.add(unfiltered);

        SensorMatrix gray75x75 = new SensorMatrix("Gray 75x75",
                ImageFilter.grayFilter(imageSource, 75, 75));
        sensorMatrices.add(gray75x75);

        SensorMatrix gray200x200 = new SensorMatrix("Gray 200x200",
                ImageFilter.grayFilter(imageSource, 200, 200));
        sensorMatrices.add(gray200x200);

        SensorMatrix threshold10x10 = new SensorMatrix("Threshold 10x10",
                ImageFilter.thresholdFilter(imageSource, 0.5f, 10, 10));
        sensorMatrices.add(threshold10x10);

        SensorMatrix threshold100x100 = new SensorMatrix("Threshold 100x100",
                ImageFilter.thresholdFilter(imageSource, 0.5f, 100, 100));
        sensorMatrices.add(threshold100x100);

        setCurrentSensorMatrix(sensorMatrices.get(0));
    }

    /**
     * Load image from specified filename.
     * @param filename path to image
     * @throws IOException thrown if the requested file is not available
     */
    public void loadImage(String filename) throws IOException {
        imageSource.loadImage(filename);
    }

    /**
     * Add a new matrix to the list.
     *
     * @param matrix the matrix to add
     */
    public void addSensorMatrix(SensorMatrix matrix) {
        sensorMatrices.add(matrix);
        setCurrentSensorMatrix(matrix);
        fireSensorMatricesUpdated();
    }

    /**
     * Show the Add Sensor Matrix dialog.
     */
    public void showSensorMatrixDialog() {
        SensorMatrixDialog dialog = new SensorMatrixDialog(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Remove the indicated sensor matrix.
     *
     * @param sensorMatrix the sensor matrix to remove
     */
    public void removeSensorMatrix(SensorMatrix sensorMatrix) {
        // Can't remove the "Unfiltered" option
        if (sensorMatrix.getName().equalsIgnoreCase("Unfiltered")) {
            return;
        }
        int dialogResult = JOptionPane.showConfirmDialog(
                null, "Are you sure you want to delete sensor panel \""
                        + sensorMatrix.getName() + "\" ?",
                "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            int index = sensorMatrices.indexOf(sensorMatrix);
            setCurrentSensorMatrix(sensorMatrices.get(index - 1));
            sensorMatrices.remove(sensorMatrix);
            // TODO: This is bad and should be handled in SensorMatrix
            ImageSource source = sensorMatrix.getSource();
            if (source instanceof ImageFilter) {
                imageSource.removeListener((ImageFilter) source);
            }
            sensorMatrix.getSource().removeListener(sensorMatrix);
            fireSensorMatricesUpdated();
        }
    }

    /** @return the image panel */
    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    /** @return Returns the image source for this ImageWorld. */
    public ImageSource getImageSource() {
        return imageSource;
    }

    /** @return the currentSensorPanel */
    public SensorMatrix getCurrentSensorMatrix() {
        return currentSensorMatrix;
    }

    /** @param sensorMatrix the currentSensorMatrix to set */
    public void setCurrentSensorMatrix(SensorMatrix sensorMatrix) {
        if (sensorMatrix == currentSensorMatrix) {
            return;
        }
        if (currentSensorMatrix != null) {
            currentSensorMatrix.getSource().removeListener(imagePanel);
        }
        sensorMatrix.getSource().addListener(imagePanel);
        currentSensorMatrix = sensorMatrix;
    }

    /** @return a list of sensor matrices */
    public List<SensorMatrix> getSensorMatrices() {
        return sensorMatrices;
    }

    /** @param listener the listener to add. */
    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    /** Fire sensor matrices update event. */
    public void fireSensorMatricesUpdated() {
        for (WorldListener listener : listeners) {
            listener.sensorMatricesUpdated();
        }
    }
}
