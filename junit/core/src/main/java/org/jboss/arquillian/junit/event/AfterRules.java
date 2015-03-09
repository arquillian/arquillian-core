package org.jboss.arquillian.junit.event;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;

/**
 *
 * Event fired After running the JUnit rules. LifecycleMethodExecutor does not
 * control anything. See BeforeRules if you want to stop rules invocation.
 *
 */
public class AfterRules extends AfterTestLifecycleEvent {

    /**
     * @param testInstance The test case instance being tested
     * @param testMethod The test method that is about to be executed
     * @param executor A call back when the LifecycleMethod represented by this event should be invoked
     */
    public AfterRules(TestClass testClass, Object testInstance, Method testMethod, LifecycleMethodExecutor executor)
    {
       super(testClass, testInstance, testMethod, executor);
    }

}
