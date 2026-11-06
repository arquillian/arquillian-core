package org.jboss.arquillian.junit5;

import java.util.Map;

/**
 * Wraps the exceptions collected while running a test in the container, keyed by the unique id of the test they
 * were reported for. It carries the exceptions of a failed test as well as of a test that was aborted, for example
 * by a failed assumption, and is unwrapped again on the client side.
 */
public class IdentifiedTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Map<String, Throwable> collectedExceptions;

    public IdentifiedTestException(Map<String, Throwable> exceptions) {
        super(exceptions.values().stream().findFirst().orElse(null));
        this.collectedExceptions = exceptions;
    }

    public Map<String, Throwable> getCollectedExceptions() {
        return collectedExceptions;
    }

    @Override
    public String toString() {
        return "IdentifiedTestException [collectedExceptions=" + collectedExceptions + "]";
    }
}
