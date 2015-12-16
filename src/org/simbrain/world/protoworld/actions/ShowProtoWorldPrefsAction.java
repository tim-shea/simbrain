/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.world.protoworld.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.propertyeditor.gui.ReflectivePropertyEditor;
import org.simbrain.world.odorworld.OdorWorldPanel;
import org.simbrain.world.protoworld.ProtoWorldComponent;

/**
 * Action for showing world preferences.
 */
public final class ShowProtoWorldPrefsAction extends AbstractAction {

    /** Plot GUI component. */
    private final ProtoWorldComponent component;

    /**
     * Construct a show prefs action
     *
     * @param component parent component
     */
    public ShowProtoWorldPrefsAction(final ProtoWorldComponent component) {
        super("Proto World Preferences...");
        if (component == null) {
            throw new IllegalArgumentException(
                    "Desktop component must not be null");
        }
        this.component = component;
        this.putValue(this.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SMALL_ICON, ResourceManager.getImageIcon("Prefs.png"));
        putValue(SHORT_DESCRIPTION, "Proto world preferences...");
    }

    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent event) {
        ReflectivePropertyEditor editor = new ReflectivePropertyEditor(
                component.getWorld());
        JDialog dialog = editor.getDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
