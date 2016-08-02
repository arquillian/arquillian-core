package org.jboss.arquillian.junit.rules;

import org.junit.Assert;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class AssertUtil
{
    public static void assertNotNullAndNotEqual(ResourcesImpl ruleResources1, ResourcesImpl ruleResources2){
        assertNotNull(ruleResources1);
        assertNotNull(ruleResources2);
        Assert.assertNotEquals(ruleResources1, ruleResources2);
    }
}
