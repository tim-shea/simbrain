package org.simbrain.simulation;

import java.util.concurrent.Executors;

import javax.swing.JButton;

import org.simbrain.util.LabelledItemPanel;

public class ControlPanel extends LabelledItemPanel {

    //TODO: Add a version that does not use runnables
    
    public void addButton(String name, Runnable task) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            Executors.newSingleThreadExecutor().execute(task);
        });
        this.addItem("", button);

    }
}
