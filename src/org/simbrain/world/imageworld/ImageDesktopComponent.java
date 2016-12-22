package org.simbrain.world.imageworld;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.SFileChooser;
import org.simbrain.util.genericframe.GenericFrame;
import org.simbrain.util.widgets.ShowHelpAction;
import org.simbrain.workspace.component_actions.CloseAction;
import org.simbrain.workspace.component_actions.OpenAction;
import org.simbrain.workspace.component_actions.SaveAction;
import org.simbrain.workspace.component_actions.SaveAsAction;
import org.simbrain.workspace.gui.GuiComponent;

/**
 * GUI Interface for vision world.
 * @author Tim Shea, Jeff Yoshimi
 */
public class ImageDesktopComponent extends GuiComponent<ImageWorldComponent> {
    private static final long serialVersionUID = 9019927108869839191L;

    /** Combo box for selecting which sensor matrix to view. */
    private JComboBox<SensorMatrix> sensorMatrixCombo = new JComboBox<SensorMatrix>();

    /** The image world component . */
    private final ImageWorldComponent component;

    /** Main toolbar */
    private final JToolBar toolbar = new JToolBar();

    /**
     * Construct a new ImageDesktopComponent GUI.
     * @param frame The frame in which to place GUI elements.
     * @param imageWorldComponent The ImageWorldComponent to interact with.
     */
    public ImageDesktopComponent(GenericFrame frame,
            ImageWorldComponent imageWorldComponent) {
        super(frame, imageWorldComponent);
        this.component = imageWorldComponent;

        JMenuBar menuBar = new JMenuBar();
        this.setUpMenus(menuBar);
        frame.setJMenuBar(menuBar);

        toolbar.add(sensorMatrixCombo);
        sensorMatrixCombo.setToolTipText(
                "Which sensor panel to view (all are always available for coupling)");
        updateComboBox();
        sensorMatrixCombo.setSelectedIndex(0);
        sensorMatrixCombo.setMaximumSize(new java.awt.Dimension(200, 100));
        sensorMatrixCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SensorMatrix selectedSensorMatrix = (SensorMatrix) sensorMatrixCombo.getSelectedItem();
                if (selectedSensorMatrix != null) {
                    component.getImageWorld().setCurrentSensorMatrix(selectedSensorMatrix);
                }
            }
        });

        JButton addSensorMatrix = new JButton(
                ResourceManager.getImageIcon("plus.png"));
        addSensorMatrix.setToolTipText("Add sensor matrix...");
        addSensorMatrix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.getImageWorld().showSensorMatrixDialog();
            }
        });
        toolbar.add(addSensorMatrix);

        JButton deleteSensorMatrix = new JButton(
                ResourceManager.getImageIcon("minus.png"));
        deleteSensorMatrix.setToolTipText("Delete sensor matrix");
        deleteSensorMatrix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SensorMatrix selectedSensorMatrix = (SensorMatrix) sensorMatrixCombo.getSelectedItem();
                component.getImageWorld().removeSensorMatrix(selectedSensorMatrix);
            }
        });
        toolbar.add(deleteSensorMatrix);

        // Lay out the whole component
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(imageWorldComponent.getImageWorld().getImagePanel(), BorderLayout.CENTER);
        imageWorldComponent.getImageWorld().getImagePanel().setPreferredSize(new Dimension(640, 480));

        component.getImageWorld().addListener(this::updateComboBox);
    }

    /** Reset the combo box for the sensor panels. */
    private void updateComboBox() {
        sensorMatrixCombo.removeAllItems();
        SensorMatrix selectedSensorMatrix = component.getImageWorld().getCurrentSensorMatrix();
        for (SensorMatrix sensorMatrix : component.getImageWorld().getSensorMatrices()) {
            sensorMatrixCombo.addItem(sensorMatrix);
            if (sensorMatrix.equals(selectedSensorMatrix)) {
                sensorMatrixCombo.setSelectedItem(sensorMatrix);
            }
        }
    }

    @Override
    protected void closing() { }

    /**
     * Sets up menus.
     * @param menuBar the bar to add menus
     */
    public void setUpMenus(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File  ");
        menuBar.add(fileMenu);

        JMenuItem loadImage = new JMenuItem("Load Image...");
        loadImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SFileChooser fileChooser = new SFileChooser(
                        System.getProperty("user.home"),
                        "Select an image to load");
                fileChooser.setUseImagePreview(true);
                File file = fileChooser.showOpenDialog();
                if (file != null) {
                    try {
                        component.getImageWorld().loadImage(file.toString());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Unabled to load file: " + file.toString());
                    }
                }
            }
        });
        fileMenu.add(loadImage);

        fileMenu.addSeparator();
        // TODO: Serialization not hooked up yet
        fileMenu.add(new OpenAction(this));
        fileMenu.add(new SaveAction(this));
        fileMenu.add(new SaveAsAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new CloseAction(this.getWorkspaceComponent()));

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("World Help");
        menuBar.add(helpMenu);
        ShowHelpAction helpAction = new ShowHelpAction(
                "Pages/Worlds/ImageWorld/ImageWorld.html"); // TODO: Create docs
        helpItem.setAction(helpAction);
        helpMenu.add(helpItem);
    }
}
