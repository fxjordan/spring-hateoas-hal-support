package de.fjobilabs.springframework.hateoas.hal.client;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Resources;

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
import com.fasterxml.jackson.databind.type.TypeFactory;

import de.fjobilabs.springframework.hateoas.hal.client.EmbeddedProperyUtils.EmbeddedResourcePropertyDescriptor;

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
    private final Map<String, EmbeddedResourcePropertyDescriptor> embeddedResourcePropertyDescriptors;
    private boolean ignoreUnknownProperties;
    
    protected HalResourceDeserializer() {
        this(HalResource.class);
    }
    
    protected HalResourceDeserializer(Class<?> targetClass) {
        super(targetClass);
        this.targetClass = targetClass;
        this.propertyTypes = PropertyUtils.createPropertyTypesMap(targetClass);
        this.embeddedResourcePropertyDescriptors = EmbeddedProperyUtils
                .createPropertyDescriptorMap(targetClass);
        this.ignoreUnknownProperties = shouldIgnoreUnknownProperties(targetClass);
    }
    
    @Override
    public HalResource deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Invalid token, expected START_OBJECT");
        }
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> embeddedResources = new HashMap<>();
        
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.getCurrentName();
            parser.nextToken();
            
            if (EMBEDDED_ELEMENT_NAME.equals(key)) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String embeddedKey = parser.getCurrentName();
                    parser.nextToken();
                    Object embeddedResource = handleEmbeddedResource(embeddedKey, parser,
                            context.getTypeFactory());
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
        if (this.targetClass == clazz) {
            return this;
        }
        return new HalResourceDeserializer(clazz);
    }
    
    private Object createInstance(Map<String, Object> properties,
            Map<String, Object> embeddedResources) {
        Object instance = BeanUtils.instantiateClass(this.targetClass);
        
        PropertyUtils.setProperties(instance, properties);
        EmbeddedProperyUtils.setEmbeddedResources(this.embeddedResourcePropertyDescriptors,
                instance, embeddedResources);
                
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
    
    private Object handleEmbeddedResource(String key, JsonParser parser, TypeFactory typeFactory)
            throws IOException {
        EmbeddedResourcePropertyDescriptor descriptor = this.embeddedResourcePropertyDescriptors
                .get(key);
        if (descriptor == null) {
            if (this.ignoreUnknownProperties) {
                return null;
            }
            throw new RuntimeException(
                    "Invalid embedded resource key '" + key + "' for type " + targetClass);
        }
        Class<?> type = descriptor.getType();
        if (type.isArray()) {
            return readEmbeddedArray(descriptor, parser, typeFactory);
        }
        if (Collection.class.isAssignableFrom(type)) {
            if (Embedded.DefaultCollectionContentType.class
                    .equals(descriptor.getCollectionContentType())) {
                throw new RuntimeException(
                        "No collection content type for embedded resource: " + key);
            }
            return readEmbeddedCollection(descriptor, parser, typeFactory);
        }
        return parser.readValueAs(type);
    }
    
    private Object readEmbeddedArray(EmbeddedResourcePropertyDescriptor descriptor,
            JsonParser parser, TypeFactory typeFactory) throws IOException {
        Collection<?> collection = readEmbeddedCollection(descriptor, parser, typeFactory);
        Object[] elements = collection.toArray();
        int length = elements.length;
        Object array = Array.newInstance(descriptor.getCollectionContentType(), length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, elements[i]);
        }
        return array;
    }
    
    private Collection<?> readEmbeddedCollection(EmbeddedResourcePropertyDescriptor descriptor,
            JsonParser parser, TypeFactory typeFactory) throws IOException {
        Resources<?> resources = parser.getCodec().readValue(parser, typeFactory
                .constructParametricType(Resources.class, descriptor.getCollectionContentType()));
        if (resources == null) {
            return null;
        }
        return resources.getContent();
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
