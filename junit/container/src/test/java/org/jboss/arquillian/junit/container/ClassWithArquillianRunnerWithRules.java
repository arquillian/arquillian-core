package org.jboss.arquillian.junit.container;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.jboss.arquillian.junit.container.JUnitTestBaseClass.Cycle;
import static org.jboss.arquillian.junit.container.JUnitTestBaseClass.wasCalled;

@RunWith(Arquillian.class)
public class ClassWithArquillianRunnerWithRules {

    @ClassRule
    public static TestRule classRule = new TestRule() {
        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    wasCalled(Cycle.BEFORE_CLASS_RULE);
                    base.evaluate();
                    wasCalled(Cycle.AFTER_CLASS_RULE);
                }
            };
        }
    };

    @Rule
    public MethodRule rule = new MethodRule() {
        @Override
        public Statement apply(final Statement base, FrameworkMethod method, Object target) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    wasCalled(Cycle.BEFORE_RULE);
                    base.evaluate();
                    wasCalled(Cycle.AFTER_RULE);
                }
            };
        }
    };

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
