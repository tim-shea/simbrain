package org.simbrain.world.imageworld.dialogs;

import org.simbrain.world.imageworld.filters.GrayFilter;
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * Panel for creating and editing gray filters.
 */
public class GrayFilterPanel extends AbstractFilterPanel {
    
    /** The filter being created. */
    private ImageFilter filter;

    /**
     * Constructor for the case where a filter is being created.
     */
    public GrayFilterPanel() {
        this.filter = new GrayFilter();
    }
    
    /**
     * Constructor for the case where an filter is being edited.
     *
     * @param filter the filter being edited
     */
    public GrayFilterPanel(ImageFilter filter) {
        this.filter = filter;
    }

    @Override
    public void commitChanges() {
    }
    
    @Override
    public ImageFilter getFilter() {
        return filter;
    }
}
