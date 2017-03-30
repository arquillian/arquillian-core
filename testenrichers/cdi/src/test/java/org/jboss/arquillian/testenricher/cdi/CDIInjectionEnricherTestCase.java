/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testenricher.cdi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.testenricher.cdi.beans.Cat;
import org.jboss.arquillian.testenricher.cdi.beans.CatService;
import org.jboss.arquillian.testenricher.cdi.beans.Dog;
import org.jboss.arquillian.testenricher.cdi.beans.DogService;
import org.jboss.arquillian.testenricher.cdi.beans.Service;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.manager.api.WeldManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CDIInjectionEnricherTestCase extends AbstractTestTestBase {
    private WeldBootstrap bootstrap;
    private WeldManager manager;
    private CDIInjectionEnricher enricher;

    @org.jboss.arquillian.core.api.annotation.Inject
    private org.jboss.arquillian.core.api.Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(CreationalContextDestroyer.class);
    }

    @Before
    public void setup() throws Exception {
        Deployment deployment = createDeployment(Service.class, Cat.class, CatService.class, Dog.class, DogService.class);
        bootstrap = new WeldBootstrap();
        bootstrap.startContainer(Environments.SE, deployment)
            .startInitialization()
            .deployBeans()
            .validateBeans()
            .endInitialization();

        manager = bootstrap.getManager(deployment.getBeanDeploymentArchives().iterator().next());

        bind(TestScoped.class, BeanManager.class, manager);

        enricher = new CDIInjectionEnricher();
        injector.get().inject(enricher);
    }

    @After
    public void teardown() throws Exception {
        bootstrap.shutdown();
    }

    @Test
    public void shouldInjectClassMembers() throws Exception {
        TestClass testClass = new TestClass();
        enricher.injectClass(testClass);
        testClass.testMethod(testClass.dogService, testClass.catService);
    }

    @Test
    public void shouldInjectMethodArguments() throws Exception {
        Method testMethod = TestClass.class.getMethod("testMethod", Service.class, Service.class);

        Object[] resolvedBeans = enricher.resolve(testMethod);

        TestClass testClass = new TestClass();
        testMethod.invoke(testClass, resolvedBeans);
    }

    @Test
    public void shouldInjectMethodArgumentsEvent() throws Exception {
        Method testMethod = TestClass.class.getMethod("testEvent", Event.class, Event.class);

        Object[] resolvedBeans = enricher.resolve(testMethod);

        TestClass testClass = new TestClass();
        testMethod.invoke(testClass, resolvedBeans);
    }

    @Test
    public void shouldReleaseCreationalContext() throws Exception {
        TestClass testClass = new TestClass();
        enricher.injectClass(testClass);

        fire(new org.jboss.arquillian.test.spi.event.suite.After(this, TestClass.class.getMethod("validateReleased")));
        testClass.validateReleased();
    }

    @Test
    public void shouldInjectMethodArgumentsInstance() throws Exception {
        Method testMethod = TestClass.class.getMethod("testInstance", Instance.class, Instance.class);

        Object[] resolvedBeans = enricher.resolve(testMethod);

        TestClass testClass = new TestClass();
        testMethod.invoke(testClass, resolvedBeans);
    }

    private Deployment createDeployment(final Class<?>... classes) {
        final BeanDeploymentArchive beanArchive = new BeanDeploymentArchive() {
            private ServiceRegistry registry = new SimpleServiceRegistry();

            public ServiceRegistry getServices() {
                return registry;
            }

            public String getId() {
                return "test.jar";
            }

            public Collection<EjbDescriptor<?>> getEjbs() {
                return Collections.emptyList();
            }

            public BeansXml getBeansXml() {
                try {
                    Collection<URL> beansXmlPaths =
                        Collections.singletonList(new URL(null, "archive://beans.xml", new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) throws IOException {
                                return new URLConnection(u) {
                                    public void connect() throws IOException {
                                    }

                                    public InputStream getInputStream() throws IOException {
                                        return new ByteArrayInputStream("<beans/>".getBytes());
                                    }
                                };
                            }
                        }));
                    return bootstrap.parse(beansXmlPaths);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
                return Collections.emptyList();
            }

            public Collection<String> getBeanClasses() {
                Collection<String> beanClasses = new ArrayList<String>();
                for (Class<?> c : classes) {
                    beanClasses.add(c.getName());
                }
                return beanClasses;
            }
        };
        final Deployment deployment = new Deployment() {
            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
                return Collections.singletonList(beanArchive);
            }

            public ServiceRegistry getServices() {
                return beanArchive.getServices();
            }

            public BeanDeploymentArchive loadBeanDeploymentArchive(
                Class<?> beanClass) {
                return beanArchive;
            }

            public Iterable<Metadata<Extension>> getExtensions() {
                return Collections.emptyList();
            }
        };
        return deployment;
    }

    private static class TestClass {
        @Inject
        Service<Dog> dogService;

        @Inject
        Service<Cat> catService;

        public void validateReleased() {
            Assert.assertTrue("@PreDestory has been called", dogService.wasReleased());
            Assert.assertTrue("@PreDestory has been called", catService.wasReleased());
        }

        public void testMethod(Service<Dog> dogService, Service<Cat> catService) {
            Assert.assertNotNull(catService);
            Assert.assertNotNull(dogService);

            Assert.assertEquals("Injected object should be of type", CatService.class, catService.getClass());
            Assert.assertEquals("Injected object should be of type", DogService.class, dogService.getClass());
        }

        @SuppressWarnings("unused") // used only via reflection
        public void testEvent(Event<Dog> dogEvent, Event<Cat> catEvent) {
            Assert.assertNotNull("Generic Event should be injected as MethodArgument", dogEvent);
            Assert.assertNotNull("Generic Event should be injected as MethodArgument", catEvent);
        }

        @SuppressWarnings("unused") // used only via reflection
        public void testInstance(Instance<Dog> dogEvent, Instance<Cat> catEvent) {
            Assert.assertNotNull("Generic Instance should be injected as MethodArgument", dogEvent);
            Assert.assertNotNull("Generic Instance should be injected as MethodArgument", catEvent);
        }
    }
}
