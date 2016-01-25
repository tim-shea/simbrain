package org.simbrain.world.threedworld.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.simbrain.world.threedworld.engine.ThreeDEngine;

import com.jme3.export.xml.XMLExporter;
import com.jme3.export.xml.XMLImporter;
import com.jme3.scene.Node;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.XppReader;

public class EntityXmlConverter implements Converter {
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(Entity.class);
    }
    
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        Entity entity = (Entity)value;
        writer.startNode("engine");
        context.convertAnother(entity.getEngine());
        writer.endNode();
        writer.startNode("name");
        writer.setValue(entity.getName());
        writer.endNode();
        writer.startNode("node");
        XMLExporter exporter = XMLExporter.getInstance();
        ByteArrayOutputStream nodeStream = new ByteArrayOutputStream();
        try {
            exporter.save(entity.getNode(), nodeStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save Entity", e);
        }
        String nodeString = new String(nodeStream.toByteArray());
        //new HierarchicalStreamCopier().copy(new XppReader(new StringReader(nodeString)), writer);
        writer.setValue(nodeString);
        writer.endNode();
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Entity entity = new Entity();
        reader.moveDown();
        entity.setEngine((ThreeDEngine)context.convertAnother(entity, ThreeDEngine.class));
        reader.moveUp();
        reader.moveDown();
        entity.setName(reader.getValue());
        reader.moveUp();
        reader.moveDown();
        String nodeString = reader.getValue();
        InputStream nodeStream = new ByteArrayInputStream(nodeString.getBytes());
        reader.moveUp();
        XMLImporter importer = XMLImporter.getInstance();
        importer.setAssetManager(entity.getEngine().getAssetManager());
        try {
            Node node = (Node)importer.load(nodeStream);
            entity.setNode(node);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load Entity", e);
        }
        return entity;
    }

}
