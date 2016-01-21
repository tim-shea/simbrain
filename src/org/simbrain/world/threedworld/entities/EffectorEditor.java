package org.simbrain.world.threedworld.entities;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

public class EffectorEditor implements Editor {
    private Agent agent;
    private Effector effector;
    private JPanel panel;
    private Action deleteAction = new AbstractAction("Delete") {
        @Override public void actionPerformed(ActionEvent event) {
            agent.removeEffector(effector);
        }
    };
    private JButton deleteButton = new JButton(deleteAction);
    
    public EffectorEditor(Agent agent, Effector effector) {
        this.agent = agent;
        this.effector = effector;
        deleteButton.setEnabled(false);
    }
    
    public Agent getAgent() {
        return agent;
    }
    
    public Effector getEffector() {
        return effector;
    }
    
    public JPanel getPanel() {
    	return panel;
    }
    
    @Override
    public JComponent layoutFields() {
        panel = new JPanel();
        panel.setLayout(new MigLayout());
        panel.setBorder(new TitledBorder(effector.getClass().getSimpleName()));
        panel.add(deleteButton, "south");
        return panel;
    }
    
    @Override
    public void readValues() {}
    
    @Override
    public void writeValues() {}
    
    @Override
    public void close() {}
}
