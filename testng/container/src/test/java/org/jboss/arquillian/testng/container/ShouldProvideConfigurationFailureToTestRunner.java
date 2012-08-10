package org.jboss.arquillian.testng.container;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ShouldProvideConfigurationFailureToTestRunner
{
    @BeforeClass
    public void failingConfiguration() throws ClassNotFoundException
    {
        throw new ClassNotFoundException();
    }

    @Test
    public void successfulTest()
    {
        Assert.assertTrue(true);
    }
}
