package org.jboss.arquillian.junit.rules;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * An implementation of MethodRule
 * 
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public class TestingMethodRule implements MethodRule
{
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target)
    {
        return new TestingStatement(base);
    }

}
