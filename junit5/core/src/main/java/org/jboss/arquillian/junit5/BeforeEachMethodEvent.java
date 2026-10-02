package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;

/**
 * An event fired around the invocation of a single {@code @BeforeEach} method. Firing it activates the same contexts
 * as the {@link org.jboss.arquillian.test.spi.event.suite.Before} event and invokes the executor where the
 * lifecycle methods are to be executed, without firing the {@link org.jboss.arquillian.test.spi.event.suite.Before}
 * event itself again for every {@code @BeforeEach} method.
 *
 * @author Radoslav Husar
 */
class BeforeEachMethodEvent extends BeforeTestLifecycleEvent {

    /**
     * Creates a new event.
     *
     * @param testInstance The test case instance
     * @param testMethod   The test method
     * @param executor     A call back to invoke the {@code @BeforeEach} method
     */
    BeforeEachMethodEvent(final Object testInstance, final Method testMethod, final LifecycleMethodExecutor executor) {
        super(testInstance, testMethod, executor);
    }
}
