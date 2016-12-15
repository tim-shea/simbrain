package org.simbrain.workspace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

public class Consumer2<V> extends Attribute2 {

    public Consumer2(Object baseObject, Method method) {
        this.baseObject = baseObject;
        this.method = method;
        if (method.getParameterTypes().length > 1) {
            // TODO Throw exception
            System.err.println("you'll need "
                    + (method.getParameterTypes().length - 1) + " arguments");
        }
        ;
    }

    // TODO consolidate all constructors and use one with a parameter list
    public Consumer2(Object baseObject, Method method, Object key) {
        this.baseObject = baseObject;
        this.method = method;
        this.keys = new Object[] { key };
        if (method.getParameterTypes().length == 0) {
            // TODO Throw exception
            System.err.println("Why are you providing a key?");
        }
        ;
    }

    void setValue(V value) {
        try {
            if (keys == null) {
                method.invoke(baseObject, new Object[] { value });
            } else {
                method.invoke(baseObject,
                        // TODO: Handle general case; perf check; generalize to
                        // producer?
                        new Object[] { value, keys[0] });
            }
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
