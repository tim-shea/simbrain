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
package org.simbrain.plot.barchart;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.simbrain.plot.ChartListener;
import org.simbrain.workspace.Consumer2;
import org.simbrain.workspace.WorkspaceComponent;

/**
 * Data for a JFreeChart bar chart.
 */
public class BarChartComponent extends WorkspaceComponent {

    /** Data model. */
    private BarChartModel model;

    /**
     * Create new BarChart Component.
     *
     * @param name chart name
     */
    public BarChartComponent(final String name) {
        super(name);
        model = new BarChartModel();
        model.defaultInit();
        addListener();
    }

    /**
     * Create new BarChart Component from a specified model. Used in
     * deserializing.
     *
     * @param name chart name
     * @param model chart model
     */
    public BarChartComponent(final String name, final BarChartModel model) {
        super(name);
        this.model = model;
        addListener();
    }

    /**
     * Add chart listener to model.
     */
    private void addListener() {

        model.addListener(new ChartListener() {

            @Override
            public void dataSourceAdded(final int index) {
                firePotentialAttributesChanged();
            }

            @Override
            public void dataSourceRemoved(final int index) {
                firePotentialAttributesChanged();
            }

            @Override
            public void chartInitialized(int numSources) {
                // No implementation yet (not used in this component thus far).
            }
        });
    }

    @Override
    public Object getObjectFromKey(String objectKey) {
        return model;
    }

    /**
     * Returns model.
     *
     * @return the model.
     */
    public BarChartModel getModel() {
        return model;
    }

    /**
     * Opens a saved bar chart.
     *
     * @param input stream
     * @param name name of file
     * @param format format
     * @return bar chart component to be opened
     */
    public static BarChartComponent open(final InputStream input,
            final String name, final String format) {
        BarChartModel dataModel = (BarChartModel) BarChartModel.getXStream()
                .fromXML(input);
        return new BarChartComponent(name, dataModel);
    }

    @Override
    public void save(final OutputStream output, final String format) {
        BarChartModel.getXStream().toXML(model, output);
    }

    @Override
    public void save2(final OutputStream output, final String format) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(BarChartModel.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(model, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasChangedSinceLastSave() {
        return false;
    }

    @Override
    public void closing() {
    }

    @Override
    public String getXML() {
        return BarChartModel.getXStream().toXML(model);
    }

    @Override
    public List<Consumer2<?>> getConsumers() {
        List<Consumer2<?>> retList = new ArrayList<>();
        // Vector coupling
        retList.add(WorkspaceComponent.getConsumer(model, "setBars"));
        // Scalar couplings
        for (int i = 0; i < model.getDataset().getColumnCount(); i++) {
            Consumer2<?> consumer = WorkspaceComponent.getConsumer(model,
                    "setValue", i);
            consumer.setDescription("Bar " + (i + 1));
            retList.add(consumer);
        }
        return retList;
    }

}
