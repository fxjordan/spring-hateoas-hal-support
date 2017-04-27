package de.fjobilabs.springframework.hateoas.hal.client.exception;

/**
 * @author Felix Jordan
 * @since 27.04.2017 - 21:16:31
 * @version 1.0
 */
public class EmbeddedResourcePropertyException extends RuntimeException {
    
    private static final long serialVersionUID = 5016032003606790456L;
    
    public EmbeddedResourcePropertyException() {
        super();
    }
    
    public EmbeddedResourcePropertyException(String message) {
        super(message);
    }
    
    public EmbeddedResourcePropertyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EmbeddedResourcePropertyException(Throwable cause) {
        super(cause);
    }
}
