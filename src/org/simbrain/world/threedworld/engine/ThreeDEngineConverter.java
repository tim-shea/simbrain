package org.simbrain.world.threedworld.engine;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ThreeDEngineConverter implements Converter {
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(ThreeDEngine.class);
    }
    
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ThreeDEngine engine = new ThreeDEngine();
        engine.queueState(ThreeDEngine.State.SystemPause, true);
        return engine;
    }
}
