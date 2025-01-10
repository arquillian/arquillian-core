package org.jboss.arquillian.test.spi.serveronly;

import org.jboss.arquillian.test.spi.IBean;

/**
 * A simple bean that throws an exception. This is loaded by a custom classloader
 * in the test client so that the exception thrown is not available on the client.
 */
public class SomeBean implements IBean {
    public String invoke() {
        return invokeImpl();
    }

    private String invokeImpl() {
        return invokeImplNested();
    }
    private String invokeImplNested() throws SomeNonClientSideException {
        throw new SomeNonClientSideException();
    }
}
