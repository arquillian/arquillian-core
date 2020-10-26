package org.arquillian.junit5;

import java.util.Map;

public class IdentifiedTestException extends RuntimeException {
    private final Map<String, Throwable> collectedExceptions;

    public IdentifiedTestException(Map<String, Throwable> exceptions) {
        this.collectedExceptions = exceptions;
    }

    public Map<String, Throwable> getCollectedExceptions() {
        return collectedExceptions;
    }
}
