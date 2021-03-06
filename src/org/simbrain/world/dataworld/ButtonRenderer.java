/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.world.dataworld;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * <b>ButtonRenderer</b> is necessary to keep the print buttons of the table rendered correctly.
 *
 * @author rbartley
 */
class ButtonRenderer implements TableCellRenderer {

    /** Default renderer for this button renderer. */
    private TableCellRenderer defaultRenderer;


    /**
     * Create a new button renderer with the specified default
     * renderer.
     *
     * @param defaultRenderer default renderer for this button renderer
     */
    public ButtonRenderer(final TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }


    /** @see TableCellRenderer */
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        if (value instanceof Component) {
            return (Component) value;
        }

        return defaultRenderer.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
    }
}
