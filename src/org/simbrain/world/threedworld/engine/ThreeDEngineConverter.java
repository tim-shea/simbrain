package org.simbrain.world.threedworld.engine;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ThreeDEngineConverter implements Converter {
    private ThreeDEngine engine;
    
    public ThreeDEngineConverter() {}
    
    public ThreeDEngineConverter(ThreeDEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public boolean canConvert(Class type) {
        return type.equals(ThreeDEngine.class);
    }
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (engine == null) {
            engine = new ThreeDEngine();
            engine.queueState(ThreeDEngine.State.SystemPause, true);
        }
        return engine;
    }
}
