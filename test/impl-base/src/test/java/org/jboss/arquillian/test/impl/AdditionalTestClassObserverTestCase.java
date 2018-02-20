package org.jboss.arquillian.test.impl;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Assert;
import org.junit.Test;

public class AdditionalTestClassObserverTestCase extends AbstractTestTestBase {

    @Test
    public void should_add_observers_to_list_of_extensions() throws Exception {
        // given
        EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(getManager());

        // when
        adaptor.beforeClass(TestClassWithTwoObservers.class, LifecycleMethodExecutor.NO_OP);

        // then
        verifyObserverPresence(ObserverClass.class, true);
        verifyObserverPresence(EmptyObserverClass.class, true);
    }

    @Test
    public void should_remove_observers_from_list_of_extensions_in_after_class_phase() throws Exception {
        // given
        EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(getManager());
        adaptor.beforeClass(TestClassWithTwoObservers.class, LifecycleMethodExecutor.NO_OP);

        // when
        adaptor.afterClass(TestClassWithTwoObservers.class, LifecycleMethodExecutor.NO_OP);

        // then
        verifyObserverPresence(ObserverClass.class, false);
        verifyObserverPresence(EmptyObserverClass.class, false);
    }

    @Test
    public void should_invoke_additionally_added_observer_and_inject_value_to_variable() throws Exception {
        // given
        EventTestRunnerAdaptor adaptor = new EventTestRunnerAdaptor(getManager());
        adaptor.beforeClass(TestClassWithTwoObservers.class, LifecycleMethodExecutor.NO_OP);
        InvokedInfo invokedInfo = new InvokedInfo();
        getManager().bind(ClassScoped.class, InvokedInfo.class, invokedInfo);

        // when
        getManager().fire(new MyEvent());

        // then
        Assert.assertTrue("The observer should be invoked but wasn't", invokedInfo.isInvoked());
    }

    private void verifyObserverPresence(Class<?> observerClass, boolean shouldBePresent) {
        Object observer = getManager().getExtension(observerClass);
        if (shouldBePresent) {
            Assert.assertNotNull("the observer of type " + observerClass.getName() + " should be present", observer);
        } else {
            Assert.assertNull("the observer of type " + observerClass.getName() + " should NOT be present", observer);
        }
    }

    @Observer({EmptyObserverClass.class, ObserverClass.class})
    private static class TestClassWithTwoObservers {
    }

    private static class ObserverClass {
        @Inject
        private Instance<InvokedInfo> invokedInfoInstance;

        void observe(@Observes MyEvent event) {
            invokedInfoInstance.get().setInvoked(true);
        }
    }

    private static class EmptyObserverClass {
    }

    private static class MyEvent {
    }

    private static class InvokedInfo {

        private boolean invoked = false;

        private boolean isInvoked() {
            return invoked;
        }

        private void setInvoked(boolean invoked) {
            this.invoked = invoked;
        }
    }
}
