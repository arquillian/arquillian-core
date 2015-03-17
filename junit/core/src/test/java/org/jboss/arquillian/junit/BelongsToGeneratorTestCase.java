package org.jboss.arquillian.junit;


import org.jboss.arquillian.junit.suite.BelongsTo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

public class BelongsToGeneratorTestCase {

    @Test
    public void shouldMultiLevelSuite() throws Exception {
        
        Description topLevel = new Runners.BelongsToRunnerGenerator().runners(Test1Class.class).getDescription();
        Assert.assertEquals(SuiteLevel.class, topLevel.getTestClass());
        Assert.assertEquals(1, topLevel.getChildren().size());
        
        Description subLevel = topLevel.getChildren().get(0);
        Assert.assertEquals(SubSuiteLevel.class, subLevel.getTestClass());
        Assert.assertEquals(1, subLevel.getChildren().size());
        
        Description testLevel = subLevel.getChildren().get(0);
        Assert.assertEquals(Test1Class.class, testLevel.getTestClass());
        Assert.assertEquals(1, testLevel.getChildren().size());
    }
    
    public static class SuiteLevel {}
    
    @BelongsTo(SuiteLevel.class)
    public static class SubSuiteLevel {}
    
    @BelongsTo(SubSuiteLevel.class)
    public static class Test1Class {
        
        @Test public void test() {}
    }
}
