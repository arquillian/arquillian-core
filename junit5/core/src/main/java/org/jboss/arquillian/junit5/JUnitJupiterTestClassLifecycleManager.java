package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Owns the Arquillian {@link TestRunnerAdaptor} that the JUnit Jupiter
 * extension drives.
 *
 * <p>Two caching modes, selected by the system property
 * {@value #PER_CLASS_MANAGER_PROPERTY} (default {@code false}):</p>
 *
 * <ul>
 *   <li><b>Singleton (default)</b> — one {@code TestRunnerAdaptor} for the
 *   whole JUnit run, cached on the root store. {@code BeforeSuite} /
 *   {@code AfterSuite} fire once per JVM. This matches the classic
 *   Arquillian model and is required for shared managed external containers
 *   (WildFly, Payara, GlassFish) that boot once and serve every test class.</li>
 *
 *   <li><b>Per-class</b> (property set to {@code true}) — a separate
 *   {@code TestRunnerAdaptor} per test class, cached on the root store under
 *   a namespace keyed by the class. {@code BeforeSuite} / {@code AfterSuite}
 *   fire once per class. Required for parallel class execution against
 *   isolating embedded containers that allocate their own resources per class
 *   (e.g. embedded Jetty on port {@code 0}). A shared manager would leak
 *   thread-local context activations between classes running on different
 *   threads.</li>
 * </ul>
 *
 * <p>Implementing {@link ExtensionContext.Store.CloseableResource} lets JUnit
 * invoke {@link #close()} (and therefore {@code afterSuite}) when it tears
 * down the root context at the end of the run.</p>
 */

public class JUnitJupiterTestClassLifecycleManager implements AutoCloseable,
    ExtensionContext.Store.CloseableResource {
    /**
     * System property that enables per-test-class caching of the
     * {@link TestRunnerAdaptor}. Defaults to {@code false}.
     *
     * @see JUnitJupiterTestClassLifecycleManager
     */
    public static final String PER_CLASS_MANAGER_PROPERTY = "arquillian.junit5.manager.perClass";

    private static final boolean PER_CLASS_MANAGER = Boolean.getBoolean(PER_CLASS_MANAGER_PROPERTY);

    private static final String MANAGER_KEY = "testRunnerManager";

    private TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    private JUnitJupiterTestClassLifecycleManager() {
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ExtensionContext.Namespace namespace = PER_CLASS_MANAGER
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
        // initialization has been attempted before and failed, refuse
        // to do anything else
        if (instance.hasInitializationException()) {
            instance.handleSuiteLevelFailure();
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

    private void handleSuiteLevelFailure() {
        throw new RuntimeException(
            "Arquillian initialization has already been attempted, but failed. See previous exceptions for cause",
            caughtInitializationException);
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
