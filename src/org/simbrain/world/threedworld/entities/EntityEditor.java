package org.simbrain.world.threedworld.entities;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.simbrain.resource.ResourceManager;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class EntityEditor implements Editor {
    private Entity entity;
    private JTabbedPane tabbedPane;
    private JPanel mainTab;
    private JTextField nameField = new JTextField(20);
    private JTextField modelField = new JTextField(20);
    private Action loadModelAction = new AbstractAction() {
        { putValue(SMALL_ICON, ResourceManager.getImageIcon("Open.png")); }
        @Override
        public void actionPerformed(ActionEvent event) {
        }
    };
    private JButton loadModelButton = new JButton(loadModelAction);
    private JFormattedTextField xLocationField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField yLocationField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField zLocationField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField yawRotationField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField pitchRotationField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField rollRotationField = new JFormattedTextField(EditorDialog.floatFormat);
    
    public EntityEditor(Entity entity) {
        this.entity = entity;
        xLocationField.setColumns(6);
        yLocationField.setColumns(6);
        zLocationField.setColumns(6);
        yawRotationField.setColumns(6);
        pitchRotationField.setColumns(6);
        rollRotationField.setColumns(6);
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    public JPanel getMainTab() {
        return mainTab;
    }
    
    @Override
    public JComponent layoutFields() {
        tabbedPane = new JTabbedPane();
        
        mainTab = new JPanel();
        MigLayout layout = new MigLayout();
        mainTab.setLayout(layout);
        tabbedPane.addTab("Entity", mainTab);
        
        mainTab.add(new JLabel("Name"));
        mainTab.add(nameField, "growx, wrap");
        
        mainTab.add(new JLabel("Model"));
        mainTab.add(modelField, "split 2");
        mainTab.add(loadModelButton, "wrap");
        
        mainTab.add(new JLabel("Location"));
        mainTab.add(xLocationField, "split 3");
        mainTab.add(yLocationField);
        mainTab.add(zLocationField, "wrap");
        
        mainTab.add(new JLabel("Rotation"));
        mainTab.add(yawRotationField, "split 3");
        mainTab.add(pitchRotationField);
        mainTab.add(rollRotationField, "wrap");
        
        return tabbedPane;
    }
    
    @Override
    public void readValues() {
        nameField.setText(entity.getName());
        xLocationField.setValue(entity.getPosition().x);
        yLocationField.setValue(entity.getPosition().y);
        zLocationField.setValue(entity.getPosition().z);
        float[] angles = entity.getRotation().toAngles(null);
        yawRotationField.setValue(FastMath.RAD_TO_DEG * angles[1]);
        pitchRotationField.setValue(FastMath.RAD_TO_DEG * angles[0]);
        rollRotationField.setValue(FastMath.RAD_TO_DEG * angles[2]);
    }
    
    @Override
    public void writeValues() {
        entity.setName(nameField.getText());
        if (modelField != null && !modelField.getText().equals("")) {
            //Spatial model = world.getApplication().getAssetManager().loadModel(meshField.getText());
            //Mesh mesh = ((Geometry) model).getMesh();
            //entity.getGeometry().setMesh(mesh);
        }
        entity.queuePosition(new Vector3f(
                ((Number)xLocationField.getValue()).floatValue(),
                ((Number)yLocationField.getValue()).floatValue(),
                ((Number)zLocationField.getValue()).floatValue()));
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(
                FastMath.DEG_TO_RAD * ((Number)pitchRotationField.getValue()).floatValue(),
                FastMath.DEG_TO_RAD * ((Number)yawRotationField.getValue()).floatValue(),
                FastMath.DEG_TO_RAD * ((Number)rollRotationField.getValue()).floatValue());
        entity.queueRotation(rotation);
    }
    
    @Override
    public void close() {}
}
