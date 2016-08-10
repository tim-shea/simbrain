package org.simbrain.simulation;

import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JInternalFrame;

import org.simbrain.util.LabelledItemPanel;

/**
 * Simple stack of buttons and other actions, used to control a simulation.
 *
 * @author Jeff Yoshimi
 *
 */
public class ControlPanel extends LabelledItemPanel {

    JInternalFrame internalFrame;

    // TODO: Add a version that does not use runnables

    public void addButton(String name, Runnable task) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            Executors.newSingleThreadExecutor().execute(task);
        });
        this.addItem("", button);
        internalFrame.pack();
    }

    /**
     * Utility for setting up a control panel in an internal frame.
     *
     * @param sim reference to parent simulation
     * @param name title to display in panel frame
     * @param x x coordinate of frame
     * @param y y coordinate of frame
     *
     * @return the internal frame
     */
    public static ControlPanel makePanel(Simulation sim, String name, int x,
            int y) {
        ControlPanel panel = new ControlPanel();
        panel.internalFrame = new JInternalFrame(name, true, true);
        // Set up Frame
        panel.internalFrame.setLocation(x, y);
        panel.internalFrame.getContentPane().add(panel);
        panel.internalFrame.setVisible(true);
        panel.internalFrame.pack();
        sim.getDesktop().addInternalFrame(panel.internalFrame);
        return panel;
    }
}
