package org.simbrain.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Producible annotation marks a method as a potential producer for a coupling.
 *
 * @author Tim Shea
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Producible { 
    
    /** The name of a method that returns a customized description for this producible.  */
    String customDescriptionMethod() default "";

}
