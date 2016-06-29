package org.simbrain.world.imageworld;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * ImageWorldComponent provides a model for building an image processing
 * pipeline and coupling inputs and outputs within a Simbrain workspace.
 *
 * @author Tim Shea
 */
public class ImageWorldComponent extends WorkspaceComponent {
    /**
     * @return A newly constructed xstream for serializing a ThreeDWorld.
     */
    public static XStream getXStream() {
        return new XStream(new DomDriver());
    }

    /**
     * Open a saved ImageWorldComponent from an XML input stream.
     * @param input The input stream to read.
     * @param name The name of the new world component.
     * @param format The format of the input stream. Should be xml.
     * @return A deserialized ImageWorldComponent.
     */
    public static ImageWorldComponent open(InputStream input, String name, String format) {
        return new ImageWorldComponent(name);
    }

    private ImagePanel imagePanel;
    private StaticImageSource imageSource;
    private ImageCoupling imageCoupling;

    /**
     * Construct a new ImageWorldComponent.
     * @param name The name of the component.
     */
    public ImageWorldComponent(String name) {
        super(name);
        addAnnotatedProducers(ImageCoupling.class);
        imagePanel = new ImagePanel();
        imagePanel.setPreferredSize(new Dimension(640, 480));
        imageSource = new StaticImageSource(20, 20);
        imagePanel.setImageSource(imageSource, false);
        imageCoupling = new ImageCoupling(imageSource);
        imageSource.setEnabled(true);
    }

    /**
     * @return Return the image panel which displays the current image for the component.
     */
    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    /**
     * @return Returns the image source for this ImageWorld.
     */
    public StaticImageSource getImageSource() {
        return imageSource;
    }

    @Override
    public void save(OutputStream output, String format) { }

    @Override
    protected void closing() { }

    @Override
    public List<PotentialProducer> getPotentialProducers() {
        return getPotentialProducersForObject("ImageCoupling", imageCoupling);
    }
}
