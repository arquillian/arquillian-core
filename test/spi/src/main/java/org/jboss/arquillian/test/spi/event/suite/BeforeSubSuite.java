package org.jboss.arquillian.test.spi.event.suite;

import org.jboss.arquillian.test.spi.TestClass;


public class BeforeSubSuite extends SubSuiteEvent {

    public BeforeSubSuite(TestClass testClass) {
        super(testClass);
    }
}
