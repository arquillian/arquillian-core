package org.jboss.arquillian.junit.event;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.junit.runners.model.TestClass;

/**
 *
 * Event fired Before running the JUnit rules. LifecycleMethodExecutor controls if
 * the Rules should be invoked or not.
 *
 */
public class BeforeRules extends BeforeTestLifecycleEvent {

    private Object statementInstance;
    private TestClass testClassInstance;

    /**
     * @param testInstance The test case instance being tested
     * @param testClassInstance The {@link TestClass} instance representing the test case
     * @param statementInstance The statement that is about to be taken at runtime in the course of running a JUnit test suite.
     * @param testMethod The test method that is about to be executed
     * @param executor A call back when the LifecycleMethod represented by this event should be invoked
     */
    public BeforeRules(Object testInstance, TestClass testClassInstance, Object statementInstance, Method testMethod,
        LifecycleMethodExecutor executor)
    {
        super(testInstance, testMethod, executor);
        this.statementInstance = statementInstance;
        this.testClassInstance = testClassInstance;
    }

    public Object getStatementInstance()
    {
        return statementInstance;
    }

    public TestClass getTestClassInstance()
    {
        return testClassInstance;
    }
}
