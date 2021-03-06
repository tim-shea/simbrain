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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.dialog.network.layout.AbstractLayoutPanel;
import org.simbrain.network.dialog.network.layout.GridLayoutPanel;
import org.simbrain.network.dialog.network.layout.LayoutPanel;
import org.simbrain.network.dialog.network.layout.LineLayoutPanel;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;
import org.simnet.layouts.Layout;
import org.simnet.networks.Hopfield;


/**
 * <b>DiscreteHopfieldDialog</b> is a dialog box for creating discrete hopfield networks.
 */
public class HopfieldDialog extends StandardDialog {

    /** File system seperator. */
    private static final String FS = System.getProperty("file.separator");

    /** Sequential network update order. */
    public static final int SEQUENTIAL = 0;

    /** Random network update order. */
    public static final int RANDOM = 1;

    /** Tabbed pane. */
    private JTabbedPane tabbedPane = new JTabbedPane();

    /** Logic tab panel. */
    private JPanel tabLogic = new JPanel();

    /** Layout tab panel. */
    private JPanel tabLayout = new JPanel();

    /** Logic panel. */
    private LabelledItemPanel logicPanel = new LabelledItemPanel();

    /** Layout panel. */
    private LayoutPanel layoutPanel;

    /** Number of units field. */
    private JTextField numberOfUnits = new JTextField();

    /** Network type combo box. */
    private JComboBox cbUpdateOrder = new JComboBox(new String[] {"Sequential", "Random" });

    /** Open training file button. */
    private JButton trainingFile = new JButton("Set");

    /** Network panel. */
    private NetworkPanel networkPanel;

    /**
     * This method is the default constructor.
     *
     * @param net Network panel
     */
    public HopfieldDialog(final NetworkPanel net) {
        networkPanel = net;
        layoutPanel = new LayoutPanel(this, new AbstractLayoutPanel[]{new GridLayoutPanel(), new LineLayoutPanel()});
        init();
    }

    /**
     * Called when dialog closes.
     */
    protected void closeDialogOk() {
        Layout layout = layoutPanel.getNeuronLayout();
        layout.setInitialLocation(networkPanel.getLastClickedPosition());
        Hopfield hop = new Hopfield(Integer.parseInt(numberOfUnits.getText()), layout);
        networkPanel.getNetwork().addNetwork(hop);
        networkPanel.repaint();
        super.closeDialogOk();
    }

    /**
     * This method initialises the components on the panel.
     */
    private void init() {
        //Initialize Dialog
        setTitle("New Hopfield Network");

        fillFieldValues();

        //Set up grapics panel
        logicPanel.addItem("Update order", cbUpdateOrder);
        logicPanel.addItem("Number of Units", numberOfUnits);
        //logicPanel.addItem("Set training file", trainingFile);

        //Set up tab panel
        tabLogic.add(logicPanel);
        tabLayout.add(layoutPanel);
        tabbedPane.addTab("Logic", logicPanel);
        tabbedPane.addTab("Layout", layoutPanel);
        setContentPane(tabbedPane);
    }

    /**
     * Populate fields with current data.
     */
    public void fillFieldValues() {
        Hopfield dh = new Hopfield();
        numberOfUnits.setText(Integer.toString(dh.getNumUnits()));
    }

    /**
     * @return the update order.
     */
    public int getType() {
        if (cbUpdateOrder.getSelectedIndex() == 0) {
            return SEQUENTIAL;
        } else {
            return RANDOM;
        }
    }
}
