package org.jboss.arquillian.test.spi;

/**
 * An exception class that is common to both the client and server container side.
 */
public class IException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IException(String message) {
        super(message);
    }
}
