package de.fjobilabs.springframework.hateoas.hal.client.exception;

/**
 * @author Felix Jordan
 * @since 27.04.2017 - 21:21:17
 * @version 1.0
 */
public class PropertyException extends RuntimeException {
    
    private static final long serialVersionUID = 4905439823784612394L;
    
    public PropertyException() {
        super();
    }
    
    public PropertyException(String message) {
        super(message);
    }
    
    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PropertyException(Throwable cause) {
        super(cause);
    }
}
