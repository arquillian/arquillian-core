package org.jboss.arquillian.junit5;

import java.util.Map;

public class IdentifiedTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private final Map<String, Throwable> collectedExceptions;

    public IdentifiedTestException(Map<String, Throwable> exceptions) {
        this.collectedExceptions = exceptions;
    }

    public Map<String, Throwable> getCollectedExceptions() {
        return collectedExceptions;
    }
}
