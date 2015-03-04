package org.jboss.arquillian.test.spi.event.suite;


public class SubSuiteEvent extends SuiteEvent {

    private Class<?> subSuiteClass;
    
    public SubSuiteEvent(Class<?> subSuiteClass) {
        this.subSuiteClass = subSuiteClass;
    }
    
    public Class<?> getSubSuiteClass() {
        return subSuiteClass;
    }
}
