/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit.rules;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.impl.InjectorImpl;
import org.jboss.arquillian.core.impl.loadable.ServiceRegistry;
import org.jboss.arquillian.core.impl.loadable.ServiceRegistryLoader;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.junit.RulesEnricher;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.junit.event.RulesEnrichment;
import org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesEnrichmentTestCase extends AbstractTestTestBase {

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(RulesEnricher.class);
    }

    @Before
    public void prepare() {
        Injector injector = InjectorImpl.of(getManager());
        ServiceRegistry registry = new ServiceRegistry(injector, new LinkedHashMap<Class<?>, Set<Class<?>>>());

        registry.addService(ResourceProvider.class, ResourcesProvider.class);
        registry.addService(TestEnricher.class, ArquillianResourceTestEnricher.class);

        ServiceRegistryLoader serviceLoader = new ServiceRegistryLoader(injector, registry);
        bind(SuiteScoped.class, ServiceLoader.class, serviceLoader);
    }

    @Test
    public void shouldEnrichInnerTestRuleInnerStatement() throws Throwable {
        testTestRuleEnrichment(new InnerRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichInnerMethodRuleInnerStatement() throws Throwable {
        testMethodRuleEnrichment(new InnerRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterTestRuleInnerStatement() throws Throwable {
        testTestRuleEnrichment(new OuterRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterMethodRuleInnerStatement() throws Throwable {
        testMethodRuleEnrichment(new OuterRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterTestRuleOuterStatement() throws Throwable {
        testTestRuleEnrichment(new OuterRuleOuterStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterMethodRuleOuterStatement() throws Throwable {
        testMethodRuleEnrichment(new OuterRuleOuterStatementEnrichment());
    }

    private void testTestRuleEnrichment(AbstractRuleStatementEnrichment test) throws Throwable {
        Statement invokeStatement = getInvokingStatement(test);
        TestClass testClass = new TestClass(test.getClass());
        Method testMethod = test.getClass().getMethod("verifyEnrichment");
        Description desc = Description.createTestDescription(test.getClass(), "verifyEnrichment");

        fire(new RulesEnrichment(test, testClass, testMethod, LifecycleMethodExecutor.NO_OP));

        final Statement statement = test.getTestRule().apply(invokeStatement, desc);
        LifecycleMethodExecutor testLifecycleMethodExecutor = getTestLifecycleMethodExecutor(statement);

        fire(new BeforeRules(test, testClass, statement, testMethod, testLifecycleMethodExecutor));
        testLifecycleMethodExecutor.invoke();

        verifyEventFired(2);
    }

    private void testMethodRuleEnrichment(AbstractRuleStatementEnrichment test) throws Throwable {
        Statement invokeStatement = getInvokingStatement(test);
        Method testMethod = test.getClass().getMethod("verifyEnrichment");
        TestClass testClass = new TestClass(test.getClass());
        FrameworkMethod method = testClass.getAnnotatedMethods(Test.class).get(0);

        fire(new RulesEnrichment(test, testClass, testMethod, LifecycleMethodExecutor.NO_OP));

        final Statement statement = test.getMethodRule().apply(invokeStatement, method, test);

        LifecycleMethodExecutor testLifecycleMethodExecutor = getTestLifecycleMethodExecutor(statement);

        fire(new BeforeRules(test, testClass, statement, testMethod, testLifecycleMethodExecutor));
        testLifecycleMethodExecutor.invoke();

        verifyEventFired(2);
    }

    private void verifyEventFired(int numberOfRules) {
        // +2 since the StatementInstance and TestInstance is included
        assertEventFired(BeforeEnrichment.class, numberOfRules + 2);
        assertEventFired(AfterEnrichment.class, numberOfRules + 2);
    }

    private Statement getInvokingStatement(final AbstractRuleStatementEnrichment test) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                test.verifyEnrichment();
            }
        };
    }

    private LifecycleMethodExecutor getTestLifecycleMethodExecutor(final Statement statement) {
        return new LifecycleMethodExecutor() {
            @Override
            public void invoke() throws Throwable {
                statement.evaluate();
            }
        };
    }
}
