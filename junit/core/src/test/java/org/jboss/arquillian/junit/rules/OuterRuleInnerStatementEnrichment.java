package org.jboss.arquillian.junit.rules;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;

/**
 * Uses Rule as normal outer java class and Statement as inner anonymous class defined within the Rule
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class OuterRuleInnerStatementEnrichment extends AbstractRuleStatementEnrichment
{
    @ArquillianResource
    private ResourcesImpl resources;

    @Rule
    public TestingTestRuleInnerStatement testRuleInnerStatement = new TestingTestRuleInnerStatement();

    @Rule
    public TestingMethodRuleInnerStatement methodRuleInnerStatement = new TestingMethodRuleInnerStatement();

    public TestRule getTestRule()
    {
        return testRuleInnerStatement;
    }

    public MethodRule getMethodRule()
    {
        return methodRuleInnerStatement;
    }

    @Test
    public void verifyEnrichment()
    {
        assertNotNull(resources);
    }
}
