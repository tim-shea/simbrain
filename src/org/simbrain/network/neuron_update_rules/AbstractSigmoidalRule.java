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
package org.simbrain.network.neuron_update_rules;

import org.simbrain.network.core.NeuronUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.BiasedUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.BoundedUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.DifferentiableUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.InvertibleUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.NoisyUpdateRule;
import org.simbrain.util.math.SquashingFunction;
import org.simbrain.util.randomizer.Randomizer;

/**
 * 
 * An abstract superclass for discrete and continuous time sigmodial squashing
 * function based update rules containing methods and variables common to
 * both.
 * 
 * @author Zach Tosi
 *
 */
public abstract class AbstractSigmoidalRule extends NeuronUpdateRule implements
    BiasedUpdateRule, DifferentiableUpdateRule, InvertibleUpdateRule,
    BoundedUpdateRule, NoisyUpdateRule {

    /**
     * The default squashing function, informs the default upper and lower
     * bounds.
     */
    public static final SquashingFunction DEFAULT_SQUASHING_FUNCTION =
        SquashingFunction.LOGISTIC;

    /** The Default upper bound. */
    public static final double DEFAULT_UPPER_BOUND =
        DEFAULT_SQUASHING_FUNCTION.getDefaultUpperBound();

    /** The Default lower bound. */
    public static final double DEFAULT_LOWER_BOUND =
        DEFAULT_SQUASHING_FUNCTION.getDefaultLowerBound();

    /** Current implementation. */
    protected SquashingFunction sFunction;

    /** Bias. */
    protected double bias;

    /** Slope. */
    protected double slope = 1;

    /** Noise dialog. */
    protected Randomizer noiseGenerator = new Randomizer();

    /** Adds noise to neuron. */
    protected boolean addNoise;

    /** The upper bound of the activity if clipping is used. */
    protected double upperBound = DEFAULT_UPPER_BOUND;

    /** The lower bound of the activity if clipping is used. */
    protected double lowerBound = DEFAULT_LOWER_BOUND;

    /**
     * 
     */
    public AbstractSigmoidalRule() {
        super();
        sFunction = DEFAULT_SQUASHING_FUNCTION;
    }

    /**
     * 
     * @param sFunction
     */
    public AbstractSigmoidalRule(SquashingFunction sFunction) {
        super();
        this.sFunction = sFunction;
        setUpperBound(sFunction.getDefaultUpperBound());
        setLowerBound(sFunction.getDefaultLowerBound());
    }

    /**
     * @return the type
     */
    public SquashingFunction getSquashFunctionType() {
        if (sFunction == null) {
            sFunction = SquashingFunction.LOGISTIC; // TODO: Explain (backwards compat)
        }
        return sFunction;
    }

    /**
     * @param type
     *            the type to set
     */
    public final void setSquashFunctionType(SquashingFunction type) {
        this.sFunction = type;
        setUpperBound(type.getDefaultUpperBound());
        setLowerBound(type.getDefaultLowerBound());
    }
    
    /**
     * Integer getter for use in property editors.
     *
     * @return the index for the squashing function
     */
    public final int getSquashFunctionInt() {
        return  sFunction.ordinal();
    }

    /**
     * Integer setter for use by property editors.
     *
     * @param typeIndex index of the squashing function
     */
    public void setSquashFunctionInt(Integer typeIndex) {
        this.sFunction = SquashingFunction.values()[typeIndex];
    }

    /**
     * @return Returns the inflectionPointSlope.
     */
    public double getSlope() {
        return slope;
    }

    /**
     * @param inflectionPointSlope
     *            The inflectionPointSlope to set.
     */
    public void setSlope(final double inflectionPointSlope) {
        this.slope = inflectionPointSlope;
    }

    /**
     * @return Returns the noise.
     */
    public Randomizer getNoiseGenerator() {
        return noiseGenerator;
    }

    /**
     * @param noise
     *            The noise to set.
     */
    public void setNoiseGenerator(final Randomizer noise) {
        this.noiseGenerator = noise;
    }

    /**
     * @return Returns the addNoise.
     */
    public boolean getAddNoise() {
        return addNoise;
    }

    /**
     * @param addNoise
     *            The addNoise to set.
     */
    public void setAddNoise(final boolean addNoise) {
        this.addNoise = addNoise;
    }

    /**
     * Copy the overlapping bits of the rule for subclasses.
     * 
     * @param sr the sigmoid rule to copy
     * @return the copy.
     */
    protected final AbstractSigmoidalRule baseDeepCopy(
            final AbstractSigmoidalRule sr) {
        sr.setBias(getBias());
        sr.setSquashFunctionType(getSquashFunctionType());
        sr.setSlope(getSlope());
        sr.setAddNoise(getAddNoise());
        sr.noiseGenerator = new Randomizer(noiseGenerator);
        return sr;
    }

    
    @Override
    public final double getUpperBound() {
        return upperBound;
    }

    @Override
    public final double getLowerBound() {
        return lowerBound;
    }

    @Override
    public final void setUpperBound(final double ceiling) {
        this.upperBound = ceiling;
    }

    @Override
    public final void setLowerBound(final double floor) {
        this.lowerBound = floor;
    }

    @Override
    public final double getInverse(final double val) {
        double up = getUpperBound();
        double lw = getLowerBound();
        double diff = up - lw;
        return sFunction.inverseVal(val, up, lw, diff);
    }

    @Override
    public final double getBias() {
        return bias;
    }

    /**
     * @param inflectionY
     *            The inflectionY to set.
     */
    @Override
    public final void setBias(final double inflectionY) {
        this.bias = inflectionY;
    }

}
