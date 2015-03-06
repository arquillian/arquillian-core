package org.jboss.arquillian.test.spi.event.suite;


public class BeforeSubSuite extends SubSuiteEvent {

    public BeforeSubSuite(SubSuiteClass subSuiteClass) {
        super(subSuiteClass);
    }
}
