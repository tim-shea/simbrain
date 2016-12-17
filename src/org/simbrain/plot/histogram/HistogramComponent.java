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
package org.simbrain.plot.histogram;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.simbrain.plot.ChartListener;
import org.simbrain.workspace.Consumer2;
import org.simbrain.workspace.WorkspaceComponent;

/**
 * The Component representation of a histogram. Contains attributes that allow
 * other components to couple to this one.
 */
public class HistogramComponent extends WorkspaceComponent {

    /** Data model. */
    private HistogramModel model;

    /** Neuron group consumer type. */
    // private AttributeType histogramConsumer;

    /**
     * Create new Histogram Component.
     *
     * @param name
     *            chart name
     */
    public HistogramComponent(final String name) {
        super(name);
        model = new HistogramModel(HistogramModel.INITIAL_DATA_SOURCES);
        init();
        addListener();
    }

    /**
     * Create new Histogram Component from a specified model. Used in
     * deserializing.
     *
     * @param name
     *            chart name
     * @param model
     *            chart model
     */
    public HistogramComponent(final String name, final HistogramModel model) {
        super(name);
        this.model = model;
        init();
        addListener();
    }

    /**
     * Initialize component.
     */
    private void init() {

        // histogramConsumer = new AttributeType(this, "Histogram", "getValue",
        // double[].class, true);
        // addConsumerType(histogramConsumer);

    }

    /**
     * Add chart listener to model.
     */
    private void addListener() {

        model.addListener(new ChartListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void dataSourceAdded(final int index) {
                firePotentialAttributesChanged();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void dataSourceRemoved(final int index) {
                firePotentialAttributesChanged();
            }

            /**
             * {@inheritDoc}
             */
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
    public HistogramModel getModel() {
        return model;
    }

    // /**
    // * Opens a saved bar chart.
    // *
    // * @param input
    // * stream
    // * @param name
    // * name of file
    // * @param format
    // * format
    // * @return bar chart component to be opened
    // */
    // public static HistogramComponent open(final InputStream input,
    // final String name, final String format) {
    // HistogramModel dataModel = (HistogramModel) HistogramModel.getXStream()
    // .fromXML(input);
    // return new HistogramComponent(name, dataModel);
    // }

    public static HistogramComponent open(final InputStream input,
            final String name, final String format) {
        // Network newNetwork = (Network) Network.getXStream().fromXML(input);
        // return new NetworkComponent(name, newNetwork);
        Unmarshaller jaxbUnmarshaller;
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(HistogramModel.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            HistogramModel hist = (HistogramModel) jaxbUnmarshaller
                    .unmarshal(input);
            System.out.println(hist);
            return new HistogramComponent(name, hist);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void open2() {
        // TODO: Hard-coded test for now
        File file = new File("jaxb_test.xml");
        Unmarshaller jaxbUnmarshaller;
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(HistogramModel.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            HistogramModel hist = (HistogramModel) jaxbUnmarshaller
                    .unmarshal(file);
            System.out.println(hist);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(final OutputStream output, final String format) {
        HistogramModel.getXStream().toXML(model, output);
    }

    @Override
    public void save2(final OutputStream output, final String format) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(HistogramModel.class);
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
        return HistogramModel.getXStream().toXML(model);
    }

    @Override
    public List<Consumer2<?>> getConsumers() {
        List<Consumer2<?>> retList = new ArrayList<>();
        retList.addAll(WorkspaceComponent.getConsumers(model)); // vector
        // coupling
        // for (int i = 0; i < model.getData().size(); i++) {
        // // String description = "Histogram " + (i + 1);
        // }
        return retList;
    }
    // @Override
    // public List<PotentialConsumer> getPotentialConsumers() {
    // List<PotentialConsumer> returnList = new ArrayList<PotentialConsumer>();
    // if (histogramConsumer.isVisible()) {
    // for (int i = 0; i < model.getData().size(); i++) {
    // String description = "Histogram " + (i + 1);
    // PotentialConsumer consumer = getAttributeManager()
    // .createPotentialConsumer(model, "addData",
    // new Class[] { double[].class, Integer.class },
    // new Object[] { i });
    // consumer.setCustomDescription(description);
    // returnList.add(consumer);
    // }
    // }
    // return returnList;
    // }

}
