/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.core.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.impl.ManagerImpl;
import org.jboss.arquillian.core.impl.UncheckedThrow;
import org.jboss.arquillian.core.impl.context.ApplicationContextImpl;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.core.spi.context.Context;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * AbstractManagerTestBase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public abstract class AbstractManagerTestBase {
    private static ManagerImpl manager;
    private static List<Class<? extends Context>> contexts;

    @Before
    public final void create() throws Exception {
        contexts = new ArrayList<Class<? extends Context>>();
        ManagerBuilder builder = ManagerBuilder.from();

        addContexts(contexts);
        for (Class<? extends Context> context : contexts) {
            builder.context(context);
        }

        // Add ApplicationContext, it's internal to Manager, but needs to be registered as a context so EventRecorder will pick up on it
        contexts.add(0, ApplicationContextImpl.class);

        builder.extension(EventRegisterObserver.class);

        List<Class<?>> extensions = new ArrayList<Class<?>>();
        addExtensions(extensions);
        for (Class<?> extension : extensions) {
            builder.extension(extension);
        }

        manager = (ManagerImpl) builder.create();
        beforeStartManager(manager);
        manager.start();

        executeInApplicationScope(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                manager.resolve(Injector.class).inject(AbstractManagerTestBase.this);
                return null;
            }
        });
        startContexts(manager);
    }

    @After
    public final void destory() {
        manager.shutdown();
        manager = null;
    }

    public ManagerImpl getManager() {
        return manager;
    }

    //-------------------------------------------------------------------------------------||
    // Assertions and Helper operations ----------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    public final void fire(Object event) {
        manager.fire(event);
    }

    public final <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance) {
        manager.bind(scope, type, instance);
    }

    public final void assertEventFired(Class<?> type) {
        Assert.assertNotNull(
            "Event " + type.getName() + " should have been fired",
            getRegister().getCount(type));
    }

    public final void assertEventFired(Class<?> type, Integer count) {
        Assert.assertEquals(
            "The event of exact type " + type.getName() + " should have been fired",
            count,
            getRegister().getCount(type));
    }

    public final void assertEventFiredTyped(Class<?> type, Integer count) {
        Assert.assertEquals(
            "The event of assiganble type to " + type.getName() + " should have been fired",
            count,
            getRegister().getCountTyped(type));
    }

    public final void assertEventFiredInContext(Class<?> type, Class<? extends Context> activeContext) {
        assertEventInContext(type, activeContext, true);
    }

    public final void assertEventNotFiredInContext(Class<?> type, Class<? extends Context> activeContext) {
        assertEventInContext(type, activeContext, false);
    }

    private void assertEventInContext(Class<?> type, Class<? extends Context> activeContext, Boolean active) {
        Assert.assertEquals(
            "Event "
                + type.getName()
                + " should"
                + (active ? " " : " not")
                + " have been fired within context "
                + activeContext.getName(),
            active,
            getRegister().wasActive(type, activeContext));
    }

    public final void assertEventFiredOnOtherThread(Class<?> type) {
        Assert.assertTrue(
            "The event of type to " + type.getName() + " should have been fired on a different thread",
            getRegister().wasExecutedOnNonMainThread(type));
    }

    private EventRegister getRegister() {
        try {
            return executeInApplicationScope(new Callable<EventRegister>() {
                @Override
                public EventRegister call() throws Exception {
                    return manager.resolve(EventRegister.class);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //-------------------------------------------------------------------------------------||
    // Extendables ------------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    protected void beforeStartManager(Manager manager) {
    }

    protected void addExtensions(List<Class<?>> extensions) {
    }

    protected void addContexts(List<Class<? extends Context>> contexts) {
    }

    protected void startContexts(Manager manager) {
        manager.getContext(ApplicationContext.class).activate();
    }

    //-------------------------------------------------------------------------------------||
    // Internal Helpers - Track events ----------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    public <T> T executeInApplicationScope(Callable<T> op) throws Exception {
        return manager.executeInApplicationContext(op);
    }

    public static class EventRegisterObserver {
        @Inject
        @ApplicationScoped
        private InstanceProducer<EventRegister> register;

        @SuppressWarnings("unchecked")
        public synchronized void register(@Observes Object event) {
            if (register.get() == null) {
                register.set(new EventRegister());
            }

            // TODO: looking up the static manager is a hack get to the Contexts dynamically. Manager can be null since events are fired during Manager creation
            if (manager == null) {
                return;
            }
            EventRegister reg = register.get();

            EventRecording rec = new EventRecording();

            for (Class<? extends Context> context : contexts) {
                Class<? extends Context> contextInterface = (Class<? extends Context>) context.getInterfaces()[0];

                rec.add(contextInterface, manager.getContext(contextInterface).isActive());
            }
            reg.add(event.getClass(), rec);
            if (event instanceof Throwable) {
                // we are listening to Object which is not really a good thing, so throw exceptions if found.
                UncheckedThrow.throwUnchecked((Throwable) event);
            }
        }
    }

    public static class EventRegister {
        private Map<Class<?>, List<EventRecording>> events;

        public EventRegister() {
            events = new HashMap<Class<?>, List<EventRecording>>();
        }

        public void add(Class<?> type, EventRecording recording) {
            if (events.get(type) == null) {
                List<EventRecording> recordings = new ArrayList<EventRecording>();
                recordings.add(recording);
                events.put(type, recordings);
            } else {
                events.get(type).add(recording);
            }
        }

        /**
         * Get the count of a assignable count.
         *
         * @param type
         *     The assignable event type
         *
         * @return Number of times fired
         */
        public Integer getCountTyped(Class<?> type) {
            int count = 0;
            for (Map.Entry<Class<?>, List<EventRecording>> recordingEntry : events.entrySet()) {
                if (type.isAssignableFrom(recordingEntry.getKey())) {
                    count += recordingEntry.getValue().size();
                }
            }
            return count;
        }

        /**
         * Get the count of a specific type.
         *
         * @param type
         *     The exact event type
         *
         * @return Number of times fired
         */
        public Integer getCount(Class<?> type) {
            return events.containsKey(type) ? events.get(type).size() : 0;
        }

        public Boolean wasExecutedOnNonMainThread(Class<?> type) {
            if (getCount(type) == 0) {
                return false;
            }
            for (EventRecording recording : events.get(type)) {
                if ("main".equalsIgnoreCase(recording.getThreadName())) {
                    return false;
                }
            }
            return true;
        }

        public Boolean wasActive(Class<?> type, Class<? extends Context> context) {
            if (getCount(type) == 0) {
                return false;
            }
            for (EventRecording recording : events.get(type)) {
                if (!recording.wasActive(context)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class EventRecording {
        private Map<Class<? extends Context>, Boolean> activeContexts;
        private String threadName;

        EventRecording() {
            threadName = Thread.currentThread().getName();
            activeContexts = new HashMap<Class<? extends Context>, Boolean>();
        }

        public String getThreadName() {
            return threadName;
        }

        public EventRecording add(Class<? extends Context> context, Boolean isActive) {
            activeContexts.put(context, isActive);
            return this;
        }

        public Boolean wasActive(Class<? extends Context> context) {
            if (activeContexts.get(context) != null) {
                return activeContexts.get(context);
            }
            return false;
        }
    }
}
