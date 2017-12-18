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
package org.jboss.arquillian.core.impl.loadable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.core.impl.loadable.util.FakeService;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for ServiceRegistry.
 *
 * @author "Davide D'Alto"
 * @version $Revision: $
 */
public class ServiceRegistryTestCase {
    @Test
    public void shouldBeAbleToAddImplementations() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(null, new HashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        registry.addService(FakeService.class, ShouldBeIncluded.class);
        Set<Class<? extends FakeService>> serviceImpls = registry.getServiceImpls(FakeService.class);

        Assert.assertEquals("Unexpected number of service implementations registered", 2, serviceImpls.size());
        Assert.assertTrue("Should contain ShouldBeIncluded class", serviceImpls.contains(ShouldBeIncluded.class));
        Assert.assertTrue("Should contain ShouldBeExcluded class", serviceImpls.contains(ShouldBeExcluded.class));
    }

    @Test
    public void shouldBeAbleToRemoveImplementation() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(null, new HashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        registry.addService(FakeService.class, ShouldBeIncluded.class);
        registry.removeService(FakeService.class, ShouldBeExcluded.class);
        Set<Class<? extends FakeService>> serviceImpls = registry.getServiceImpls(FakeService.class);

        Assert.assertEquals("Unexpected number of service implementations registered", 1, serviceImpls.size());
        Assert.assertEquals("Should contain ShouldBeIncluded class", ShouldBeIncluded.class,
            serviceImpls.iterator().next());
    }

    @Test
    public void shouldBeAbleToOverrideImplementation() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(null, new HashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        registry.overrideService(FakeService.class, ShouldBeExcluded.class, ShouldBeIncluded.class);
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        Set<Class<? extends FakeService>> serviceImpls = registry.getServiceImpls(FakeService.class);

        Assert.assertEquals("Unexpected number of service implementations registered", 1, serviceImpls.size());
        Assert.assertEquals("Should contain ShouldBeIncluded class", ShouldBeIncluded.class,
            serviceImpls.iterator().next());
    }

    @Test
    public void shouldBeAbleToNotAddVetoedServices() throws Exception {
        final Map<Class<?>, Set<Class<?>>> vetoed = new HashMap<Class<?>, Set<Class<?>>>();
        final Set<Class<?>> vetoedServiceImpls = new LinkedHashSet<Class<?>>();
        vetoedServiceImpls.add(ShouldBeExcluded.class);
        vetoed.put(FakeService.class, vetoedServiceImpls);

        ServiceRegistry registry = new ServiceRegistry(null, vetoed);
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        registry.addService(FakeService.class, ShouldBeIncluded.class);
        Set<Class<? extends FakeService>> serviceImpls = registry.getServiceImpls(FakeService.class);

        Assert.assertEquals("Unexpected number of service implementations registered", 1, serviceImpls.size());
        Assert.assertTrue("Should contain ShouldBeIncluded class", serviceImpls.contains(ShouldBeIncluded.class));
    }
}
