package org.jboss.arquillian.test.spi.event.suite;


public class BeforeSubSuite extends SubSuiteEvent {

    public BeforeSubSuite(Class<?> subSuiteClass) {
        super(subSuiteClass);
    }
}
