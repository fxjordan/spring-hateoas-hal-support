package de.fjobilabs.springframework.hateoas.hal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Adds support for embedded resources to a server side resource class.<br>
 * Spring automatically renders the correct HAL json form this class. You simply
 * have to add the resources by callign the {@link #embedd(ResourceSupport)} or
 * {@link #embedd(ResourceSupport, String)} method.
 * 
 * @author Felix Jordan
 * @since 13.04.2017 - 23:07:01
 * @version 1.0
 */
public class HalEmbeddedResourceSupport extends Resources<EmbeddedWrapper> {
    
    private final List<EmbeddedWrapper> embeddedResources;
    private final EmbeddedWrappers embeddedWrappers;
    
    /**
     * Creates an new {@link HalEmbeddedResourceSupport} instance.
     */
    public HalEmbeddedResourceSupport() {
        this.embeddedResources = new ArrayList<>();
        this.embeddedWrappers = new EmbeddedWrappers(false);
    }
    
    /**
     * Embeds an object or collection into the resource.
     * 
     * @param object The object to embed.
     */
    public void embedd(Object object) {
        /*
         * TODO Automatically use relation provides by @Relation annotation
         */
        this.embeddedResources.add(this.embeddedWrappers.wrap(object));
    }
    
    /**
     * Embeds an object or collection into the resource.
     * 
     * @param object The object to embed.
     * @param rel The relation to use.
     */
    public void embedd(Object object, String rel) {
        this.embeddedResources.add(this.embeddedWrappers.wrap(object, rel));
    }
    
    @JsonProperty("_embedded")
    @Override
    public Collection<EmbeddedWrapper> getContent() {
        return Collections.unmodifiableCollection(embeddedResources);
    }
    
    @Override
    public Iterator<EmbeddedWrapper> iterator() {
        return embeddedResources.iterator();
    }
    
    @Override
    public String toString() {
        return String.format("Resources { content: %s, %s }", getContent(), super.toString());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }
        HalEmbeddedResourceSupport that = (HalEmbeddedResourceSupport) obj;
        boolean contentEqual = this.embeddedResources == null ? that.embeddedResources == null
                : this.embeddedResources.equals(that.embeddedResources);
        return contentEqual ? super.equals(obj) : false;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += embeddedResources == null ? 0 : 17 * embeddedResources.hashCode();
        return result;
    }
}
