package org.jboss.arquillian.test.spi.event.suite;

import org.jboss.arquillian.test.spi.TestClass;


public class SubSuiteEvent extends SuiteEvent {

    private TestClass testClass;

    public SubSuiteEvent(TestClass testClass) {

       Validate.notNull(testClass, "TestClass must be specified");
       this.testClass = testClass;
    }

    public TestClass getTestClass() {
        return testClass;
    }

    public boolean isSuite() {
        return testClass != null && testClass.isSuite();
    }
}
