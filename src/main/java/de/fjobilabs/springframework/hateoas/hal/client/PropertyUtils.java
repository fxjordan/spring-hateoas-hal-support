package de.fjobilabs.springframework.hateoas.hal.client;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.fjobilabs.springframework.hateoas.hal.client.exception.PropertyException;

/**
 * @author Felix Jordan
 * @since 28.11.2016 - 17:21:04
 * @version 1.0
 */
public class PropertyUtils {
    
    private PropertyUtils() {
    }
    
    public static Map<String, Class<?>> createPropertyTypesMap(Class<?> type) {
        Map<String, Class<?>> types = new HashMap<>();
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(type);
        for (PropertyDescriptor descriptor : descriptors) {
            String name = getPropertyName(descriptor);
            if (name != null) {
                types.put(name, descriptor.getPropertyType());
            }
        }
        return types;
    }
    
    public static void setProperties(Object instance, Map<String, Object> properties) {
        for (Entry<String, Object> property : properties.entrySet()) {
            PropertyDescriptor descriptor = findPropertyDescriptor(instance, property.getKey());
            setPropertyValue(instance, descriptor, property.getValue());
        }
    }
    
    private static void setPropertyValue(Object instance, PropertyDescriptor descriptor,
            Object value) {
        Method writerMethod = descriptor.getWriteMethod();
        if (writerMethod == null) {
            throw new PropertyException("Property '" + descriptor.getName() + "' can't be written");
        }
        try {
            writerMethod.invoke(instance, value);
        } catch (IllegalAccessException e) {
            throw new PropertyException(
                    "Cannot acess writer method for property '" + descriptor.getName() + "'", e);
        } catch (IllegalArgumentException | InvocationTargetException e) {
            throw new PropertyException("Failed to write property '" + descriptor.getName() + "'",
                    e);
        }
    }
    
    private static PropertyDescriptor findPropertyDescriptor(Object instance, String name) {
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(instance.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (name.equals(getPropertyName(descriptor))) {
                return descriptor;
            }
        }
        throw new PropertyException(
                "Failed to find property '" + name + "' for instance " + instance);
    }
    
    private static String getPropertyName(PropertyDescriptor descriptor) {
        String name = getPropertyNameFromMethod(descriptor.getReadMethod());
        if (name == null) {
            name = getPropertyNameFromMethod(descriptor.getWriteMethod());
        }
        if (name == null) {
            return descriptor.getName();
        }
        return name;
    }
    
    private static String getPropertyNameFromMethod(Method method) {
        if (method == null) {
            return null;
        }
        JsonProperty annotation = AnnotationUtils.getAnnotation(method, JsonProperty.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }
}
