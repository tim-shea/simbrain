package org.simbrain.world.imageworld.dialogs;

import javax.swing.JTextField;

import org.simbrain.world.imageworld.filters.ImageFilter;
import org.simbrain.world.imageworld.filters.ThresholdFilter;

/**
 * Panel for creating and editing threshold filters.
 */
public class ThresholdFilterPanel extends AbstractFilterPanel {
    
    /** The filter to edit. */
    private ThresholdFilter filter;

    /** Text field to edit threshold. */
    private JTextField thresholdField = new JTextField();

    /**
     * Constructor for the case where the filter is being created.
     */
    public ThresholdFilterPanel() {
        this.filter = new ThresholdFilter();
        initPanel();
    }
    
    /**
     * Constructor for the case where the filter is being edited.
     *
     * @param filter the filter to edit
     */
    public ThresholdFilterPanel(ThresholdFilter filter) {
        this.filter = filter;
        initPanel();
    }
    
    /**
     * Initialize the panel
     */
    private void initPanel() {
       addItem("Threshold", thresholdField);         
       fillFieldValues();
    }

    @Override
    public void commitChanges() {
        filter.setThreshold(Float.parseFloat(thresholdField.getText()));
    }
    
    /**
     * Set the values of all fields.
     */
    private void fillFieldValues() {
        thresholdField.setText("" + filter.getThreshold());
    }
    
    @Override
    public ImageFilter getFilter() {
        return filter;
    }
}
