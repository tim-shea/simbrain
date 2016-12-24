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
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * Add or edit a sensor matrix.
 */
public class SensorMatrixDialog extends StandardDialog {
    private static final long serialVersionUID = 1L;

    /** Parent image world. */
    private ImageWorld world;

    /** Select filter type. */
    private JComboBox<String> filterTypeCombo = new JComboBox<String>(FilterPanel.FILTER_TYPES);

    /** Main dialog panel. */
    private Box mainPanel = Box.createVerticalBox();

    /** Panel for editing the sensor matrix. */
    private LabelledItemPanel sensorMatrixPanel = new LabelledItemPanel();

    /** Text field to edit name. */
    private JTextField nameField = new JTextField();

    /** Panel for editing the ImageFilter. */
    private FilterPanel filterPanel = new FilterPanel();

    /**
     * Construct a new SensorMatrixDialog for selecting parameters of a new
     * SensorMatrix.
     * @param world The ImageWorld which will hold the new SensorMatrix.
     */
    public SensorMatrixDialog(ImageWorld world) {
        init("Create sensor matrix");
        this.world = world;
    }

    /**
     * Initialize default constructor.
     * @param title The title of the dialog.
     */
    private void init(String title) {
        setTitle(title);
        ShowHelpAction helpAction = new ShowHelpAction("Pages/Worlds/ImageWorld/sensorMatrix.html");
        addButton(new JButton(helpAction));
        mainPanel.add(sensorMatrixPanel);
        filterTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterPanel.setFilterType((String) filterTypeCombo.getSelectedItem());
            }
        });
        sensorMatrixPanel.addItem("Name", nameField);
        sensorMatrixPanel.addItem("Filter", filterTypeCombo);
        mainPanel.add(filterPanel);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    protected void closeDialogOk() {
        super.closeDialogOk();
        commitChanges();
    }

    /** Called externally when the dialog is closed, to commit any changes made. */
    public void commitChanges() {
        String name = nameField.getText();
        ImageFilter filter = filterPanel.createFilter(world.getImageSource());
        SensorMatrix matrix = new SensorMatrix(name, filter);
        world.addSensorMatrix(matrix);
    }
}
