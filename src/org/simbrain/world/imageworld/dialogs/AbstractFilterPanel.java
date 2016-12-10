package org.simbrain.world.imageworld.dialogs;

import org.simbrain.util.LabelledItemPanel;
import org.simbrain.world.imageworld.filters.ImageFilter;

/**
 * Super class for filter panels.
 */
public abstract class AbstractFilterPanel extends LabelledItemPanel {

    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public abstract void commitChanges();
    
    /** 
     * Returns the image filter this panel edits.
     * 
     * @return the image filter
     */
    public abstract ImageFilter getFilter();


}
