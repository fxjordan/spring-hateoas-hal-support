package de.fjobilabs.springframework.hateoas.hal.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Felix Jordan
 * @since 14.04.2017 - 23:49:40
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Embedded {
    
    String value();
    
    Class<?> collectionContentType() default DefaultCollectionContentType.class;
    
    /**
     * Empty class to have a default value for the
     * {@link Embedded#collectionContentType()} property.
     * 
     * @author Felix Jordan
     * @since 20.04.2017 - 00:59:42
     * @version 1.0
     */
    public static final class DefaultCollectionContentType {
    }
}
