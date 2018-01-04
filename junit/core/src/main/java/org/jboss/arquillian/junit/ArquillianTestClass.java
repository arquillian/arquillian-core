package org.jboss.arquillian.junit;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Class rule for Arquillian tests. Allows arquillian to be combined with other runners.
 * Always use both rules together to get the full functionality of Arquillian.
 * <p>
 * <pre>
 * @ClassRule
 * public static ArquillianTestClass arquillianTestClass = new ArquillianTestClass();
 * @Rule
 * public ArquillianTest arquillianTest = new ArquillianTest();
 * </pre>
 *
 * @author <a href="mailto:alexander.schwartz@gmx.net">Alexander Schwartz</a>
 */
public class ArquillianTestClass implements TestRule {

    private TestRunnerAdaptor adaptor;

    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ArquillianTestClassLifecycleManager arquillian =
                    new ArquillianTestClassLifecycleManager() {
                        protected void setAdaptor(TestRunnerAdaptor testRunnerAdaptor) {
                            adaptor = testRunnerAdaptor;
                        }

                        protected TestRunnerAdaptor getAdaptor() {
                            return adaptor;
                        }
                    };
                arquillian.beforeTestClassPhase(description.getTestClass());
                try {
                    base.evaluate();
                } finally {
                    arquillian.afterTestClassPhase(description.getTestClass());
                }
            }
        };
    }

}
