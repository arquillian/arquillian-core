package org.jboss.arquillian.junit.rules;

import org.junit.Assert;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ResourceAssertion {
    public static void assertNotNullAndNotEqual(ResourceStub ruleResources1, ResourceStub ruleResources2) {
        assertNotNull(ruleResources1);
        assertNotNull(ruleResources2);
        Assert.assertNotEquals(ruleResources1, ruleResources2);
    }
}
