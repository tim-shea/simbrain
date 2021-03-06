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
package org.simbrain.network.dialog.network;

import javax.swing.JTextField;

import org.simbrain.network.NetworkPanel;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;
import org.simnet.layouts.LayersLayout;
import org.simnet.networks.Backprop;


/**
 * <b>BackpropDialog</b> is a dialog box for creating backprop networks.
 */
public class BackpropDialog extends StandardDialog {

    /** Main panel. */
    private LabelledItemPanel mainPanel = new LabelledItemPanel();

    /** Number of input units. */
    private JTextField numberOfInputUnits = new JTextField();

    /** Number of hidden units. */
    private JTextField numberOfHiddenUnits = new JTextField();

    /** Number of output units. */
    private JTextField numberOfOutputUnits = new JTextField();

    /** Reference to network panel. */
    private NetworkPanel networkPanel;

    /**
     * Default constructor.
     *
     * @param np Network panel.
     */
    public BackpropDialog(final NetworkPanel np) {
        init();
        networkPanel = np;
    }

    /**
     * This method initialises the components on the panel.
     */
    private void init() {
        //Initialize Dialog
        setTitle("New Backprop Network");

        fillFieldValues();

        numberOfHiddenUnits.setColumns(3);

        //Set up grapics panel
        mainPanel.addItem("Number of Input Units", numberOfInputUnits);
        mainPanel.addItem("Number of Hidden Units", numberOfHiddenUnits);
        mainPanel.addItem("Number of Output Units", numberOfOutputUnits);

        setContentPane(mainPanel);
    }

    /**
     * Called when dialog closes.
     */
    protected void closeDialogOk() {
      LayersLayout layout = new LayersLayout(40, 40, LayersLayout.HORIZONTAL);
      layout.setInitialLocation(networkPanel.getLastClickedPosition());
      int inputs = Integer.parseInt(numberOfInputUnits.getText());
      int hidden = Integer.parseInt(numberOfHiddenUnits.getText());
      int outputs = Integer.parseInt(numberOfOutputUnits.getText());
      Backprop backprop = new Backprop(inputs, hidden, outputs, layout);
      networkPanel.getNetwork().addNetwork(backprop);
      networkPanel.repaint();
      super.closeDialogOk();
    }

    /**
     * Populate fields with current data.
     */
    private void fillFieldValues() {
        Backprop bp = new Backprop();
        numberOfInputUnits.setText(Integer.toString(bp.getNInputs()));
        numberOfHiddenUnits.setText(Integer.toString(bp.getNHidden()));
        numberOfOutputUnits.setText(Integer.toString(bp.getNOutputs()));
    }

}
