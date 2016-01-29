package org.simbrain.world.threedworld.entities;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class ModelEditor extends EntityEditor {
    private ModelEntity model;
    private JTextField fileNameField = new JTextField(20);
    
    public ModelEditor(ModelEntity model) {
        super(model);
        this.model = model;
        fileNameField.setEditable(false);
    }
    
    @Override
    public JComponent layoutFields() {
        JComponent entityLayout = super.layoutFields();
        
        JPanel modelTab = new JPanel();
        modelTab.setLayout(new MigLayout());
        getTabbedPane().addTab("Model", modelTab);
        
        modelTab.add(new JLabel("File Name"));
        modelTab.add(fileNameField, "split");
        modelTab.add(new JButton("Browse"), "wrap");
        
        return entityLayout;
    }
    
    @Override
    public void readValues() {
        super.readValues();
        fileNameField.setText(model.getFileName());
    }
    
    @Override
    public void writeValues() {
        super.writeValues();
        model.reload(fileNameField.getText());
    }
}