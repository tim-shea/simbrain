package org.simbrain.world.imageworld.dialogs;

import org.simbrain.world.imageworld.filters.IdentityFilter;
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * Panel for creating and editing null filters.
 */
public class NullFilterPanel extends AbstractFilterPanel {
    
    /** The filter being created. */
    private ImageFilter filter;

    /**
     * Constructor for the case where a filter is being created.
     */
    public NullFilterPanel() {
        this.filter = new IdentityFilter();
    }
    
    /**
     * Constructor for the case where an filter is being edited.
     *
     * @param filter the filter being edited
     */
    public NullFilterPanel(ImageFilter filter) {
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
    
