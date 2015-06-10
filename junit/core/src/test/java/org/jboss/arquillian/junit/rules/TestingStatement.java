package org.jboss.arquillian.junit.rules;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runners.model.Statement;

/**
 * An implementation of Statement
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class TestingStatement extends Statement
{
    @ArquillianResource
    private ResourcesImpl resources;

    private Statement base;

    public TestingStatement(Statement base)
    {
        this.base = base;
    }

    @Override
    public void evaluate() throws Throwable
    {
        assertNotNull(resources);
        base.evaluate();
    }
}
