package org.simbrain.world.imageworld.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;
import org.simbrain.util.widgets.ShowHelpAction;
import org.simbrain.world.imageworld.ImageWorld;
import org.simbrain.world.imageworld.SensorMatrix;
import org.simbrain.world.imageworld.filters.GrayFilter;
import org.simbrain.world.imageworld.filters.IdentityFilter;
import org.simbrain.world.imageworld.filters.ThresholdFilter;

/**
 * Add or edit a sensor matrix.
 */
public class SensorMatrixDialog extends StandardDialog {

    /** Parent image world. */
    private ImageWorld world;

    /** SensorMatrix being added / edited. */
    private SensorMatrix sensorMatrix;

    /** True if this is a creation dialog; false if it's for editing. */
    private boolean isCreationDialog;

    /**
     * Select filter type.
     */
    private JComboBox cbFilterType = new JComboBox(SensorMatrix.FILTER_TYPES);

    /** Panel that changes to a specific filter panel. */
    private AbstractFilterPanel currentFilterPanel;

    /** Main dialog panel. */
    private Box mainPanel = Box.createVerticalBox();

    /** Panel for editing the sensor matrix. */
    private LabelledItemPanel sensorMatrixpanel = new LabelledItemPanel();

    /** Text field to edit name. */
    private JTextField nameField = new JTextField();

    /** Text field to edit number of horizontal pixels. */
    private JTextField widthField = new JTextField();

    /** Text field to edit number of vertical pixels . */
    private JTextField heightField = new JTextField();

    /**
     * Dialog constructor for creating
     *
     * @param parent parent image world
     */
    public SensorMatrixDialog(ImageWorld parent) {
        isCreationDialog = true;
        init("Create sensor matrix");
        this.world = parent;
    }

    /**
     * Dialog constructor for editing
     *
     * @param parent parent image world
     * @param matrix the sensor matrix to edit
     */
    public SensorMatrixDialog(ImageWorld parent, SensorMatrix matrix) {
        isCreationDialog = false;
        this.sensorMatrix = matrix;
        this.world = parent;
        init("Edit sensor matrix");
    }

    /**
     * Initialize default constructor.
     */
    private void init(String title) {
        setTitle(title);
        ShowHelpAction helpAction = new ShowHelpAction(
                "Pages/Worlds/ImageWorld/sensorMatrix.html");
        addButton(new JButton(helpAction));
        mainPanel.add(sensorMatrixpanel);

        cbFilterType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilterPanel();
            }
        });

        sensorMatrixpanel.addItem("Name", nameField);
        if(isCreationDialog) {
            sensorMatrixpanel.addItem("Width (in pixels)", widthField);
            sensorMatrixpanel.addItem("Heigh (in pixels)", heightField);            
        }
        sensorMatrixpanel.addItem("Filter", cbFilterType);

        if (isCreationDialog) {
            updateFilterPanel();
        } else {
            initFilterPanel();
        }
        fillFieldValues();
        setContentPane(mainPanel);
    }

    /**
     * Initialize all dialog fields.
     */
    private void fillFieldValues() {

        if (isCreationDialog) {
            // A default sensor matrix
            nameField.setText("Threshold 20x20");
            widthField.setText("20");
            heightField.setText("20");
            cbFilterType.setSelectedIndex(2);
        } else {
            nameField.setText(sensorMatrix.getName());
            widthField.setText("" + sensorMatrix.getWidth());
            heightField.setText("" + sensorMatrix.getHeight());
            cbFilterType
                    .setSelectedItem(sensorMatrix.getFilter().getName());
        }

    }

    /**
     * Initialize the filter panel based on the sensormatrix being edited
     */
    private void initFilterPanel() {
        clearFilterPanel();
        if (sensorMatrix.getFilter().getName() == IdentityFilter.NAME) {
            currentFilterPanel = new NullFilterPanel(
                    (IdentityFilter) sensorMatrix.getFilter());
        } else if (sensorMatrix.getFilter().getName() == GrayFilter.NAME) {
            currentFilterPanel = new GrayFilterPanel(
                    (GrayFilter) sensorMatrix.getFilter());
        } else if (sensorMatrix.getFilter().getName() == ThresholdFilter.NAME) {
            currentFilterPanel = new ThresholdFilterPanel(
                    (ThresholdFilter) sensorMatrix.getFilter());
        }
        mainPanel.add(currentFilterPanel);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Update the filter panel based on the current combo box selection.
     */
    private void updateFilterPanel() {

        clearFilterPanel();
        if (cbFilterType.getSelectedItem() == IdentityFilter.NAME) {
            currentFilterPanel = new NullFilterPanel();
        } else if (cbFilterType.getSelectedItem() == GrayFilter.NAME) {
            currentFilterPanel = new GrayFilterPanel();
        } else if (cbFilterType.getSelectedItem() == ThresholdFilter.NAME) {
            currentFilterPanel = new ThresholdFilterPanel();
        }
        mainPanel.add(currentFilterPanel);
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    protected void closeDialogOk() {
        super.closeDialogOk();
        commitChanges();
    }

    /**
     * Remove current panel, if any.
     */
    private void clearFilterPanel() {
        if (currentFilterPanel != null) {
            mainPanel.remove(currentFilterPanel);
        }
    }

    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public void commitChanges() {
        if (isCreationDialog) {
            sensorMatrix = new SensorMatrix(world.getImageSource());
            sensorMatrix.setWidth(Integer.parseInt(widthField.getText()));
            sensorMatrix.setHeight(Integer.parseInt(heightField.getText()));
        }
        // TODO: It would be nice to only update if changes were made...
        sensorMatrix.setName(nameField.getText());
        currentFilterPanel.commitChanges();
        sensorMatrix.setFilter(currentFilterPanel.getFilter());
        if (isCreationDialog) {
            world.addSensorMatrix(sensorMatrix);
        } else {
            world.fireSensorMatrixUpdated(sensorMatrix);
        }
    }

}
