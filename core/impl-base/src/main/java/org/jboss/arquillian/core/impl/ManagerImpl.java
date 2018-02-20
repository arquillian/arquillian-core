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
package org.jboss.arquillian.core.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.api.event.ManagerStopping;
import org.jboss.arquillian.core.api.threading.ExecutorService;
import org.jboss.arquillian.core.impl.context.ApplicationContextImpl;
import org.jboss.arquillian.core.impl.threading.ThreadedExecutorService;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.EventPoint;
import org.jboss.arquillian.core.spi.Extension;
import org.jboss.arquillian.core.spi.InjectionPoint;
import org.jboss.arquillian.core.spi.InvocationException;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.NonManagedObserver;
import org.jboss.arquillian.core.spi.ObserverMethod;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.spi.context.ObjectStore;
import org.jboss.arquillian.core.spi.event.ManagerProcessing;

/**
 * ManagerImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagerImpl implements Manager {
    //-------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    private final RuntimeLogger runtimeLogger;
    private final List<Context> contexts;
    private final List<Extension> extensions;
    /*
     * Hack:
     * Events can be fired nested. If a nested handler throws a exception, the exception is fired on the bus for handling.
     * It is up to the exception handler to re-throw the exception if it can't handle it. When the re-throw happens
     * the higher level event will get the exception and re-fire it on the bus. We need to keep track of which exceptions
     * has been handled in the call chain so we can re-throw without re-firing on a higher level.
     */
    private ThreadLocal<Set<Class<? extends Throwable>>> handledThrowables =
        new ThreadLocal<Set<Class<? extends Throwable>>>() {
            @Override
            protected Set<Class<? extends Throwable>> initialValue() {
                return new HashSet<Class<? extends Throwable>>();
            }
        };

    ManagerImpl(final Collection<Class<? extends Context>> contextClasses, final Collection<Class<?>> extensionClasses) {
        this.contexts = new ArrayList<Context>();
        this.extensions = new ArrayList<Extension>();
        this.runtimeLogger = new RuntimeLogger();

        try {
            List<Extension> createdExtensions = createExtensions(extensionClasses);
            List<Context> createdContexts = createContexts(contextClasses);

            createBuiltInServices();

            this.contexts.addAll(createdContexts);
            this.extensions.addAll(createdExtensions);

            addContextsToApplicationScope();
            fireProcessing();
            addContextsToApplicationScope();
        } catch (Exception e) {
            throw new RuntimeException("Could not create and process manager", e);
        }
    }

    //-------------------------------------------------------------------------------------||
    // Required Implementations - Manager -------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    @Override
    public void fire(Object event) {
        fire(event, null);
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.impl.core.spi.Manager#fire(java.lang.Object, org.jboss.arquillian.impl.core.spi.NonManagedObserver)
     */
    @Override
    public <T> void fire(T event, NonManagedObserver<T> nonManagedObserver) {
        Validate.notNull(event, "Event must be specified");

        runtimeLogger.debug(event, true);
        // we start fresh pr new event
        handledThrowables.get().clear();

        List<ObserverMethod> observers = resolveObservers(event.getClass());
        List<ObserverMethod> interceptorObservers = resolveInterceptorObservers(event.getClass());

        ApplicationContext context = (ApplicationContext) getScopedContext(ApplicationScoped.class);
        // We need to know if we were to the one to Activate it to avoid:
        // * nested ApplicationContexts
        // * ending the scope to soon (to low in the stack)
        boolean activatedApplicationContext = false;
        try {
            if (!context.isActive()) {
                context.activate();
                activatedApplicationContext = true;
            }
            new EventContextImpl<T>(this, interceptorObservers, observers, nonManagedObserver, event,
                runtimeLogger).proceed();
        } catch (Exception e) {
            Throwable fireException = e;
            if (fireException instanceof InvocationException) {
                fireException = fireException.getCause();
            }
            if (handledThrowables.get().contains(fireException.getClass())) {
                UncheckedThrow.throwUnchecked(fireException);
            } else {
                fireException(fireException);
            }
        } finally {
            runtimeLogger.debug(event, false);
            if (activatedApplicationContext && context.isActive()) {
                context.deactivate();
            }
        }
    }

    @Override
    public <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance) {
        Validate.notNull(scope, "Scope must be specified");
        Validate.notNull(type, "Type must be specified");
        Validate.notNull(instance, "Instance must be specified");

        Context scopedContext = getScopedContext(scope);
        if (scopedContext == null) {
            throw new IllegalArgumentException("No Context registered with support for scope: " + scope);
        }
        if (!scopedContext.isActive()) {
            throw new IllegalArgumentException("No active " + scope.getSimpleName() + " Context to bind to");
        }
        scopedContext.getObjectStore().add(type, instance);
    }

    @Override
    public <T> T resolve(Class<T> type) {
        Validate.notNull(type, "Type must be specified");
        List<Context> activeContexts = resolveActiveContexts();
        for (int i = activeContexts.size() - 1; i >= 0; i--) {
            Context context = activeContexts.get(i);
            T object = context.getObjectStore().get(type);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    @Override
    public void inject(Object obj) {
        inject(ExtensionImpl.of(obj));
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.Manager#getContext(java.lang.Class)
     */
    @Override
    public <T> T getContext(Class<T> type) {
        for (Context context : contexts) {
            if (type.isInstance(context)) {
                return type.cast(context);
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------------------||
    // Exposed Convenience Impl Methods ---------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    public <T> T executeInApplicationContext(Callable<T> callable) throws Exception {
        ApplicationContext context = (ApplicationContext) getScopedContext(ApplicationScoped.class);
        boolean activatedByUs = false;
        try {
            if (!context.isActive()) {
                context.activate();
                activatedByUs = true;
            }
            return callable.call();
        } finally {
            if (activatedByUs && context.isActive()) {
                context.deactivate();
            }
        }
    }

    public List<Context> getContexts() {
        return Collections.unmodifiableList(contexts);
    }

    /**
     * @param <T>
     * @param scope
     * @param type
     * @param instance
     */
    public <T> void bindAndFire(Class<? extends Annotation> scope, Class<T> type, T instance) {
        bind(scope, type, instance);
        fire(instance);
    }

    /**
     * @return the extensions
     */
    public <T> T getExtension(Class<T> type) {
        for (Extension extension : extensions) {
            Object target = ((ExtensionImpl) extension).getTarget();
            if (type.isInstance(target)) {
                return type.cast(target);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.Manager#start()
     */
    @Override
    public void start() {
        fire(new ManagerStarted());
        getContext(ApplicationContext.class).activate();
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.Manager#shutdown()
     */
    @Override
    public void shutdown() {
        Throwable shutdownException = null;
        try {
            fire(new ManagerStopping());
        } catch (Exception e) {
            try {
                fireException(e);
            } catch (Exception e2) {
                shutdownException = e2;
            }
        }
        synchronized (this) {
            for (Context context : contexts) {
                context.clearAll();
            }
            contexts.clear();
            extensions.clear();

            runtimeLogger.clear();

            handledThrowables.remove();
        }
        if (shutdownException != null) {
            UncheckedThrow.throwUnchecked(shutdownException);
        }
    }

    //-------------------------------------------------------------------------------------||
    // Internal Helper Methods ------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    public void fireProcessing() throws Exception {
        final Set<Class<?>> extensions = new HashSet<Class<?>>();
        final Set<Class<? extends Context>> contexts = new HashSet<Class<? extends Context>>();
        fire(new ManagerProcessing() {
            @Override
            public ManagerProcessing observer(Class<?> observer) {
                if (extensions.contains(observer)) {
                    throw new IllegalArgumentException(
                        "Attempted to register the same Observer: " + observer.getName()
                            + " multiple times, please check classpath for conflicting jar versions");
                }
                extensions.add(observer);
                return this;
            }

            @Override
            public ManagerProcessing context(Class<? extends Context> context) {
                if (contexts.contains(context)) {
                    throw new IllegalArgumentException(
                        "Attempted to register the same " + Context.class.getSimpleName() + " : " + context.getName()
                            + " multiple times, please check classpath for conflicting jar versions");
                }
                contexts.add(context);
                return this;
            }
        });

        this.extensions.addAll(createExtensions(extensions));
        this.contexts.addAll(createContexts(contexts));
    }

    public void addExtension(Class<?> extensionClass) throws Exception {
        runtimeLogger.debugExtension(extensionClass);
        ExtensionImpl newExtension = ExtensionImpl.of(Reflections.createInstance(extensionClass));
        inject(newExtension);
        extensions.add(newExtension);
    }

    public void removeExtension(Class<?> extensionClass) {
        for (Extension extension : extensions) {
            Object target = ((ExtensionImpl) extension).getTarget();
            if (extensionClass.isInstance(target)) {
                extensions.remove(extension);
                break;
            }
        }
    }

    boolean isExceptionHandled(Throwable e) {
        return handledThrowables.get().contains(e.getClass());
    }

    void fireException(Throwable event) {
        runtimeLogger.debug(event, true);
        try {
            List<ObserverMethod> observers = resolveObservers(event.getClass());
            if (observers.size() == 0) // no one is handling this Exception, throw it out.
            {
                UncheckedThrow.throwUnchecked(event);
            }
            for (int i = 0; i < observers.size(); i++) {
                ObserverMethod observer = observers.get(i);
                try {
                    runtimeLogger.debug(observer, false);
                    observer.invoke(this, event);
                } catch (Exception e) {
                    // getCause(InocationTargetException).getCause(RealCause);
                    Throwable toBeFired = e.getCause();
                    // same type of exception being fired as caught and is the last observer, throw to avoid loop
                    if (toBeFired.getClass() == event.getClass()) {
                        // on throw if this is the last Exception observer
                        if (i == observers.size() - 1) {
                            handledThrowables.get().add(toBeFired.getClass());
                            // this will throw checked exception if any, and will break the declaration of fire(), will throw the original cause
                            UncheckedThrow.throwUnchecked(toBeFired);
                        }
                    } else {
                        // a new exception was raised, throw
                        fireException(toBeFired);
                    }
                }
            }
        } finally {
            runtimeLogger.debug(event, false);
        }
    }

    /**
     * @param extensions
     * @return
     */
    private List<Extension> createExtensions(Collection<Class<?>> extensionClasses) throws Exception {
        List<Extension> created = new ArrayList<Extension>();
        for (Class<?> extensionClass : extensionClasses) {
            Extension extension = ExtensionImpl.of(Reflections.createInstance(extensionClass));
            inject(extension);
            created.add(extension);
            runtimeLogger.debugExtension(extensionClass);
        }
        return created;
    }

    /**
     * @param contexts2
     * @return
     */
    private List<Context> createContexts(Collection<Class<? extends Context>> contextClasses) throws Exception {
        List<Context> created = new ArrayList<Context>();
        for (Class<? extends Context> contextClass : contextClasses) {
            created.add(Reflections.createInstance(contextClass));
        }
        return created;
    }

    private void createBuiltInServices() throws Exception {
        final ApplicationContext context = new ApplicationContextImpl();
        contexts.add(context);
        executeInApplicationContext(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ManagerImpl.this.bind(
                    ApplicationScoped.class, Injector.class, InjectorImpl.of(ManagerImpl.this));
                ManagerImpl.this.bind(
                    ApplicationScoped.class, ExecutorService.class, new ThreadedExecutorService(ManagerImpl.this));
                return null;
            }
        });
    }

    /**
     * @param objectStore
     */
    @SuppressWarnings("unchecked")
    private void addContextsToApplicationScope() throws Exception {
        executeInApplicationContext(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ApplicationContext appContext = getContext(ApplicationContext.class);
                ObjectStore store = appContext.getObjectStore();

                for (Context context : contexts) {
                    store.add((Class<Context>) context.getClass().getInterfaces()[0], context);
                }
                return null;
            }
        });
    }

    /**
     * @param eventType
     * @return
     */
    private List<ObserverMethod> resolveObservers(Class<?> eventType) {
        List<ObserverMethod> observers = new ArrayList<ObserverMethod>();
        for (Extension extension : extensions) {
            for (ObserverMethod observer : extension.getObservers()) {
                if (Reflections.getType(observer.getType()).isAssignableFrom(eventType) && !Reflections.isType(
                    observer.getType(), EventContext.class)) {
                    observers.add(observer);
                }
            }
        }
        Collections.sort(observers);
        return observers;
    }

    private List<ObserverMethod> resolveInterceptorObservers(Class<?> eventType) {
        List<ObserverMethod> observers = new ArrayList<ObserverMethod>();
        for (Extension extension : extensions) {
            for (ObserverMethod observer : extension.getObservers()) {
                if (Reflections.isType(observer.getType(), EventContext.class)) {
                    if (Reflections.getType(observer.getType()).isAssignableFrom(eventType)) {
                        observers.add(observer);
                    }
                }
            }
        }
        Collections.sort(observers);
        return observers;
    }

    private List<Context> resolveActiveContexts() {
        List<Context> activeContexts = new ArrayList<Context>();
        for (Context context : contexts) {
            if (context.isActive()) {
                activeContexts.add(context);
            }
        }
        return activeContexts;
    }

    private void inject(Extension extension) {
        injectInstances(extension);
        injectEvents(extension);
    }

    /**
     * @param extension
     */
    private void injectInstances(Extension extension) {
        for (InjectionPoint point : extension.getInjectionPoints()) {
            point.set(InstanceImpl.of(Reflections.getType(point.getType()), point.getScope(), this));
        }
    }

    /**
     * @param extension
     */
    private void injectEvents(Extension extension) {
        for (EventPoint point : extension.getEventPoints()) {
            point.set(EventImpl.of(Reflections.getType(point.getType()), this));
        }
    }

    private Context getScopedContext(Class<? extends Annotation> scope) {
        for (Context context : contexts) {
            if (context.getScope() == scope) {
                return context;
            }
        }
        return null;
    }
}
