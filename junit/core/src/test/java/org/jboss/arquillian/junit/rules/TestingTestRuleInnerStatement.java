package org.jboss.arquillian.junit.rules;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * An Implementation of TestRule with Statement declared as inner anonymous class
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class TestingTestRuleInnerStatement implements TestRule
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
                assertNotNull(ruleResources);
                assertNotNull(statementResources);
                Assert.assertNotEquals(statementResources, ruleResources);
                base.evaluate();
            }
        };
    }

}
