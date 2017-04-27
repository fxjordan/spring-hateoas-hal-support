package de.fjobilabs.springframework.hateoas.hal.client;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import de.fjobilabs.springframework.hateoas.hal.client.exception.EmbeddedResourcePropertyException;

/**
 * @author Felix Jordan
 * @since 15.04.2017 - 21:07:35
 * @version 1.0
 */
public class EmbeddedProperyUtils {
    
    private EmbeddedProperyUtils() {
    }
    
    public static Map<String, EmbeddedResourcePropertyDescriptor> createPropertyDescriptorMap(
            Class<?> type) {
        Map<String, EmbeddedResourcePropertyDescriptor> propetyDescriptors = new HashMap<>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(type);
        for (PropertyDescriptor descriptor : descriptors) {
            EmbeddedResourcePropertyDescriptor propertyDescriptor = createEmbeddedResourcePropertyDescriptor(
                    descriptor);
            if (propertyDescriptor != null) {
                propetyDescriptors.put(propertyDescriptor.getRelationName(), propertyDescriptor);
            }
        }
        return propetyDescriptors;
    }
    
    public static void setEmbeddedResources(
            Map<String, EmbeddedResourcePropertyDescriptor> propertyDescriptors, Object instance,
            Map<String, Object> properties) {
        for (Entry<String, Object> property : properties.entrySet()) {
            PropertyDescriptor descriptor = propertyDescriptors.get(property.getKey())
                    .getDescriptor();
            setEmbeddedResource(instance, descriptor, property.getValue());
        }
    }
    
    private static void setEmbeddedResource(Object instance, PropertyDescriptor descriptor,
            Object embeddedResource) {
        Method writerMethod = descriptor.getWriteMethod();
        if (writerMethod == null) {
            throw new EmbeddedResourcePropertyException(
                    "Embedded resource '" + descriptor.getName() + "' can't be written");
        }
        try {
            writerMethod.invoke(instance, embeddedResource);
        } catch (IllegalAccessException e) {
            throw new EmbeddedResourcePropertyException("Cannot acess writer method for embedded resource '"
                    + descriptor.getName() + "'", e);
        } catch (IllegalArgumentException | InvocationTargetException e) {
            throw new EmbeddedResourcePropertyException(
                    "Failed to write embedded resource '" + descriptor.getName() + "'", e);
        }
    }
    
    private static EmbeddedResourcePropertyDescriptor createEmbeddedResourcePropertyDescriptor(
            PropertyDescriptor descriptor) {
        EmbeddedResourcePropertyDescriptor propertyDescriptor = getPropertyDescriptorFromMethod(
                descriptor.getReadMethod(), descriptor);
        if (propertyDescriptor == null) {
            propertyDescriptor = getPropertyDescriptorFromMethod(descriptor.getWriteMethod(),
                    descriptor);
        }
        return propertyDescriptor;
    }
    
    private static EmbeddedResourcePropertyDescriptor getPropertyDescriptorFromMethod(Method method,
            PropertyDescriptor descriptor) {
        if (method == null) {
            return null;
        }
        Embedded annotation = AnnotationUtils.getAnnotation(method, Embedded.class);
        if (annotation != null) {
            return new EmbeddedResourcePropertyDescriptor(descriptor, annotation.value(),
                    descriptor.getPropertyType(), annotation.collectionContentType());
        }
        return null;
    }
    
    public static class EmbeddedResourcePropertyDescriptor implements Serializable {
        
        private static final long serialVersionUID = 201632889205761170L;
        
        private PropertyDescriptor descriptor;
        private String relationName;
        private Class<?> type;
        private Class<?> collectionContentType;
        
        public EmbeddedResourcePropertyDescriptor(PropertyDescriptor descriptor,
                String relationName, Class<?> type, Class<?> collectionContentType) {
            this.descriptor = descriptor;
            this.relationName = relationName;
            this.type = type;
            this.collectionContentType = collectionContentType;
        }
        
        public PropertyDescriptor getDescriptor() {
            return descriptor;
        }
        
        public String getRelationName() {
            return relationName;
        }
        
        public Class<?> getType() {
            return type;
        }
        
        public Class<?> getCollectionContentType() {
            return collectionContentType;
        }
    }
}
