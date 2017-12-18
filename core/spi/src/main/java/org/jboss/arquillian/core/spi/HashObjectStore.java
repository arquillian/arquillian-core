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
package org.jboss.arquillian.core.spi;

import java.util.concurrent.ConcurrentHashMap;
import org.jboss.arquillian.core.spi.context.ObjectStore;

/**
 * ObjectStore
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class HashObjectStore implements ObjectStore {
    private ConcurrentHashMap<Class<?>, Object> store;

    public HashObjectStore() {
        store = new ConcurrentHashMap<Class<?>, Object>();
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.impl.ObjectStore#add(java.lang.Class, T)
     */
    @Override
    public <T> ObjectStore add(Class<T> type, T instance) {
        Validate.notNull(type, "Type must be specified");
        Validate.notNull(instance, "Instance must be specified");

        store.put(type, instance);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.impl.ObjectStore#get(java.lang.Class)
     */
    @Override
    public <T> T get(Class<T> type) {
        Validate.notNull(type, "Type must be specified");

        return type.cast(store.get(type));
    }

    /* (non-Javadoc)
     * @see org.jboss.arquillian.core.impl.ObjectStore#clear()
     */
    @Override
    public ObjectStore clear() {
        store.clear();
        return this;
    }
}
