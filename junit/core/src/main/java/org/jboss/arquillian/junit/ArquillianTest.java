package org.jboss.arquillian.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.junit.event.AfterRules;
import org.jboss.arquillian.junit.event.BeforeRules;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * Method rule for Arquillian tests. Allows arquillian to be combined with other runners.
 * Always use both rules together to get the full functionality of Arquillian.
 * <p>
 * <pre>
 * @ClassRule
 * public static ArquillianClassRule arquillianClassRuleRule = new ArquillianClassRule();
 * @Rule
 * public ArquillianRule arquillianRule = new ArquillianRule();
 * </pre>
 *
 * @author <a href="mailto:alexander.schwartz@gmx.net">Alexander Schwartz</a>
 */
public class ArquillianTest implements MethodRule {

    private TestRunnerAdaptor adaptor;

    public ArquillianTest() {
        if (State.hasTestAdaptor()) {
            adaptor = State.getTestAdaptor();
        } else {
            throw new IllegalStateException("arquillian not initialized");
        }
    }

    public Statement apply(final Statement base, final FrameworkMethod method,
                           final Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final List<Throwable> errors = new ArrayList<Throwable>();

                adaptor.fireCustomLifecycle(
                    new BeforeRules(target, new TestClass(method.getDeclaringClass()), base, method.getMethod(),
                        LifecycleMethodExecutor.NO_OP));

                adaptor.before(target, method.getMethod(), LifecycleMethodExecutor.NO_OP);

                try {
                    TestResult result = adaptor.test(new TestMethodExecutor() {
                        public void invoke(Object... parameters)
                            throws Throwable {
                            try {
                                base.evaluate();
                            } catch (Throwable e) {
                                errors.add(e);
                            }
                        }

                        public Method getMethod() {
                            return method.getMethod();
                        }

                        public Object getInstance() {
                            return target;
                        }
                    });
                    if (result.getStatus() != Status.PASSED) {
                        throw new RuntimeException("problem: " + result);
                    }

                } finally {
                    adaptor.after(target, method.getMethod(),
                        LifecycleMethodExecutor.NO_OP);
                    try {
                        adaptor.fireCustomLifecycle(
                            new AfterRules(target, method.getMethod(), LifecycleMethodExecutor.NO_OP));
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                }
                org.junit.runners.model.MultipleFailureException.assertEmpty(errors);
            }
        };
    }

}
