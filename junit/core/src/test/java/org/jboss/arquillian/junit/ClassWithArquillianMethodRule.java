package org.jboss.arquillian.junit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.jboss.arquillian.junit.JUnitTestBaseClass.wasCalled;

public class ClassWithArquillianMethodRule {

    @Rule
    public ArquillianTest arquillianTest = new ArquillianTest();

    @BeforeClass
    public static void beforeClass() throws Throwable {
        wasCalled(JUnitTestBaseClass.Cycle.BEFORE_CLASS);
    }

    @AfterClass
    public static void afterClass() throws Throwable {
        wasCalled(JUnitTestBaseClass.Cycle.AFTER_CLASS);
    }

    @Before
    public void before() throws Throwable {
        wasCalled(JUnitTestBaseClass.Cycle.BEFORE);
    }

    @After
    public void after() throws Throwable {
        wasCalled(JUnitTestBaseClass.Cycle.AFTER);
    }

    @Test
    public void shouldBeInvoked() throws Throwable {
        wasCalled(JUnitTestBaseClass.Cycle.TEST);
    }
}
