package org.simbrain.world.imageworld;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.simbrain.util.SFileChooser;
import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.workspace.gui.GuiComponent;

/**
 * ImageDesktopComponent construct the GUI for interacting with an ImageWorld.
 *
 * @author Tim Shea
 */
public class ImageDesktopComponent extends GuiComponent<ImageWorldComponent> {
    
    private static final long serialVersionUID = 9019927108869839191L;

    /**
     * Construct a new ImageDesktopComponent GUI.
     * @param frame The frame in which to place GUI elements.
     * @param workspaceComponent The ImageWorldComponent to interact with.
     */
    public ImageDesktopComponent(GenericFrame frame, ImageWorldComponent workspaceComponent) {
        super(frame, workspaceComponent);
        JMenuBar menubar = new JMenuBar();
        StaticImageSource source = workspaceComponent.getImageSource();
        menubar.add(new JButton(new AbstractAction("Load Image") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                SFileChooser fileChooser = new SFileChooser(
                        System.getProperty("user.home"), "Select an image to load");
                fileChooser.setUseImagePreview(true);
                File file = fileChooser.showOpenDialog();
                source.loadImage(file.toString());
            }
        }));
        JToggleButton sensorViewButton = new JToggleButton("Sensor View");
        sensorViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                workspaceComponent.getImagePanel().setSensorView(sensorViewButton.isSelected());
            }
            
        });

        menubar.add(sensorViewButton);
        JTextField widthField = new JTextField(Integer.toString(source.getWidth()));
        JTextField heightField = new JTextField(Integer.toString(source.getHeight()));
        source.addListener(new ImageSourceListener() {
            @Override
            public void onImage(ImageSource source) { }

            @Override
            public void onResize(ImageSource source) {
                widthField.setText(Integer.toString(source.getWidth()));
                heightField.setText(Integer.toString(source.getHeight()));
            }
        });
        menubar.add(new JLabel("Width"));
        menubar.add(widthField);
        menubar.add(new JLabel("Height"));
        menubar.add(heightField);
        
        JButton resizeButton = new JButton(new AbstractAction("Resize") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                source.resize(Integer.parseInt(widthField.getText()),
                        Integer.parseInt(heightField.getText()));
            }
        });
        menubar.add(resizeButton);
        menubar.add(new JCheckBox(new AbstractAction("Auto") {
            {
                putValue(SHORT_DESCRIPTION, "Resize the image source to fit the panel.");
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                JCheckBox source = (JCheckBox) evt.getSource();
                workspaceComponent.getImagePanel().setResizeSource(source.isSelected());
                widthField.setEditable(!source.isSelected());
                heightField.setEditable(!source.isSelected());
                resizeButton.setEnabled(!source.isSelected());
            }
        }));
        frame.setJMenuBar(menubar);
        setLayout(new BorderLayout());
        add(workspaceComponent.getImagePanel(), BorderLayout.CENTER);
    }

    @Override
    protected void closing() { }
}
