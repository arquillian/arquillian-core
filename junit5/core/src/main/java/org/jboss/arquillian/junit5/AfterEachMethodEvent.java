package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;

/**
 * An event fired around the invocation of a single {@code @AfterEach} method. Firing it activates the same contexts
 * as the {@link org.jboss.arquillian.test.spi.event.suite.After} event and invokes the executor where the
 * lifecycle methods are to be executed, without firing the {@link org.jboss.arquillian.test.spi.event.suite.After}
 * event itself again for every {@code @AfterEach} method.
 *
 * @author Radoslav Husar
 */
class AfterEachMethodEvent extends AfterTestLifecycleEvent {

    /**
     * Creates a new event.
     *
     * @param testInstance The test case instance
     * @param testMethod   The test method
     * @param executor     A call back to invoke the {@code @AfterEach} method
     */
    AfterEachMethodEvent(final Object testInstance, final Method testMethod, final LifecycleMethodExecutor executor) {
        super(testInstance, testMethod, executor);
    }
}
