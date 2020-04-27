/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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

import java.lang.reflect.Method;
import java.util.List;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.testenricher.cdi.beans.Cat;
import org.jboss.arquillian.testenricher.cdi.beans.CatService;
import org.jboss.arquillian.testenricher.cdi.beans.Dog;
import org.jboss.arquillian.testenricher.cdi.beans.DogService;
import org.jboss.arquillian.testenricher.cdi.beans.Service;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CDIInjectionEnricherTestCase extends AbstractTestTestBase {
    private SeContainer container;
    private BeanManager manager;
    private CDIInjectionEnricher enricher;

    @org.jboss.arquillian.core.api.annotation.Inject
    private org.jboss.arquillian.core.api.Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(CreationalContextDestroyer.class);
    }

    @Before
    public void setup() throws Exception {
        container = SeContainerInitializer.newInstance()
            .addBeanClasses(Service.class, Cat.class, CatService.class, Dog.class, DogService.class).initialize();
        manager = container.getBeanManager();

        bind(TestScoped.class, BeanManager.class, manager);

        enricher = new CDIInjectionEnricher();
        injector.get().inject(enricher);
    }

    @After
    public void teardown() throws Exception {
        container.close();
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
