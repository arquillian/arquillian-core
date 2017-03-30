/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.core.impl.threading;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.threading.ContextSnapshot;
import org.jboss.arquillian.core.impl.ManagerImpl;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.spi.context.IdBoundContext;
import org.jboss.arquillian.core.spi.context.NonIdBoundContext;

public class ThreadedExecutorService implements org.jboss.arquillian.core.api.threading.ExecutorService {

    private ExecutorService service;

    private ManagerImpl manager;
    private Injector injector;

    public ThreadedExecutorService(final ManagerImpl manager) {
        this.manager = manager;
        try {
            this.injector = manager.executeInApplicationContext(new Callable<Injector>() {
                @Override
                public Injector call() throws Exception {
                    return manager.resolve(Injector.class);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor().submit(wrap(injector.inject(task)));
    }

    @Override
    public ContextSnapshot createSnapshotContext() {
        return ContextualStateSnapshot.from(manager);
    }

    private ExecutorService executor() {
        if (this.service == null) {
            this.service = Executors.newCachedThreadPool();
        }
        return this.service;
    }

    private <T> Callable<T> wrap(Callable<T> callable) {
        return new ContextualCallable<T>(callable, createSnapshotContext());
    }

    private static class ContextualCallable<T> implements Callable<T> {
        private Callable<T> delegate;
        private ContextSnapshot state;

        public ContextualCallable(Callable<T> delegate, ContextSnapshot state) {
            this.delegate = delegate;
            this.state = state;
        }

        @Override
        public T call() throws Exception {
            try {
                state.activate();
                return delegate.call();
            } finally {
                state.deactivate();
            }
        }
    }

    public static class ContextualStateSnapshot implements ContextSnapshot {

        private Map<Context, Object> activeContexts;

        private ContextualStateSnapshot(Map<Context, Object> activeContexts) {
            this.activeContexts = activeContexts;
        }

        @SuppressWarnings("unchecked")
        private static ContextSnapshot from(ManagerImpl manager) {
            List<Context> contexts = manager.getContexts();
            Map<Context, Object> activeContexts = new HashMap<Context, Object>();
            for (Context context : contexts) {
                if (context.isActive()) {
                    if (context instanceof NonIdBoundContext) {
                        activeContexts.put(context, null);
                    } else {
                        activeContexts.put(context, ((IdBoundContext<Object>) context).getActiveId());
                    }
                }
            }
            return new ContextualStateSnapshot(activeContexts);
        }

        @SuppressWarnings("unchecked")
        public void activate() {
            for (Map.Entry<Context, Object> entry : activeContexts.entrySet()) {
                if (entry.getKey() instanceof NonIdBoundContext) {
                    ((NonIdBoundContext) entry.getKey()).activate();
                } else if (entry.getKey() instanceof IdBoundContext) {
                    ((IdBoundContext<Object>) entry.getKey()).activate(entry.getValue());
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void deactivate() {
            for (Map.Entry<Context, Object> entry : activeContexts.entrySet()) {
                if (entry.getKey() instanceof NonIdBoundContext) {
                    ((NonIdBoundContext) entry.getKey()).deactivate();
                } else if (entry.getKey() instanceof IdBoundContext) {
                    ((IdBoundContext<Object>) entry.getKey()).deactivate();
                }
            }
        }
    }
}
