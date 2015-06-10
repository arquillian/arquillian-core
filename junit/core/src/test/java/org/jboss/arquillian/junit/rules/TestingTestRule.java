package org.jboss.arquillian.junit.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * An Implementation of TestRule
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class TestingTestRule implements TestRule
{
    @Override
    public Statement apply(Statement base, Description description)
    {
        return new TestingStatement(base);
    }

}
