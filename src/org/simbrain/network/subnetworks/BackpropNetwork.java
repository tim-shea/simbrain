/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.network.subnetworks;

import java.awt.geom.Point2D;

import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.Group;
import org.simbrain.network.neuron_update_rules.LinearRule;
import org.simbrain.network.trainers.Trainable;
import org.simbrain.network.trainers.TrainingSet;

/**
 * Backprop network.
 *
 * @author Jeff Yoshimi
 */
public class BackpropNetwork extends FeedForward implements Trainable {

    /**
     * Training set.
     */
    private final TrainingSet trainingSet = new TrainingSet();

    /**
     * Construct a new backprop network.
     *
     * @param network reference to root network
     * @param nodesPerLayer number of layers
     * @param initialPosition initial position in network
     */
    public BackpropNetwork(Network network, int[] nodesPerLayer,
            Point2D initialPosition) {
        super(network, nodesPerLayer, initialPosition,
                new Neuron(network, new LinearRule()));
        setLabel("Backprop");
    }

    /**
     * Construct a new backprop network with a default location of (0,0).
     *
     * @param network reference to root network
     * @param nodesPerLayer number of layers
     */
    public BackpropNetwork(Network network, int[] nodesPerLayer) {
        super(network, nodesPerLayer, new Point2D.Double(1, 1),
                new Neuron(network, new LinearRule()));
        setLabel("Backprop");
    }
    
    @Override
    public TrainingSet getTrainingSet() {
        return trainingSet;
    }

    @Override
    public Group getNetwork() {
        return this;
    }

    @Override
    public void initNetwork() {
    }
}
