package org.jboss.arquillian.junit.rules;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.impl.InjectorImpl;
import org.jboss.arquillian.core.impl.loadable.ServiceRegistry;
import org.jboss.arquillian.core.impl.loadable.ServiceRegistryLoader;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.junit.RulesEnricher;
import org.jboss.arquillian.junit.event.BeforeRules;
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
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesEnrichmentTestCase extends AbstractTestTestBase
{

    @Override
    protected void addExtensions(List<Class<?>> extensions)
    {
        extensions.add(RulesEnricher.class);
    }

    @Before
    public void prepare()
    {
        Injector injector = InjectorImpl.of(getManager());
        ServiceRegistry registry = new ServiceRegistry(injector);

        registry.addService(ResourceProvider.class, ResourcesProvider.class);
        registry.addService(TestEnricher.class, ArquillianResourceTestEnricher.class);

        ServiceRegistryLoader serviceLoader = new ServiceRegistryLoader(injector, registry);
        bind(SuiteScoped.class, ServiceLoader.class, serviceLoader);
    }

    @Test
    public void shouldEnrichInnerTestRuleInnerStatement() throws Throwable
    {
        testTestRuleEnrichment(new InnerRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichInnerMethodRuleInnerStatement() throws Throwable
    {
        testMethodRuleEnrichment(new InnerRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterTestRuleInnerStatement() throws Throwable
    {
        testTestRuleEnrichment(new OuterRuleInnerStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterMethodRuleInnerStatement() throws Throwable
    {
        testMethodRuleEnrichment(new OuterRuleInnerStatementEnrichment());
    }
    
    @Test
    public void shouldEnrichOuterTestRuleOuterStatement() throws Throwable
    {
        testTestRuleEnrichment(new OuterRuleOuterStatementEnrichment());
    }

    @Test
    public void shouldEnrichOuterMethodRuleOuterStatement() throws Throwable
    {
        testMethodRuleEnrichment(new OuterRuleOuterStatementEnrichment());
    }

    private void testTestRuleEnrichment(AbstractRuleStatementEnrichment test) throws Throwable
    {
        Statement invokeStatement = getInvokingStatement(test);

        TestClass testClass = new TestClass(test.getClass());

        Description desc = Description.createTestDescription(test.getClass(), "verifyEnrichment");
        final Statement statement = test.getTestRule().apply(invokeStatement, desc);

        LifecycleMethodExecutor testLifecycleMethodExecutor = getTestLifecycleMethodExecutor(statement);

        fire(new BeforeRules(test, testClass, statement, test.getClass().getMethod("verifyEnrichment"), testLifecycleMethodExecutor));
        testLifecycleMethodExecutor.invoke();

        verifyEventFired();
    }

    private void testMethodRuleEnrichment(AbstractRuleStatementEnrichment test) throws Throwable
    {
        Statement invokeStatement = getInvokingStatement(test);

        Method testMethod = test.getClass().getMethod("verifyEnrichment");

        TestClass testClass = new TestClass(test.getClass());
        FrameworkMethod method = testClass.getAnnotatedMethods(Test.class).get(0);
        final Statement statement = test.getMethodRule().apply(invokeStatement, method, test);

        LifecycleMethodExecutor testLifecycleMethodExecutor = getTestLifecycleMethodExecutor(statement);

        fire(new BeforeRules(test, testClass, statement, testMethod, testLifecycleMethodExecutor));
        testLifecycleMethodExecutor.invoke();

        verifyEventFired();
    }

    private void verifyEventFired()
    {
        assertEventFired(BeforeEnrichment.class, 1);
        assertEventFired(AfterEnrichment.class, 1);
    }

    private Statement getInvokingStatement(final AbstractRuleStatementEnrichment test)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                test.verifyEnrichment();
            }
        };
    }

    private LifecycleMethodExecutor getTestLifecycleMethodExecutor(final Statement statement)
    {
        return new LifecycleMethodExecutor()
        {
            @Override
            public void invoke() throws Throwable
            {
                statement.evaluate();
            }
        };
    }
}
