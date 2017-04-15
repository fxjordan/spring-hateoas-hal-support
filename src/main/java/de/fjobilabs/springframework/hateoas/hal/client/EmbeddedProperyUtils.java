package de.fjobilabs.springframework.hateoas.hal.client;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author Felix Jordan
 * @since 15.04.2017 - 21:07:35
 * @version 1.0
 */
public class EmbeddedProperyUtils {
    
    private EmbeddedProperyUtils() {
    }
    
    public static Map<String, Class<?>> createEmbeddedResourcesTypesMap(Class<?> type) {
        Map<String, Class<?>> types = new HashMap<>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(type);
        for (PropertyDescriptor descriptor : descriptors) {
            String name = getEmbeddedResourceName(descriptor);
            if (name != null) {
                types.put(name, descriptor.getPropertyType());
            }
        }
        return types;
    }
    
    public static void setEmbeddedResources(Object instance, Map<String, Object> properties) {
        for (Entry<String, Object> property : properties.entrySet()) {
            PropertyDescriptor descriptor = findPropertyDescriptor(instance, property.getKey());
            setEmbeddedResource(instance, descriptor, property.getValue());
        }
    }
    
    private static void setEmbeddedResource(Object instance, PropertyDescriptor descriptor,
            Object embeddedResource) {
        Method writerMethod = descriptor.getWriteMethod();
        if (writerMethod == null) {
            throw new RuntimeException(
                    "Embedded resource '" + descriptor.getName() + "' can't be written");
        }
        try {
            writerMethod.invoke(instance, embeddedResource);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot acess writer method for embedded resource '"
                    + descriptor.getName() + "'", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Failed to write embedded resource '" + descriptor.getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Failed to write embedded resource '" + descriptor.getName() + "'", e);
        }
    }
    
    private static PropertyDescriptor findPropertyDescriptor(Object instance, String name) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(instance.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (name.equals(getEmbeddedResourceName(descriptor))) {
                return descriptor;
            }
        }
        throw new RuntimeException(
                "Failed to find property '" + name + "' for instance " + instance);
    }
    
    private static String getEmbeddedResourceName(PropertyDescriptor descriptor) {
        String name = getPropertyNameFromMethod(descriptor.getReadMethod());
        if (name == null) {
            name = getPropertyNameFromMethod(descriptor.getWriteMethod());
        }
        return name;
    }
    
    private static String getPropertyNameFromMethod(Method method) {
        if (method == null) {
            return null;
        }
        Embedded annotation = AnnotationUtils.getAnnotation(method, Embedded.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }
}
