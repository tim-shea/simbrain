package org.simbrain.world.threedworld.entities;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

public class AgentEditor extends EntityEditor {
    private JPanel sensorTab;
    private JComboBox<String> addSensorComboBox = new JComboBox<String>();
    private Action addSensorAction = new AbstractAction("Add") {
        @Override public void actionPerformed(ActionEvent event) {}
    };
    private JButton addSensorButton = new JButton(addSensorAction);
    private JPanel effectorTab;
    private List<Editor> sensorEditors = new ArrayList<Editor>();
    private List<Editor> effectorEditors = new ArrayList<Editor>();
    private JComboBox<String> addEffectorComboBox = new JComboBox<String>();
    private Action addEffectorAction = new AbstractAction("Add") {
        @Override public void actionPerformed(ActionEvent event) {}
    };
    private JButton addEffectorButton = new JButton(addEffectorAction);
    
    public AgentEditor(Entity entity) {
        super(entity);
        addSensorComboBox.addItem("VisionSensor");
        addSensorButton.setEnabled(false);
        addEffectorComboBox.addItem("WalkingEffector");
        addEffectorAction.setEnabled(false);
    }
    
    @Override
    public JComponent layoutFields() {
        JComponent entityComponent = super.layoutFields();
        getTabbedPane().setTitleAt(0, "Agent");
        
        sensorTab = new JPanel();
        sensorTab.setLayout(new MigLayout("", "[grow]", ""));
        getTabbedPane().addTab("Sensors", sensorTab);
        layoutSensors();
        
        effectorTab = new JPanel();
        effectorTab.setLayout(new MigLayout("", "[grow]", ""));
        getTabbedPane().addTab("Effectors", effectorTab);
        layoutEffectors();
        
        return entityComponent;
    }
    
    private void layoutSensors() {
        sensorTab.add(addSensorComboBox, "growx, split 2");
        sensorTab.add(addSensorButton, "wrap");
        for (Editor editor : sensorEditors) {
            sensorTab.add(editor.layoutFields(), "growx, wrap");
        }
    }
    
    private void layoutEffectors() {
    	effectorTab.add(addEffectorComboBox, "growx, split 2");
    	effectorTab.add(addEffectorButton, "wrap");
        for (Editor editor : effectorEditors) {
            effectorTab.add(editor.layoutFields());
        }
    }
    
    @Override
    public void readValues() {
        super.readValues();
        Agent agent = (Agent)getEntity();
        for (Sensor sensor : agent.getSensors()) {
            Editor sensorEditor = sensor.getEditor();
            sensorEditor.readValues();
            sensorEditors.add(sensorEditor);
        }
        for (Effector effector : agent.getEffectors()) {
            Editor effectorEditor = effector.getEditor();
            effectorEditor.readValues();
            effectorEditors.add(effectorEditor);
        }
    }
    
    @Override
    public void writeValues() {
        super.writeValues();
        for (Editor sensorEditor : sensorEditors)
            sensorEditor.writeValues();
        for (Editor effectorEditor : effectorEditors)
            effectorEditor.writeValues();
    }
    
    @Override
    public void close() {
        super.close();
        for (Editor sensorEditor : sensorEditors)
            sensorEditor.close();
        for (Editor effectorEditor : effectorEditors)
            effectorEditor.close();
    }
}
