package org.jboss.arquillian.test.spi.event.suite;


public class AfterSubSuite extends SubSuiteEvent {

    public AfterSubSuite(Class<?> subSuiteClass) {
        super(subSuiteClass);
    }
}
