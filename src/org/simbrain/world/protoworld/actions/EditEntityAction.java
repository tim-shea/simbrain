package org.simbrain.world.protoworld.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import org.simbrain.util.propertyeditor.gui.ReflectivePropertyEditor;
import org.simbrain.world.protoworld.ProtoWorld;

public class EditEntityAction implements ActionListener {
    public class TestEntity {
        private String name = "test";
        private String model = "test";
        
        public String getName() { return name; }
        public void setName(String value) { name = value; }
        public String getModel() { return model; }
        public void setModel(String value) { model = value; }
    }
    
    private ProtoWorld world;
    private TestEntity testEntity = new TestEntity();
    
    public EditEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        ReflectivePropertyEditor editor = new ReflectivePropertyEditor(world.getSelectedEntity());
        JDialog dialog = editor.getDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
