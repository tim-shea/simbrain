package org.simbrain.world.protoworld.actions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.simbrain.util.propertyeditor.gui.ReflectivePropertyEditor;
import org.simbrain.world.protoworld.ProtoEntity;
import org.simbrain.world.protoworld.ProtoWorld;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

public class EditEntityAction implements ActionListener {
    public class FloatTableModel extends DefaultTableModel {
        public FloatTableModel(int rows, int columns) {
            super(rows, columns);
        }
        
        public float getFloatAt(int row, int column) {
            Object value = getValueAt(row, column);
            if (value.getClass() == Float.class)
                return (float)value;
            else
                return Float.parseFloat(value.toString());
        }
        
        @Override
        public void setValueAt(Object value, int row, int column) {
            try {
                Float.parseFloat(value.toString());
                super.setValueAt(value, row, column);
            } catch (NumberFormatException e) {}
        }
    }
    
    private ProtoWorld world;
    
    public EditEntityAction(ProtoWorld world) {
        this.world = world;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        ProtoEntity entity = world.getSelectedEntity();
        if (entity != null) {
            JDialog dialog = new JDialog();
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.5;
            constraints.weighty = 0.2;
            constraints.insets = new Insets(2, 2, 2, 2);
            constraints.fill = GridBagConstraints.BOTH;
            dialog.add(new JLabel("Name"), constraints);
            JTextField nameField = new JTextField();
            nameField.setText(entity.getName());
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            dialog.add(nameField, constraints);
            
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            dialog.add(new JLabel("Mesh"), constraints);
            JButton selectMeshButton = new JButton("Select");
            JTextField meshField = new JTextField();
            selectMeshButton.addActionListener((selectMeshEvent) -> {
                JFileChooser meshChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("jME3 Models", "j3o");
                meshChooser.setFileFilter(filter);
                File assetsDirectory = new File("./bin/org/simbrain/assets/");
                meshChooser.setCurrentDirectory(assetsDirectory);
                int returnValue = meshChooser.showOpenDialog(dialog);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    URI meshUri = meshChooser.getSelectedFile().toURI();
                    String asset = assetsDirectory.toURI().relativize(meshUri).toString();
                    meshField.setText(asset);
                }
            });
            constraints.gridx = 1;
            constraints.weightx = 0.25;
            dialog.add(meshField, constraints);
            constraints.gridx = 2;
            dialog.add(selectMeshButton, constraints);
            
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weightx = 0.5;
            dialog.add(new JLabel("Location"), constraints);
            JTable locationTable = new JTable();
            locationTable.setGridColor(Color.gray);
            FloatTableModel locationModel = new FloatTableModel(1, 3);
            locationModel.setValueAt(entity.getLocation().x, 0, 0);
            locationModel.setValueAt(entity.getLocation().y, 0, 1);
            locationModel.setValueAt(entity.getLocation().z, 0, 2);
            locationTable.setModel(locationModel);
            locationTable.setBorder(BorderFactory.createLineBorder(Color.black));
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            dialog.add(locationTable, constraints);
            
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.weightx = 0;
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            dialog.add(new JLabel("Rotation"), constraints);
            JTable rotationTable = new JTable();
            rotationTable.setGridColor(Color.gray);
            FloatTableModel rotationModel = new FloatTableModel(1, 3);
            float[] angles = entity.getOrientation().toAngles(null);
            rotationModel.setValueAt(angles[0], 0, 0);
            rotationModel.setValueAt(angles[1], 0, 1);
            rotationModel.setValueAt(angles[2], 0, 2);
            rotationTable.setModel(rotationModel);
            rotationTable.setBorder(BorderFactory.createLineBorder(Color.black));
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            dialog.add(rotationTable, constraints);
            
            JButton okButton = new JButton("Ok");
            okButton.addActionListener((okEvent) -> {
                entity.setName(nameField.getText());
                if (meshField != null && !meshField.getText().equals("")) {
                    Spatial model = world.getAssetManager().loadModel(meshField.getText());
                    Mesh mesh = ((Geometry)model).getMesh();
                    entity.getGeometry().setMesh(mesh);
                }
                entity.setLocation(new Vector3f(locationModel.getFloatAt(0, 0),
                        locationModel.getFloatAt(0, 1), locationModel.getFloatAt(0, 2)));
                Quaternion orientation = new Quaternion();
                orientation.fromAngles(rotationModel.getFloatAt(0, 0),
                        rotationModel.getFloatAt(0, 1), rotationModel.getFloatAt(0, 2));
                entity.setOrientation(orientation);
                dialog.dispose();
            });
            constraints.gridx = 1;
            constraints.gridy = 4;
            constraints.gridwidth = 1;
            dialog.add(okButton, constraints);
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener((cancelEvent) -> {
                dialog.dispose();
            });
            constraints.gridx = 2;
            dialog.add(cancelButton, constraints);
            
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {}
            });
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
}
