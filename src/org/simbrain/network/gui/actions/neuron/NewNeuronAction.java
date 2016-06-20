/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
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
package org.simbrain.network.gui.actions.neuron;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.neuron_update_rules.LinearRule;
import org.simbrain.resource.ResourceManager;

/**
 * New neuron action.
 */
public final class NewNeuronAction extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;

    /**
     * Create a new neuron action with the specified network panel.
     *
     * @param networkPanel network panel, must not be null
     */
    public NewNeuronAction(final NetworkPanel networkPanel) {
        super("Add Neuron");

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("AddNeuron.png"));
        putValue(SHORT_DESCRIPTION, "Add or \"put\" new node (p)");

        networkPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke('p'), this);
        networkPanel.getActionMap().put(this, this);

    }

    /** @see AbstractAction 
     * @param event
     */
    public void actionPerformed(final ActionEvent event) {
        networkPanel.addNeuron(new LinearRule());
    }
}