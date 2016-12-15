package org.simbrain.workspace;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class Attribute2 {

    Object baseObject;
    Method method;

    public abstract Type getType();

    @Override
    public String toString() {
        return baseObject.getClass().getSimpleName() + ":" + method.getName();
    }
}
