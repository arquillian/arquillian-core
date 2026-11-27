/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.impl.ClientTestInstanceEnricher;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.impl.TestContextHandler;
import org.jboss.arquillian.test.impl.context.ClassContextImpl;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.impl.context.TestContextImpl;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link EnclosingInstancesEnricher} enriches the instances enclosing a
 * {@link org.junit.jupiter.api.Nested} test instance exactly when, and only when, core enriches the test instance
 * itself.
 */
public class EnclosingInstancesEnricherTestCase {

    private final List<Object> enriched = new ArrayList<>();

    private Manager manager;

    @BeforeEach
    public void createManager() {
        manager = ManagerBuilder.from()
            .context(SuiteContextImpl.class)
            .context(ClassContextImpl.class)
            .context(TestContextImpl.class)
            .extensions(TestContextHandler.class, ClientTestInstanceEnricher.class, EnclosingInstancesEnricher.class,
                MethodParameterObserver.class)
            .create();
        manager.start();
        manager.bind(ApplicationScoped.class, ServiceLoader.class, new EnricherServiceLoader(new RecordingEnricher()));
    }

    @AfterEach
    public void shutdownManager() {
        manager.shutdown();
    }

    @Test
    public void shouldEnrichEnclosingInstancesOutermostFirstAfterTheTestInstance() throws Exception {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        Outer.Inner.Deeper deeper = inner.new Deeper();

        before(deeper, List.of(outer, inner));

        Assertions.assertEquals(List.of(deeper, outer, inner), enriched);
    }

    @Test
    public void shouldNotEnrichEnclosingInstancesWhenTheTestInstanceIsNotEnriched() throws Exception {
        // An in-container test is enriched in the container, not on the client
        bindTestableDeployment();
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();

        before(inner, List.of(outer));

        Assertions.assertEquals(Collections.emptyList(), enriched);
    }

    @Test
    public void shouldEnrichEnclosingInstancesOnTheClientWhenTheNestedClassRunsAsClient() throws Exception {
        bindTestableDeployment();
        Outer outer = new Outer();
        Outer.ClientInner inner = outer.new ClientInner();

        before(inner, List.of(outer));

        Assertions.assertEquals(List.of(inner, outer), enriched);
    }

    @Test
    public void shouldOnlyEnrichTheEnclosingInstancesOfTheEnrichedTest() throws Exception {
        Outer first = new Outer();
        Outer second = new Outer();
        Outer.Inner firstInner = first.new Inner();
        Outer.Inner secondInner = second.new Inner();
        manager.fire(new EnclosingInstancesProducerEvent(firstInner, testMethod(),
            new EnclosingInstances(firstInner, List.of(first))));

        before(secondInner, List.of(second));

        Assertions.assertEquals(List.of(secondInner, second), enriched);
    }

    @Test
    public void shouldNotEnrichEnclosingInstancesAgainWhenMethodParametersAreResolved() throws Exception {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();

        before(inner, List.of(outer));
        // Resolving the method parameters fires another AfterEnrichment for the test instance
        manager.fire(new BeforeTestExecutionEvent(inner, testMethod()));

        Assertions.assertEquals(List.of(inner, outer), enriched);
    }

    @Test
    public void shouldNotEnrichEnclosingInstancesWhenMethodParametersAreResolvedForAnInContainerTest() throws Exception {
        bindTestableDeployment();
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();

        before(inner, List.of(outer));
        manager.fire(new BeforeTestExecutionEvent(inner, testMethod()));

        Assertions.assertEquals(Collections.emptyList(), enriched);
    }

    private void before(Object testInstance, List<Object> enclosingInstances) throws Exception {
        manager.fire(new MethodParameterProducerEvent(testInstance, testMethod(), new MethodParameters()));
        manager.fire(new EnclosingInstancesProducerEvent(testInstance, testMethod(),
            new EnclosingInstances(testInstance, enclosingInstances)));
        manager.fire(new Before(testInstance, testMethod()));
    }

    private void bindTestableDeployment() {
        DeploymentDescription description = mock(DeploymentDescription.class);
        when(description.testable()).thenReturn(true);
        Deployment deployment = mock(Deployment.class);
        when(deployment.getDescription()).thenReturn(description);
        when(deployment.isDeployed()).thenReturn(true);
        manager.bind(ApplicationScoped.class, Deployment.class, deployment);
    }

    private static Method testMethod() throws Exception {
        return Outer.class.getMethod("test");
    }

    public static class Outer {

        public void test() {
        }

        public class Inner {

            public class Deeper {
            }
        }

        @RunAsClient
        public class ClientInner {
        }
    }

    private class RecordingEnricher implements TestEnricher {

        @Override
        public void enrich(Object testCase) {
            enriched.add(testCase);
        }

        @Override
        public Object[] resolve(Method method) {
            return new Object[0];
        }
    }

    private static class EnricherServiceLoader implements ServiceLoader {

        private final TestEnricher enricher;

        EnricherServiceLoader(TestEnricher enricher) {
            this.enricher = enricher;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Collection<T> all(Class<T> serviceClass) {
            return serviceClass == TestEnricher.class ? List.of((T) enricher) : Collections.emptyList();
        }

        @Override
        public <T> T onlyOne(Class<T> serviceClass) {
            return null;
        }

        @Override
        public <T> T onlyOne(Class<T> serviceClass, Class<? extends T> defaultServiceClass) {
            return null;
        }
    }
}
