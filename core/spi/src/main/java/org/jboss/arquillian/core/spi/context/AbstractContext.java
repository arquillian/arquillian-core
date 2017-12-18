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
package org.jboss.arquillian.core.spi.context;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.jboss.arquillian.core.spi.Validate;

/**
 * AbstractContext
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class AbstractContext<T> implements Context, IdBoundContext<T> {
    private static Logger log = Logger.getLogger("Context");

    private ConcurrentHashMap<T, ObjectStore> stores;

    private ThreadLocal<Stack<StoreHolder<T>>> activeStore = new ThreadLocal<Stack<StoreHolder<T>>>() {

        @Override
        protected Stack<StoreHolder<T>> initialValue() {
            return new Stack<StoreHolder<T>>();
        }
    };

    public AbstractContext() {
        stores = new ConcurrentHashMap<T, ObjectStore>();
    }

    @Override
    public T getActiveId() {
        if (isActive()) {
            return activeStore.get().peek().getId();
        }
        return null;
    }

    @Override
    public void activate(T id) {
        Validate.notNull(id, "ID must be specified");
        activeStore.get().push(new StoreHolder<T>(id, createObjectStore(id)));
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            activeStore.get().pop();
        } else {
            log.info("Trying to deactivate context, but none active: " + super.getClass().getSimpleName());
        }
    }

    public void deactivateAll() {
        if (isActive()) {
            activeStore.get().clear();
        }
    }

    @Override
    public boolean isActive() {
        return !activeStore.get().isEmpty();
    }

    @Override
    public void destroy(T id) {
        ObjectStore store = stores.remove(id);
        if (store != null) {
            store.clear();
        }
    }

    @Override
    public ObjectStore getObjectStore() {
        if (isActive()) {
            return activeStore.get().peek().getStore();
        }
        throw new RuntimeException("Context is not active: " + super.getClass().getSimpleName());
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.spi.context.Context#clearAll()
     */
    @Override
    public void clearAll() {
        synchronized (this) {
            if (isActive()) {
                deactivateAll();
            }
            activeStore.remove();
            for (Map.Entry<T, ObjectStore> entry : stores.entrySet()) {
                entry.getValue().clear();
            }
            stores.clear();
        }
    }

    protected abstract ObjectStore createNewObjectStore();

    //-------------------------------------------------------------------------------------||
    // Internal Helper Methods ------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    private ObjectStore createObjectStore(T id) {
        Validate.notNull(id, "ID must be specified");
        ObjectStore store = createNewObjectStore();
        ObjectStore previousStore = stores.putIfAbsent(id, store);
        if (previousStore != null) {
            return previousStore;
        }
        return store;
    }

    private class StoreHolder<X> {
        private X id;
        private ObjectStore store;

        public StoreHolder(X id, ObjectStore store) {
            this.id = id;
            this.store = store;
        }

        /**
         * @return the id
         */
        public X getId() {
            return id;
        }

        /**
         * @return the store
         */
        public ObjectStore getStore() {
            return store;
        }
    }
}
