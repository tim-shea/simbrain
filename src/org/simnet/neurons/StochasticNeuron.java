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
package org.simnet.neurons;

import org.simnet.interfaces.Neuron;


/**
 * <b>StochasticNeuron</b>.
 */
public class StochasticNeuron extends Neuron {
    /** Probability the neuron will fire. */
    private double firingProbability = .5;

    /**
     * Default constructor needed for external calls which create neurons then  set their parameters.
     */
    public StochasticNeuron() {
    }

    /**
     * @return Time type.
     */
    public int getTimeType() {
        return org.simnet.interfaces.Network.DISCRETE;
    }

    /**
     * This constructor is used when creating a neuron of one type from another neuron of another type Only values
     * common to different types of neuron are copied.
     * @param n Neuron to be made type.
     */
    public StochasticNeuron(final Neuron n) {
        super(n);
    }

    /**
     * @return duplicate StochasticNeuron (used, e.g., in copy/paste).
     */
    public Neuron duplicate() {
        StochasticNeuron sn = new StochasticNeuron();
        sn = (StochasticNeuron) super.duplicate(sn);
        sn.setFiringProbability(getFiringProbability());

        return sn;
    }

    /**
     * Updates neuron.
     */
    public void update() {
        double rand = Math.random();

        if (rand > firingProbability) {
            setBuffer(lowerBound);
        } else {
            setBuffer(upperBound);
        }
    }

    /**
     * @return Returns the firingProbability.
     */
    public double getFiringProbability() {
        return firingProbability;
    }

    /**
     * @param firingProbability The firingProbability to set.
     */
    public void setFiringProbability(final double firingProbability) {
        this.firingProbability = firingProbability;
    }

    /**
     * @return Name of neuron type.
     */
    public static String getName() {
        return "Stochastic";
    }
}
