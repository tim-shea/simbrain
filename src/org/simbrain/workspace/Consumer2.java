package org.simbrain.workspace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Consumer2<V> extends Attribute2 {

    public Consumer2(Object baseObject, Method method) {
        this.baseObject = baseObject;
        this.method = method;
    }

    void setValue(V value) {
        try {
            method.invoke(baseObject,
                    new Object[] {value});
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Type getType() {
        return method.getGenericParameterTypes()[0];
    }
}
