package de.fjobilabs.springframework.hateoas.hal.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Resource class which want to use embedded resources by the {@link Embedded}
 * annotation should inherit from this class to get deserialized by the correct
 * deserializer.
 * 
 * @author Felix Jordan
 * @since 15.04.2017 - 20:34:12
 * @version 1.0
 */
@JsonDeserialize(using = HalResourceDeserializer.class)
public class HalResource {
}
