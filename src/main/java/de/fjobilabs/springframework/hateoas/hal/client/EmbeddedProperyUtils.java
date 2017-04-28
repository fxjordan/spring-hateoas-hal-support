package de.fjobilabs.springframework.hateoas.hal.client;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
            setEmbeddedResource(instance, propertyDescriptors.get(property.getKey()),
                    property.getValue());
        }
    }
    
    private static void setEmbeddedResource(Object instance,
            EmbeddedResourcePropertyDescriptor descriptor, Object embeddedResource) {
        Method writerMethod = descriptor.getWriteMethod();
        if (writerMethod == null) {
            throw new EmbeddedResourcePropertyException(
                    "Embedded resource '" + descriptor.getRelationName() + "' can't be written");
        }
        try {
            writerMethod.invoke(instance, embeddedResource);
        } catch (IllegalAccessException e) {
            throw new EmbeddedResourcePropertyException(
                    "Cannot acess writer method for embedded resource '"
                            + descriptor.getRelationName() + "'",
                    e);
        } catch (IllegalArgumentException | InvocationTargetException e) {
            throw new EmbeddedResourcePropertyException(
                    "Failed to write embedded resource '" + descriptor.getRelationName() + "'", e);
        }
    }
    
    private static EmbeddedResourcePropertyDescriptor createEmbeddedResourcePropertyDescriptor(
            PropertyDescriptor descriptor) {
        EmbeddedResourcePropertyDescriptor propertyDescriptor = getPropertyDescriptorFromMethod(
                descriptor.getReadMethod(), descriptor.getPropertyType());
        if (propertyDescriptor == null) {
            propertyDescriptor = getPropertyDescriptorFromMethod(descriptor.getWriteMethod(),
                    descriptor.getPropertyType());
        }
        return propertyDescriptor;
    }
    
    private static EmbeddedResourcePropertyDescriptor getPropertyDescriptorFromMethod(Method method,
            Class<?> propertyType) {
        if (method == null) {
            return null;
        }
        Embedded annotation = AnnotationUtils.getAnnotation(method, Embedded.class);
        if (annotation != null) {
            return new EmbeddedResourcePropertyDescriptor(method, annotation.value(), propertyType,
                    annotation.collectionContentType());
        }
        return null;
    }
    
    public static class EmbeddedResourcePropertyDescriptor implements Serializable {
        
        private static final long serialVersionUID = 201632889205761170L;
        
        private SerializableMethod writeMethod;
        private String relationName;
        private Class<?> type;
        private Class<?> collectionContentType;
        
        public EmbeddedResourcePropertyDescriptor(Method writeMethod, String relationName,
                Class<?> type, Class<?> collectionContentType) {
            this.writeMethod = new SerializableMethod(writeMethod);
            this.relationName = relationName;
            this.type = type;
            this.collectionContentType = collectionContentType;
        }
        
        public Method getWriteMethod() {
            return writeMethod.getMethod();
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
    
    private static class SerializableMethod implements Serializable {
        
        private static final long serialVersionUID = -6525630597759789730L;
        
        private Method method;
        
        public SerializableMethod(Method method) {
            this.method = method;
        }
        
        public Method getMethod() {
            return method;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.method.getDeclaringClass());
            out.writeUTF(this.method.getName());
            out.writeObject(this.method.getParameterTypes());
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Class<?> declaringClass = (Class<?>) in.readObject();
            String name = in.readUTF();
            Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
            try {
                this.method = declaringClass.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IOException(String.format(
                        "Exception occurred while resolving deserialized method '%s.%s'",
                        declaringClass.getSimpleName(), name), e);
            }
        }
    }
}
