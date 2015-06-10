package org.jboss.arquillian.junit.rules;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 *
 */
public abstract class AbstractRuleStatementEnrichment
{
    public abstract void verifyEnrichment();

    public abstract TestRule getTestRule();

    public abstract MethodRule getMethodRule();
}
