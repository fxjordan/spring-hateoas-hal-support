package de.fjobilabs.springframework.hateoas.hal.client.exception;

/**
 * @author Felix Jordan
 * @since 27.04.2017 - 21:19:43
 * @version 1.0
 */
public class HalResourceDeserializerException extends RuntimeException {
    
    private static final long serialVersionUID = 4180588688687360284L;
    
    public HalResourceDeserializerException() {
        super();
    }
    
    public HalResourceDeserializerException(String message) {
        super(message);
    }
    
    public HalResourceDeserializerException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HalResourceDeserializerException(Throwable cause) {
        super(cause);
    }
}
