package org.simbrain.world.threedworld.entities;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.simbrain.world.threedworld.engine.ThreeDEngine;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.util.TangentBinormalGenerator;

public class BoxEntity extends PhysicalEntity {
    private class BoxEntityEditor extends EntityEditor {
        private JFormattedTextField sizeXField = new JFormattedTextField(EditorDialog.floatFormat);
        private JFormattedTextField sizeYField = new JFormattedTextField(EditorDialog.floatFormat);
        private JFormattedTextField sizeZField = new JFormattedTextField(EditorDialog.floatFormat);
        private JFormattedTextField massField = new JFormattedTextField(EditorDialog.floatFormat);
        private JTextField materialField = new JTextField(20);
        
        {
            sizeXField.setColumns(6);
            sizeYField.setColumns(6);
            sizeZField.setColumns(6);
            massField.setColumns(6);
        }
        
        private BoxEntityEditor() {
            super(BoxEntity.this);
        }
        
        @Override
        public JComponent layoutFields() {
            JComponent entityLayout = super.layoutFields();
            
            JPanel boxTab = new JPanel();
            boxTab.setLayout(new MigLayout());
            getTabbedPane().addTab("Box", boxTab);
            
            boxTab.add(new JLabel("Size"));
            boxTab.add(sizeXField, "split 3");
            boxTab.add(sizeYField);
            boxTab.add(sizeZField, "wrap");
            
            boxTab.add(new JLabel("Mass"));
            boxTab.add(massField, "wrap");
            
            boxTab.add(new JLabel("Material"));
            boxTab.add(materialField, "wrap");
            
            return entityLayout;
        }
        
        @Override
        public void readValues() {
            super.readValues();
            sizeXField.setValue(getSize().x);
            sizeYField.setValue(getSize().y);
            sizeZField.setValue(getSize().z);
            massField.setValue(getMass());
            materialField.setText(getMaterial());
        }
        
        @Override
        public void writeValues() {
            super.writeValues();
            setSize(new Vector3f(
                    ((Number)sizeXField.getValue()).floatValue(),
                    ((Number)sizeYField.getValue()).floatValue(),
                    ((Number)sizeZField.getValue()).floatValue()));
            setMass(((Number)massField.getValue()).floatValue());
            setMaterial(materialField.getText());
        }
    }
    
    private Box box;
	private Geometry geometry;
	
	public BoxEntity(ThreeDEngine engine, String name) {
		this(engine, name, Vector3f.UNIT_XYZ, 1, "Materials/BlueTile.j3m");
	}
	
	public BoxEntity(ThreeDEngine engine, String name, Vector3f size, float mass, String material) {
		super(engine, new Node(name));
		box = new Box(size.x, size.y, size.z);
        TangentBinormalGenerator.generate(box);
        geometry = new Geometry(name + "Geometry", box);
        geometry.setMaterial(engine.getAssetManager().loadMaterial(material));
        getNode().attachChild(geometry);
        CollisionShape shape = CollisionShapeFactory.createBoxShape(geometry);
        RigidBodyControl body = new RigidBodyControl(shape, mass);
        setBody(body);
	}
	
	public Vector3f getSize() {
		return new Vector3f(box.xExtent, box.yExtent, box.zExtent);
	}
	
	public void setSize(Vector3f value) {
		box = new Box(value.x, value.y, value.z);
		TangentBinormalGenerator.generate(box);
		geometry.setMesh(box);
		CollisionShape shape = CollisionShapeFactory.createBoxShape(geometry);
		getBody().setCollisionShape(shape);
	}
	
	public float getMass() {
		return getBody().getMass();
	}
	
	public void setMass(float value) {
		getBody().setMass(value);
	}
	
	public String getMaterial() {
		return geometry.getMaterial().getAssetName();
	}
	
	public void setMaterial(String value) {
		geometry.setMaterial(getEngine().getAssetManager().loadMaterial(value));
	}
	
	@Override
	public BoundingVolume getBounds() {
	    return geometry.getWorldBound();
	}
	
	@Override
	public Editor getEditor() {
	    return new BoxEntityEditor();
	}
}
