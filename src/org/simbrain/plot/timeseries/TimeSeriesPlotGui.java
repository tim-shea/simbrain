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
package org.simbrain.plot.timeseries;

import java.awt.BorderLayout;
import java.awt.Dimension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.simbrain.workspace.gui.GenericFrame;
import org.simbrain.workspace.gui.GuiComponent;

/**
 * Display a TimeSeriesPlot.
 */
public class TimeSeriesPlotGui extends GuiComponent<TimeSeriesPlotComponent> {

    /** The underlying plot component. */
    private final TimeSeriesPlotComponent component;

    /**
     * Construct a time series plot gui.
     * 
     * @param frame parent frame
     * @param component the underlying component
     */
    public TimeSeriesPlotGui(final GenericFrame frame, final TimeSeriesPlotComponent component) {
        super(frame, component);
        this.component = component;
        setPreferredSize(new Dimension(500, 400));
    }

    /**
     * Initializes frame.
     */
    @Override
    public void postAddInit() {
        setLayout(new BorderLayout());
        
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Time series", // Title
            "Iterations", // x-axis Label
            "Value(s)", // y-axis Label
            component.getDataset(), // Dataset
            PlotOrientation.VERTICAL, // Plot Orientation
            true, // Show Legend
            true, // Use tooltips
            false // Configure chart to generate URLs?
        );
        
        ChartPanel panel = new ChartPanel(chart);
        add("Center", panel);
    }

    @Override
    public void closing() {
    }

    @Override
    public void update() {
    }
   
}