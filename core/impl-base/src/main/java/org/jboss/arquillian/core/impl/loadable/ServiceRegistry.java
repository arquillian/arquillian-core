/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.core.impl.loadable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * ServiceRegistry
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Davide D'Alto
 * @version $Revision: $
 */
public class ServiceRegistry {
    private final Injector injector;
    private final Map<Class<?>, Set<Class<?>>> registry;
    private final Map<Class<?>, Set<Class<?>>> vetoed;

    public ServiceRegistry(Injector injector, Map<Class<?>, Set<Class<?>>> vetoed) {
        this.registry = new HashMap<Class<?>, Set<Class<?>>>();
        this.vetoed = new HashMap<Class<?>, Set<Class<?>>>(vetoed);
        this.injector = injector;
    }

    public <T> void addService(Class<T> service, Class<? extends T> serviceImpl) {
        synchronized (registry) {
            if (isImplementationVetoed(service, serviceImpl)) {
                return;
            }

            Set<Class<?>> registeredImpls = registry.get(service);
            if (registeredImpls == null) {
                registeredImpls = new HashSet<Class<?>>();
            }
            registeredImpls.add(serviceImpl);
            registry.put(service, registeredImpls);
        }
    }

    public <T> void removeService(Class<T> service, Class<? extends T> serviceImpl) {
        synchronized (registry) {
            Set<Class<?>> registeredImpls = registry.get(service);
            if (registeredImpls == null) {
                return;
            }

            registeredImpls.remove(serviceImpl);
        }
    }

    public <T> void overrideService(Class<T> service, Class<? extends T> oldServiceImpl,
        Class<? extends T> newServiceImpl) {
        synchronized (registry) {

            if (isImplementationVetoed(service, newServiceImpl)) {
                return;
            }

            Set<Class<?>> vetoedImpls = vetoed.get(service);
            if (vetoedImpls == null) {
                vetoedImpls = new HashSet<Class<?>>();
                vetoed.put(service, vetoedImpls);
            }
            vetoedImpls.add(oldServiceImpl);

            removeService(service, oldServiceImpl);
            addService(service, newServiceImpl);
        }
    }

    public <T> Set<Class<? extends T>> getServiceImpls(Class<T> service) {
        Set<Class<?>> registeredImpls = registry.get(service);
        Set<Class<? extends T>> typedImpls = new HashSet<Class<? extends T>>();
        if (registeredImpls == null) {
            return typedImpls;
        }
        for (Class<?> registeredImpl : registeredImpls) {
            typedImpls.add(registeredImpl.asSubclass(service));
        }

        return typedImpls;
    }

    private <T> boolean isImplementationVetoed(Class<?> service, Class<? extends T> serviceImpl) {
        Set<Class<?>> vetoedImpls = vetoed.get(service);
        return vetoedImpls != null && vetoedImpls.contains(serviceImpl);
    }

    public void clear() {
        registry.clear();
    }

    public ServiceLoader getServiceLoader() {
        return new ServiceRegistryLoader(injector, this);
    }
}
