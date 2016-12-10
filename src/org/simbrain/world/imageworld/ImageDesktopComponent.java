package org.simbrain.world.imageworld;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import org.simbrain.world.imageworld.ImageWorld.WorldListener;

/**
 * GUI Interface for vision world.
 *
 * @author Tim Shea
 */
public class ImageDesktopComponent extends GuiComponent<ImageWorldComponent> {

    private static final long serialVersionUID = 9019927108869839191L;

    /** Combo box for selecting which sensor matrix to view. */
    private JComboBox<SensorMatrix> cbSensorPanel = new JComboBox<SensorMatrix>();

    /** The image world component . */
    private final ImageWorldComponent component;

    /** Main toolbar. */
    private final JToolBar toolbar = new JToolBar();

    /**
     * Construct a new ImageDesktopComponent GUI.
     * 
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

        // Combo box
        toolbar.add(cbSensorPanel);
        cbSensorPanel.setToolTipText(
                "Which sensor panel to view (all are always available for coupling)");
        updateComboBox();
        cbSensorPanel.setSelectedIndex(0);
        cbSensorPanel.setMaximumSize(new java.awt.Dimension(200, 100));
        cbSensorPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.getImageWorld().setCurrentSensorMatrix(
                        (SensorMatrix) cbSensorPanel.getSelectedItem());
            }

        });

        JButton addSensorMatrix = new JButton(
                ResourceManager.getImageIcon("plus.png"));
        addSensorMatrix.setToolTipText("Add sensor matrix...");
        addSensorMatrix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.getImageWorld().addSensorMatrix();
            }
        });
        toolbar.add(addSensorMatrix);

        JButton deleteSensorMatrix = new JButton(
                ResourceManager.getImageIcon("minus.png"));
        deleteSensorMatrix.setToolTipText("Delete sensor matrix");
        deleteSensorMatrix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.getImageWorld().removeSensorMatrix(
                        (SensorMatrix) cbSensorPanel.getSelectedItem());
            }
        });
        toolbar.add(deleteSensorMatrix);

        JButton editSensorMatrix = new JButton(
                ResourceManager.getImageIcon("Properties.png"));
        editSensorMatrix.setToolTipText("Edit sensor matrix...");
        editSensorMatrix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.getImageWorld().editSensorMatrix(
                        (SensorMatrix) cbSensorPanel.getSelectedItem());
            }
        });
        toolbar.add(editSensorMatrix);

        // Lay out the whole component
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(imageWorldComponent.getImageWorld().getImagePanel(),
                BorderLayout.CENTER);

        // Resize display image when panel is resized
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                imageWorldComponent.getImageWorld().getImagePanel().rescale(
                        e.getComponent().getWidth(),
                        e.getComponent().getHeight());
            }

        });

        /**
         * Update the main combo box when sensor matrices are added or removed. 
         */
        component.getImageWorld().addListener(new WorldListener() {
            @Override
            public void sensorMatricesUpdated() {
                updateComboBox();
                cbSensorPanel.setSelectedIndex(imageWorldComponent
                        .getImageWorld().getSensorMatrices().size() - 1);
            }

            @Override
            public void sensorMatrixUpdated(SensorMatrix changed) {
                updateComboBox();
                cbSensorPanel.setSelectedItem(changed);
            }
        });

    }

    /**
     * Reset the combo box for the sensor panels.
     */
    private void updateComboBox() {
        cbSensorPanel.removeAllItems();
        for (SensorMatrix sp : component.getImageWorld().getSensorMatrices()) {
            cbSensorPanel.addItem(sp);
        }
    }

    @Override
    protected void closing() {
    }

    /**
     * Sets up menus.
     * 
     * @param menuBar
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
                    component.getImageWorld().loadImage(file.toString());
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
