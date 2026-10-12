package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Owns the Arquillian {@link TestRunnerAdaptor} that the JUnit Jupiter
 * extension drives.
 *
 * <p>How widely one adaptor - and the Arquillian {@code Manager} behind it -
 * is shared is chosen by the JUnit configuration parameter
 * {@value #MANAGER_SCOPE_PROPERTY}.</p>
 *
 * <ul>
 *   <li><b>{@code suite}</b> (default) - one {@code TestRunnerAdaptor} for
 *   the whole JUnit run. {@code BeforeSuite} / {@code AfterSuite} fire once
 *   per JVM. This matches the classic Arquillian model and is what a shared
 *   external container (WildFly, Payara, GlassFish) that boots once and
 *   serves every test class needs.</li>
 *
 *   <li><b>{@code class}</b> - one {@code TestRunnerAdaptor} per test class.
 *   {@code BeforeSuite} / {@code AfterSuite} fire once per class. This is
 *   what makes parallel class execution possible: a shared manager would
 *   leak thread-local context activations between classes running on
 *   different threads.</li>
 * </ul>
 *
 * <p><b>Class scope is experimental.</b> It only works with some container
 * types, and both the parameter and its behaviour may change. See the
 * "Parallel Test Class Execution" section of the Arquillian Core guide.</p>
 *
 * <p>Implementing {@link ExtensionContext.Store.CloseableResource} lets JUnit
 * invoke {@link #close()} (and therefore {@code afterSuite}) when it tears
 * down the store holding this manager.</p>
 */

@SuppressWarnings("deprecation") // CloseableResource is deprecated since JUnit 5.13; remove when dropping JUnit 5.13 support
public class JUnitJupiterTestClassLifecycleManager implements AutoCloseable, ExtensionContext.Store.CloseableResource {
    /**
     * JUnit configuration parameter choosing how widely the
     * {@link TestRunnerAdaptor} is shared. Accepts {@code suite} (the
     * default) or {@code class}; any other value fails the run. Resolved via
     * {@link ExtensionContext#getConfigurationParameter(String)}, so it can
     * come from a {@code junit-platform.properties} file on the test
     * classpath, from the build's JUnit configuration parameters, from a JVM
     * system property, or from anything else the JUnit Platform Launcher
     * supports.
     *
     * @see JUnitJupiterTestClassLifecycleManager
     */
    public static final String MANAGER_SCOPE_PROPERTY = "arquillian.junit.jupiter.manager.scope";

    private static final String MANAGER_KEY = "testRunnerManager";

    private TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    private JUnitJupiterTestClassLifecycleManager() {
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ManagerScope scope = ManagerScope.parse(context.getConfigurationParameter(MANAGER_SCOPE_PROPERTY));
        ExtensionContext.Namespace namespace = scope == ManagerScope.CLASS
            ? ExtensionContext.Namespace.create(
                JUnitJupiterTestClassLifecycleManager.class,
                context.getRequiredTestClass())
            : ExtensionContext.Namespace.create(JUnitJupiterTestClassLifecycleManager.class);
        ExtensionContext.Store store = context.getRoot().getStore(namespace);
        JUnitJupiterTestClassLifecycleManager instance = store.getOrComputeIfAbsent(
            MANAGER_KEY,
            key -> {
                JUnitJupiterTestClassLifecycleManager mgr = new JUnitJupiterTestClassLifecycleManager();
                try {
                    mgr.initializeAdaptor();
                } catch (Exception e) {
                    mgr.caughtInitializationException = e;
                }
                return mgr;
            },
            JUnitJupiterTestClassLifecycleManager.class);
        // initialization was attempted before and failed; surface the original
        // failure and don't retry (the failed instance stays cached)
        if (instance.hasInitializationException()) {
            instance.rethrowInitializationException();
        }
        return instance;
    }

    private void initializeAdaptor() throws Exception {
        try {
            // ARQ-1742 If exceptions happen during boot
            adaptor = TestRunnerAdaptorBuilder.build();
            // don't set it if beforeSuite fails
            adaptor.beforeSuite();
        } catch (Exception e) {
            // caught exception during BeforeSuite, mark this as failed
            handleBeforeSuiteFailure(e);
        }
    }

    @Override
    public void close() {
        if (adaptor == null) {
            return;
        }

        try {
            adaptor.afterSuite();
            adaptor.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Could not run @AfterSuite", e);
        }
    }

    private void rethrowInitializationException() throws Exception {
        // Rethrow the original failure so callers see the real exception type,
        // as they did before per-class managers were introduced.
        if (caughtInitializationException instanceof Exception) {
            throw (Exception) caughtInitializationException;
        }
        if (caughtInitializationException instanceof Error) {
            throw (Error) caughtInitializationException;
        }
        throw new RuntimeException(caughtInitializationException);
    }

    private boolean hasInitializationException() {
        return caughtInitializationException != null;
    }
    /**
     * Handles initialization failures that occur during the beforeSuite phase.
     * This method captures the exception and stores it for later retrieval,
     * preventing repeated initialization attempts.
     *
     * @param e the exception that occurred during initialization
     */
    private void handleBeforeSuiteFailure(Exception e) throws Exception {
        caughtInitializationException = e;
        throw e;
    }

    /**
     * Retrieves the TestRunnerAdaptor instance managed by this lifecycle manager.
     *
     * @return the TestRunnerAdaptor instance, or null if initialization failed
     */
    TestRunnerAdaptor getAdaptor() {
        return adaptor;
    }
}
