package org.jboss.arquillian.test.spi.serveronly;

import org.jboss.arquillian.test.spi.IException;

public class SomeNonClientSideException extends IException {
    private static final long serialVersionUID = 1L;
    public SomeNonClientSideException() {
        super("The server side reason for this exception");
    }
}
