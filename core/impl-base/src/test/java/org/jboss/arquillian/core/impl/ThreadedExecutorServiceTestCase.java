package org.jboss.arquillian.core.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.threading.ExecutorService;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.core.test.context.ManagerTest2Context;
import org.jboss.arquillian.core.test.context.ManagerTest2ContextImpl;
import org.jboss.arquillian.core.test.context.ManagerTestContext;
import org.jboss.arquillian.core.test.context.ManagerTestContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class ThreadedExecutorServiceTestCase extends AbstractManagerTestBase {

    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(ManagerTestContextImpl.class);
        contexts.add(ManagerTest2ContextImpl.class);
    }

    @Inject
    private Instance<ExecutorService> serviceInst;

    @Test
    public void shouldReplicateContextualInformationToNewThread() throws Exception {
        ExecutorService service = serviceInst.get();

        Assert.assertFalse(getManager().getContext(ManagerTest2Context.class).isActive());

        getManager().getContext(ManagerTestContext.class).activate();

        Future<String> future = service.submit(new Callable<String>() {

            @Inject Event<String> event;

            @Override
            public String call() throws Exception {
                event.fire("C");
                return "Test";
            }
        });

        Assert.assertEquals("Test", future.get());
        assertEventFired(String.class, 1);

        // Only ManagerTestContext is active when submitted,
        // so only that Context should have been replicated.
        assertEventFiredInContext(String.class, ManagerTestContext.class);
        assertEventNotFiredInContext(String.class, ManagerTest2Context.class);
    }
}