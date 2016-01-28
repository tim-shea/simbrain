package org.simbrain.world.threedworld.entities;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class AgentXmlConverter implements Converter {
    @Override
    public boolean canConvert(Class type) {
        return type.equals(Agent.class);
    }
    
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        Agent agent = (Agent)value;
        
        writer.startNode("model");
        context.convertAnother(agent.getModel());
        writer.endNode();
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        ModelEntity model = (ModelEntity)context.convertAnother(null, ModelEntity.class);
        reader.moveUp();
        
        return new Agent(model);
    }

}
