package org.simbrain.world.threedworld.entities;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

public class ModelEditor extends EntityEditor {
    private ModelEntity model;
    private JTextField fileNameField = new JTextField(20);
    private JFileChooser fileChooser = new JFileChooser();
    private AbstractAction browseAction = new AbstractAction("Browse") {
        @Override
        public void actionPerformed(ActionEvent event) {
            int result = fileChooser.showOpenDialog(getTabbedPane());
            if (result == JFileChooser.APPROVE_OPTION) {
                fileNameField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    };
    private JButton browseButton = new JButton(browseAction);
    
    public ModelEditor(ModelEntity model) {
        super(model);
        this.model = model;
        fileNameField.setEditable(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("jME3 Geometry", "j3o"));
        //fileChooser.setCurrentDirectory();
    }
    
    @Override
    public JComponent layoutFields() {
        JComponent entityLayout = super.layoutFields();
        
        JPanel modelTab = new JPanel();
        modelTab.setLayout(new MigLayout());
        getTabbedPane().addTab("Model", modelTab);
        
        modelTab.add(new JLabel("File Name"));
        modelTab.add(fileNameField, "split");
        modelTab.add(browseButton, "wrap");
        
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