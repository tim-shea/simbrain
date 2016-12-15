package org.simbrain.workspace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Producer2<V> extends Attribute2 {

    public Producer2(Object baseObject, Method method) {
        this.baseObject = baseObject;
        this.method = method;
    }

    public Producer2(Object baseObject, Method method, Object[] keys) {
        this(baseObject, method);
        this.keys = keys;
    }

    V getValue() {
        try {
            return (V) method.invoke(baseObject, keys);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Type getType() {
        return method.getReturnType();
    }
}