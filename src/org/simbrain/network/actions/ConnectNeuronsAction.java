/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005-2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.network.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.nodes.NeuronNode;
import org.simnet.synapses.ClampedSynapse;

/**
 * Connect neurons action.  Connects a set of source neurons to a set of target neurons.
 */
public final class ConnectNeuronsAction
    extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;

    /** Source neuron. */
    private Collection sourceNeurons;

    /** Target neuron. */
    private Collection targetNeurons;


    /**
     * Create a new connect neurons action.  Connects a set of source neurons to a set of target neurons.
     *
     * @param networkPanel network panel, must not be null
     * @param sourceNeurons NeuronNodes to connect from
     * @param targetNeurons NeuronNodes to connect to
     */
    public ConnectNeuronsAction(final NetworkPanel networkPanel,
                                final Collection sourceNeurons,
                                final Collection targetNeurons) {
        super("Connect");

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;
        this.sourceNeurons = sourceNeurons;
        this.targetNeurons = targetNeurons;
    }


    /** @see AbstractAction */
    public void actionPerformed(final ActionEvent event) {
        for (Iterator i = sourceNeurons.iterator(); i.hasNext(); ) {
            for (Iterator j = targetNeurons.iterator(); j.hasNext(); ) {
                NeuronNode source = (NeuronNode) i.next();
                NeuronNode target = (NeuronNode) j.next();
                networkPanel.getNetwork().addWeight(new ClampedSynapse(source.getNeuron(), target.getNeuron()));
            }
        }
    }
}