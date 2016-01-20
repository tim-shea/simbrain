package org.simbrain.world.threedworld.actions;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.simbrain.world.threedworld.entities.Entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class EntityEditor {
    public static class FloatTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 7779686921439025449L;
        
        public FloatTableModel(int rows, int columns) {
            super(rows, columns);
        }
        
        public float parseFloatAt(int row, int column) {
            Object value = getValueAt(row, column);
            return Float.parseFloat(value.toString());
        }
        
        public void formatFloatAt(float value, int row, int column) {
            setValueAt(format.format(value), row, column);
        }
        
        @Override public void setValueAt(Object value, int row, int column) {
            try {
                Float.parseFloat(value.toString());
                super.setValueAt(value, row, column);
            } catch (NumberFormatException e) {
                // Ignore new value
            }
        }
    }
    
    static float radToDeg(float value) {
        return (float)(180 * value / Math.PI);
    }
    
    static float degToRad(float value) {
        return (float)(Math.PI * value / 180);
    }
    
    static DecimalFormat format = new DecimalFormat();
    
    static {
        format.setMaximumFractionDigits(3);
        format.setMinimumFractionDigits(1);
    }
    
    public void showEditor(Entity entity) {
        if (entity != null) {
            JDialog dialog = new JDialog();
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            DecimalFormat floatFormat = new DecimalFormat();
            floatFormat.setMaximumFractionDigits(3);
            floatFormat.setMinimumFractionDigits(1);
            
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
            locationTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            locationTable.setGridColor(Color.gray);
            FloatTableModel locationModel = new FloatTableModel(1, 3);
            locationModel.formatFloatAt(entity.getLocation().x, 0, 0);
            locationModel.formatFloatAt(entity.getLocation().y, 0, 1);
            locationModel.formatFloatAt(entity.getLocation().z, 0, 2);
            locationTable.setModel(locationModel);
            locationTable.setBorder(BorderFactory.createLineBorder(Color.black));
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            dialog.add(locationTable, constraints);
            
            constraints.gridx = 0;
            constraints.gridy = 3;
            dialog.add(new JLabel("Rotation"), constraints);
            JTable rotationTable = new JTable();
            rotationTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            rotationTable.setGridColor(Color.gray);
            FloatTableModel rotationModel = new FloatTableModel(1, 3);
            float[] angles = entity.getOrientation().toAngles(null);
            rotationModel.formatFloatAt(radToDeg(angles[0]), 0, 0);
            rotationModel.formatFloatAt(radToDeg(angles[1]), 0, 1);
            rotationModel.formatFloatAt(radToDeg(angles[2]), 0, 2);
            rotationTable.setModel(rotationModel);
            rotationTable.setBorder(BorderFactory.createLineBorder(Color.black));
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            dialog.add(rotationTable, constraints);
            
            JButton okButton = new JButton("Ok");
            okButton.addActionListener((okEvent) -> {
                entity.setName(nameField.getText());
                if (meshField != null && !meshField.getText().equals("")) {
                    //Spatial model = world.getApplication().getAssetManager().loadModel(meshField.getText());
                    //Mesh mesh = ((Geometry) model).getMesh();
                    //entity.getGeometry().setMesh(mesh);
                }
                locationTable.clearSelection();
                entity.queueLocation(new Vector3f(locationModel.parseFloatAt(0, 0),
                        locationModel.parseFloatAt(0, 1), locationModel.parseFloatAt(0, 2)));
                Quaternion orientation = new Quaternion();
                rotationTable.clearSelection();
                orientation.fromAngles(degToRad(rotationModel.parseFloatAt(0, 0)),
                        degToRad(rotationModel.parseFloatAt(0, 1)), degToRad(rotationModel.parseFloatAt(0, 2)));
                entity.queueOrientation(orientation);
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
                @Override public void windowClosed(WindowEvent event) {
                }
            });
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
}
