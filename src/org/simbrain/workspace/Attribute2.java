package org.simbrain.workspace;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class Attribute2 {

    Object baseObject;
    Method method;
    private String description = "";

    Object[] keys;

    public abstract Type getType();

    @Override
    public String toString() {

        //TODO: Not working
        String typeName;
        if (getType().getClass().isArray()) {
            typeName = "Array[" + getType().getClass().getComponentType() + "]";
        } else {
            typeName = getType().toString();
        }

        if (description.isEmpty()) {
            description = baseObject.getClass().getSimpleName();
        }

        return description + ":" + method.getName()
                + "<" + typeName + ">";
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the baseObject
     */
    public Object getBaseObject() {
        return baseObject;
    }

    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }
}
