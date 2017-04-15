package de.fjobilabs.springframework.hateoas.hal.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Felix Jordan
 * @since 15.04.2017 - 19:05:10
 * @version 1.0
 */
public class HalResourceDeserializer extends StdDeserializer<HalResource>
        implements ContextualDeserializer {
        
    private static final long serialVersionUID = -8650768414804516240L;
    
    private static final String EMBEDDED_ELEMENT_NAME = "_embedded";
    private static final String LINKS_ELEMENT_NAME = "_links";
    
    private final Class<?> targetClass;
    private final Map<String, Class<?>> propertyTypes;
    private final Map<String, Class<?>> embeddedResourceTypes;
    private boolean ignoreUnknownProperties;
    
    protected HalResourceDeserializer() {
        this(HalResource.class);
    }
    
    protected HalResourceDeserializer(Class<?> targetClass) {
        super(targetClass);
        this.targetClass = targetClass;
        this.propertyTypes = PropertyUtils.createPropertyTypesMap(targetClass);
        this.embeddedResourceTypes = EmbeddedProperyUtils
                .createEmbeddedResourcesTypesMap(targetClass);
        this.ignoreUnknownProperties = shouldIgnoreUnknownProperties(targetClass);
    }
    
    @Override
    public HalResource deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        System.out.println("Deserializing class: " + this.targetClass);
        
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Invalid token, expected START_OBJECT");
        }
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> embeddedResources = new HashMap<>();
        
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();
            parser.nextToken();
            System.out.println("key: " + key);
            
            if (EMBEDDED_ELEMENT_NAME.equals(key)) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String embeddedKey = parser.getCurrentName();
                    System.out.println("embedded key: " + embeddedKey);
                    parser.nextToken();
                    Object embeddedResource = handleEmbeddedResource(embeddedKey, parser);
                    if (embeddedResource != null) {
                        embeddedResources.put(embeddedKey, embeddedResource);
                    }
                }
            } else if (LINKS_ELEMENT_NAME.equals(key)) {
                parser.skipChildren();
            } else {
                // Normal properties
                Object property = handleProperty(key, parser);
                if (property != null) {
                    properties.put(key, property);
                }
            }
        }
        return (HalResource) createInstance(properties, embeddedResources);
    }
    
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context,
            BeanProperty property) throws JsonMappingException {
        Class<?> clazz = context.getContextualType().getRawClass();
        System.out.println("class to deserialize: " + clazz);
        if (this.targetClass == clazz) {
            System.out.println("Deserializer is already configured for: " + clazz);
            return this;
        }
        System.out.println("Creating contextual deserializer for class: " + clazz);
        return new HalResourceDeserializer(clazz);
    }
    
    private Object createInstance(Map<String, Object> properties,
            Map<String, Object> embeddedResources) {
        Object instance = BeanUtils.instantiateClass(this.targetClass);
        
        PropertyUtils.setProperties(instance, properties);
        EmbeddedProperyUtils.setEmbeddedResources(instance, embeddedResources);
        
        return instance;
    }
    
    private Object handleProperty(String key, JsonParser parser) throws IOException {
        Class<?> type = this.propertyTypes.get(key);
        if (type == null) {
            if (this.ignoreUnknownProperties) {
                return null;
            }
            throw new RuntimeException(
                    "Invalid property key '" + key + "' for type " + targetClass);
        }
        return parser.readValueAs(type);
    }
    
    private Object handleEmbeddedResource(String key, JsonParser parser) throws IOException {
        Class<?> type = this.embeddedResourceTypes.get(key);
        if (type == null) {
            if (this.ignoreUnknownProperties) {
                return null;
            }
            throw new RuntimeException(
                    "Invalid embedded resource key '" + key + "' for type " + targetClass);
        }
        return parser.readValueAs(type);
    }
    
    private boolean shouldIgnoreUnknownProperties(Class<?> clazz) {
        JsonIgnoreProperties annotation = AnnotationUtils.findAnnotation(clazz,
                JsonIgnoreProperties.class);
        if (annotation == null) {
            return false;
        }
        return annotation.ignoreUnknown();
    }
}
