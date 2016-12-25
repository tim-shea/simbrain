package org.simbrain.world.imageworld;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * StaticImageSource allows static images (JPG, BMP, PNG) to be loaded and
 * filtered using the ImageSource interface.
 * @author Tim Shea
 */
public class StaticImageSource extends ImageSourceAdapter {
    private String filename;

    /**
     * Construct a new StaticImageSource.
     */
    public StaticImageSource() {
        super();
        filename = "";
    }

    public StaticImageSource(String filename, BufferedImage currentImage) {
        super(currentImage);
        this.filename = filename;
    }

    /**
     * @return Get the name of the currently loaded image file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Load an image from a file and update the current image.
     * @param filename the file to load.
     * @throws IOException upon failure to read the requested file
     */
    public void loadImage(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            this.filename = "";
            setCurrentImage(new BufferedImage(10, 10,
                    BufferedImage.TYPE_INT_RGB));
        } else {
            BufferedImage image = ImageIO.read(new File(filename));
            this.filename = filename;
            setCurrentImage(image);
        }
    }

    /**
     * Create image from  a provided image icon.
     * @param imageIcon the image icon
     */
    public void loadImage(ImageIcon imageIcon) {
        filename = "";
        BufferedImage image = new BufferedImage(imageIcon.getIconWidth(),
                imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(imageIcon.getImage(), 0, 0, null);
        g.dispose();
        setCurrentImage(image);
    }
}
