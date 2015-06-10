package org.jboss.arquillian.junit.rules;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;

/**
 * Uses Rule and Statement as normal outer java classes.
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class OuterRuleOuterStatementEnrichment extends AbstractRuleStatementEnrichment
{
    @ArquillianResource
    private ResourcesImpl resources;

    @Rule
    public TestingTestRule testRule = new TestingTestRule();

    @Rule
    public TestingMethodRule methodRule = new TestingMethodRule();

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
        assertNotNull(resources);
    }
}
