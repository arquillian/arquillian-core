package org.jboss.arquillian.junit;

import org.jboss.arquillian.junit.JUnitTestBaseClass.Cycle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.jboss.arquillian.junit.JUnitTestBaseClass.wasCalled;

public class ClassWithArquillianClassAndMethodRuleWithExceptionInBeforeRule {
    @ClassRule
    public static ArquillianTestClass arquillianTestClass = new ArquillianTestClass();

    @Rule
    public MethodRuleChain rule = MethodRuleChain.outer(new ArquillianTest()).around(new MethodRule() {
        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    throw new RuntimeException("BeforeRuleException");
                }
            };
        }
    });

    @BeforeClass
    public static void beforeClass() throws Throwable {
        wasCalled(Cycle.BEFORE_CLASS);
    }

    @AfterClass
    public static void afterClass() throws Throwable {
        wasCalled(Cycle.AFTER_CLASS);
    }

    @Before
    public void before() throws Throwable {
        wasCalled(Cycle.BEFORE);
    }

    @After
    public void after() throws Throwable {
        wasCalled(Cycle.AFTER);
    }

    @Test
    public void shouldBeInvoked() throws Throwable {
        wasCalled(Cycle.TEST);
    }
}
