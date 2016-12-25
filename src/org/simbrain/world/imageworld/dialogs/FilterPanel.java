package org.simbrain.world.imageworld.dialogs;

import javax.swing.JTextField;

import org.simbrain.util.LabelledItemPanel;
import org.simbrain.world.imageworld.ImageSource;
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * GUI panel to edit parameters of an ImageFilter.
 */
public class FilterPanel extends LabelledItemPanel {
    private static final long serialVersionUID = 1L;

    /** Types of filters that this filter panel supports. */
    public static final String[] FILTER_TYPES = {
        "Color",
        "Gray",
        "Threshold",
    };

    private String filterType = FILTER_TYPES[0];
    private JTextField widthField = new JTextField("20");
    private JTextField heightField = new JTextField("20");
    private JTextField thresholdField = new JTextField("0.5");

    /** Construct a new FilterPanel to assign parameters to an ImageFilter. */
    public FilterPanel() {
        setFilterType(FILTER_TYPES[0]);
    }

    /** @param value the filterType to apply to the newly created ImageFilter */
    public void setFilterType(String value) {
        filterType = value;
        removeAll();
        switch (filterType) {
        case "Color":
        case "Gray":
            addItem("Width", widthField);
            addItem("Height", heightField);
            break;
        case "Threshold":
            addItem("Width", widthField);
            addItem("Height", heightField);
            addItem("Threshold", thresholdField);
            break;
        }
    }

    /**
     * Returns the image filter this panel edits.
     * @param source the ImageSource to apply the filter
     * @return the image filter
     */
    public ImageFilter createFilter(ImageSource source) {
        int width = Integer.parseInt(widthField.getText());
        int height = Integer.parseInt(heightField.getText());
        switch (filterType) {
        case "Color":
            return ImageFilter.rgbFilter(source, width, height);
        case "Gray":
            return ImageFilter.grayFilter(source, width, height);
        case "Threshold":
            float threshold = Float.parseFloat(thresholdField.getText());
            return ImageFilter.thresholdFilter(source, threshold, width, height);
        default:
            throw new IllegalArgumentException("Invalid Filter Type");
        }
    }
}
