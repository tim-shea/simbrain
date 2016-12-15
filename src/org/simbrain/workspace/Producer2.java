package org.simbrain.workspace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Producer2<V> extends Attribute2 {

    public Producer2(Object baseObject, Method method) {
        this.baseObject = baseObject;
        this.method = method;
    }

    V getValue() {
        try {
            return (V) method.invoke(baseObject, null);
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