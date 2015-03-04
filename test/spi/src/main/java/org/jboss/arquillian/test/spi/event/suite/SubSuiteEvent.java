package org.jboss.arquillian.test.spi.event.suite;

import org.jboss.arquillian.test.spi.TestClass;


public class SubSuiteEvent extends SuiteEvent {

    private TestClass subSuiteClass;
    
    public SubSuiteEvent(Class<?> subSuiteClass) {
        this.subSuiteClass = new TestClass(subSuiteClass);
    }
    
    public TestClass getSubSuiteClass() {
        return subSuiteClass;
    }
}
