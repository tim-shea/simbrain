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
package org.simbrain.network.dialog.neuron;

import javax.swing.JTextField;

import org.simbrain.network.NetworkUtils;
import org.simnet.neurons.LogisticNeuron;


/**
 * <b>LogisticNeuronPanel</b>.
 */
public class LogisticNeuronPanel extends AbstractNeuronPanel {

    /** Growth rate field. */
    private JTextField tfGrowthRate = new JTextField();

    /**
     * Creates an instance of this panel.
     *
     */
    public LogisticNeuronPanel() {
        addItem("Growth rate", tfGrowthRate);

        this.addBottomText("<html>Note: for chaos, growth rates between <p> 3.6 and 4 are recommended </html>");
    }

    /**
     * Populate fields with current data.
     */
    public void fillFieldValues() {
        LogisticNeuron neuronRef = (LogisticNeuron) neuronList.get(0);

        tfGrowthRate.setText(Double.toString(neuronRef.getGrowthRate()));

        //Handle consistency of multiple selections
        if (!NetworkUtils.isConsistent(neuronList, LogisticNeuron.class, "getGrowthRate")) {
            tfGrowthRate.setText(NULL_STRING);
        }
    }

    /**
     * Populate fields with default data.
     */
    public void fillDefaultValues() {
        LogisticNeuron neuronRef = new LogisticNeuron();
        tfGrowthRate.setText(Double.toString(neuronRef.getGrowthRate()));
    }

    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public void commitChanges() {
        for (int i = 0; i < neuronList.size(); i++) {
            LogisticNeuron neuronRef = (LogisticNeuron) neuronList.get(i);

            if (!tfGrowthRate.getText().equals(NULL_STRING)) {
                neuronRef.setGrowthRate(Double.parseDouble(tfGrowthRate.getText()));
            }
        }
    }
}
