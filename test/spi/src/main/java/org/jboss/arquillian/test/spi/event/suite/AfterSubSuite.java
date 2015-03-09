package org.jboss.arquillian.test.spi.event.suite;

import org.jboss.arquillian.test.spi.TestClass;


public class AfterSubSuite extends SubSuiteEvent {

    public AfterSubSuite(TestClass testClass) {
        super(testClass);
    }
}
