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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.impl.loadable.util.FakeService;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Verify the behavior of the ServiceLoader exposed by the ServiceRegistry.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServiceRegistryLoaderTestCase extends AbstractManagerTestBase {
    @Inject
    private Instance<Injector> injector;

    @Test
    public void shouldBeAbleToLoadAll() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeExcluded.class);
        registry.addService(FakeService.class, ShouldBeIncluded.class);

        Collection<FakeService> services = registry.getServiceLoader().all(FakeService.class);

        Assert.assertNotNull(services);
        Assert.assertEquals(
            "Verify both services were loaded",
            2, services.size());

        for (FakeService service : services) {
            Assert.assertTrue(
                "Verify that the services are of the expected types",
                (service instanceof ShouldBeExcluded) || (service instanceof ShouldBeIncluded));
        }
    }

    @Test
    public void shouldBeAbleToLoadAllEvenIfNonRegistered() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());

        Collection<FakeService> services = registry.getServiceLoader().all(FakeService.class);

        Assert.assertNotNull(services);
        Assert.assertEquals(
            "Verify no services were loaded",
            0, services.size());
    }

    @Test
    public void shouldBeAbleToLoadOnlyOne() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeIncluded.class);

        FakeService service = registry.getServiceLoader().onlyOne(FakeService.class);

        Assert.assertNotNull(service);
        Assert.assertTrue(
            "Verify service is of expected type",
            service instanceof ShouldBeIncluded);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfMultipleFoundWhenTryingOnlyOne() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeIncluded.class);
        registry.addService(FakeService.class, ShouldBeExcluded.class);

        // throws exception
        registry.getServiceLoader().onlyOne(FakeService.class);
    }

    @Test
    public void shouldBeAbleToLoadDefaultIfNoneFound() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());

        Assert.assertNull(registry.getServiceLoader().onlyOne(FakeService.class));

        FakeService service = registry.getServiceLoader().onlyOne(FakeService.class, ShouldBeIncluded.class);
        Assert.assertNotNull(service);
        Assert.assertTrue(
            "Verify service is of expected type",
            service instanceof ShouldBeIncluded);
    }

    @Test
    public void loadedServicesShouldBeStaticallyInjected() throws Exception {
        bind(ApplicationScoped.class, String.class, "TEST");

        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());
        registry.addService(FakeService.class, ShouldBeIncluded.class);

        FakeService service = registry.getServiceLoader().onlyOne(FakeService.class);
        Assert.assertTrue(
            "Verify service has been statically injected",
            service.isValid());
    }

    /*
     * ARQ-528: Default services should also be statically injected
     */
    @Test
    public void loadedDefaultServicesShouldBeStaticallyInjected() throws Exception {
        bind(ApplicationScoped.class, String.class, "TEST");

        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());

        Assert.assertNull(registry.getServiceLoader().onlyOne(FakeService.class));

        FakeService service = registry.getServiceLoader().onlyOne(FakeService.class, ShouldBeIncluded.class);
        Assert.assertTrue(
            "Verify service has been statically injected",
            service.isValid());
    }

    /*
     * ARQ-1024: Protected services should be loadable
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldBeAbleToLoadProtectedServices() throws Exception {
        ServiceRegistry registry = new ServiceRegistry(injector.get(), new LinkedHashMap<Class<?>, Set<Class<?>>>());
        registry.addService(
            FakeService.class,
            (Class<FakeService>) Class.forName("org.jboss.arquillian.core.impl.loadable.util.PackageProtectedService"));

        FakeService service = registry.getServiceLoader().onlyOne(FakeService.class);
        Assert.assertNotNull("Could load package protected service", service);
    }
}
