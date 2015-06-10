package org.jboss.arquillian.junit.rules;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Uses Rule and Statement as inner anonymous classes.
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 * 
 */
public class InnerRuleInnerStatementEnrichment extends AbstractRuleStatementEnrichment
{
    @ArquillianResource
    private ResourcesImpl testResources;

    @Rule
    public TestRule testRule = new TestRule()
    {
        @ArquillianResource
        private ResourcesImpl ruleResources;

        @Override
        public Statement apply(final Statement base, Description description)
        {
            return new Statement()
            {
                @ArquillianResource
                private ResourcesImpl statementResources;

                @Override
                public void evaluate() throws Throwable
                {
                    assertNotNull(testResources);
                    assertNotNull(ruleResources);
                    assertNotNull(statementResources);

                    Assert.assertNotEquals(testResources, ruleResources);
                    Assert.assertNotEquals(testResources, statementResources);
                    Assert.assertNotEquals(statementResources, ruleResources);

                    base.evaluate();
                }
            };
        }
    };

    @Rule
    public MethodRule methodRule = new MethodRule()
    {
        @ArquillianResource
        private ResourcesImpl ruleResources;

        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target)
        {
            return new Statement()
            {
                @ArquillianResource
                private ResourcesImpl statementResources;

                @Override
                public void evaluate() throws Throwable
                {
                    assertNotNull(testResources);
                    assertNotNull(ruleResources);
                    assertNotNull(statementResources);

                    Assert.assertNotEquals(testResources, ruleResources);
                    Assert.assertNotEquals(testResources, statementResources);
                    Assert.assertNotEquals(statementResources, ruleResources);
                    
                    base.evaluate();
                }
            };
        }
    };

    public TestRule getTestRule()
    {
        return testRule;
    }

    public MethodRule getMethodRule()
    {
        return methodRule;
    }

    @Test
    public void verifyEnrichment()
    {
        assertNotNull(testResources);
    }
}
