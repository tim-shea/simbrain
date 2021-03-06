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
import org.simnet.interfaces.Synapse;
import org.simnet.util.RandomSource;


/**
 * <b>IACNeuron</b>.
 */
public class IACNeuron extends Neuron {
    /** Neuron decay. */
    private double decay = 0;
    /** Rest. */
    private double rest = 0;
    /** Noise dialog box. */
    private RandomSource noiseGenerator = new RandomSource();
    /** Add noise to the neuron. */
    private boolean addNoise = false;
    /** Clipping. */
    private boolean clipping = true;

    /**
     * Default constructor needed for external calls which create neurons then  set their parameters.
     */
    public IACNeuron() {
    }

    /**
     * This constructor is used when creating a neuron of one type from another neuron of another type Only values
     * common to different types of neuron are copied.
     * @param n Neuron to make type IAC
     */
    public IACNeuron(final Neuron n) {
        super(n);
    }

    /**
     * @return Time type.
     */
    public int getTimeType() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @return duplicate IACNeuron (used, e.g., in copy/paste).
     */
    public Neuron duplicate() {
        IACNeuron iac = new IACNeuron();
        iac.setDecay(getDecay());
        iac.setRest(getRest());
        iac.setClipping(getClipping());
        iac.setAddNoise(getAddNoise());
        iac.noiseGenerator = noiseGenerator.duplicate(noiseGenerator);

        return super.duplicate(iac);
    }

    /**
     * Update the neuron.
     */
    public void update() {
        double val = activation;
        double wtdSum = 0;

        if (fanIn.size() > 0) {
            for (int j = 0; j < fanIn.size(); j++) {
                Synapse w = (Synapse) fanIn.get(j);
                Neuron source = w.getSource();

                if (source.getActivation() > 0) {
                    wtdSum += (w.getStrength() * source.getActivation());
                }
            }
        }

        if (wtdSum > 0) {
            val += ((wtdSum * (upperBound - activation)) - (decay * (activation - rest)));
        } else {
            val += ((wtdSum * (activation - lowerBound)) - (decay * (activation - rest)));
        }

        if (addNoise) {
            val += noiseGenerator.getRandom();
        }

        if (clipping) {
            val = clip(val);
        }

        setBuffer(val);
    }

    /**
     * @return Returns the decay.
     */
    public double getDecay() {
        return decay;
    }

    /**
     * @param decay The decay to set.
     */
    public void setDecay(final double decay) {
        this.decay = decay;
    }

    /**
     * @return Returns the rest.
     */
    public double getRest() {
        return rest;
    }

    /**
     * @param rest The rest to set.
     */
    public void setRest(final double rest) {
        this.rest = rest;
    }

    /**
     * @return Name of neuron type.
     */
    public static String getName() {
        return "IAC";
    }

    /**
     * @return Returns the addNoise.
     */
    public boolean getAddNoise() {
        return addNoise;
    }

    /**
     * @param addNoise The addNoise to set.
     */
    public void setAddNoise(final boolean addNoise) {
        this.addNoise = addNoise;
    }

    /**
     * @return Returns the clipping.
     */
    public boolean getClipping() {
        return clipping;
    }

    /**
     * @param clipping The clipping to set.
     */
    public void setClipping(final boolean clipping) {
        this.clipping = clipping;
    }

    /**
     * @return Returns the noiseGenerator.
     */
    public RandomSource getNoiseGenerator() {
        return noiseGenerator;
    }

    /**
     * @param noiseGenerator The noiseGenerator to set.
     */
    public void setNoiseGenerator(final RandomSource noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
    }
}
