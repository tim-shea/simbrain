package org.simbrain.simulation;

import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;

import org.simbrain.util.LabelledItemPanel;

/**
 * Simple stack of buttons and other actions, used to control a simulation.
 *
 * @author Jeff Yoshimi
 *
 */
public class ControlPanel extends LabelledItemPanel {

    /** The internal frame all components are placed in. */
    private JInternalFrame internalFrame;

    // TODO: Add a version that does not use runnables?

    /**
     * Add a button to the control panel.
     *
     * @param buttonText name for the button itself
     * @param task the task to run when the button is pressed
     */
    public void addButton(String buttonText, Runnable task) {
        addButton("", buttonText, task);
    }

    /**
     * Add a button to the control panel and text next to the button.
     *
     * @param buttonText text for the button itself
     * @param buttonLabel text in the panel to the left of the button
     * @param task the task to run when the button is pressed
     */
    public void addButton(String buttonText, String buttonLabel,
            Runnable task) {
        JButton button = new JButton(buttonLabel);
        button.addActionListener(e -> {
            Executors.newSingleThreadExecutor().execute(task);
        });
        this.addItem(buttonText, button);
        internalFrame.pack();
    }

    /**
     * Add a text field to the panel.
     *
     * @param fieldLabel text in the panel to the left of the field
     * @param initText initial text in the textfield
     * @return the text field
     */
    public JTextField addTextField(String fieldLabel, String initText) {
        JTextField tf = new JTextField(initText); // TODO: Check issue 35
        this.addItem(fieldLabel, tf);
        internalFrame.pack();
        return tf;
    }

    /**
     * Add a checkbox to the panel.
     *
     * @param label text in the panel to the left of the field
     * @param checked whether the box should initially be checked
     * @param task task to run when clicking on the checkbox
     * @return the checkbox
     */
    public JCheckBox addCheckBox(String label, boolean checked, Runnable task) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(checked);
        this.addItem(label, checkBox);
        checkBox.addActionListener(e -> {
            Executors.newSingleThreadExecutor().execute(task);
        });
        internalFrame.pack();
        return checkBox;
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
