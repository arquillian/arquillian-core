package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Owns the {@link TestRunnerAdaptor} for a single JUnit Jupiter test class.
 *
 * <p>The manager is cached in the root store under a {@link ExtensionContext.Namespace}
 * keyed by the test class, so each class gets its own {@code TestRunnerAdaptor}
 * and underlying {@code Manager}. This matters under parallel class execution:
 * a shared {@code Manager} would leak thread-local context activations between
 * classes running on different threads.</p>
 *
 * <p>Implementing {@link ExtensionContext.Store.CloseableResource} lets JUnit
 * call {@link #close()} (and therefore {@code afterSuite}) when it tears down
 * the root context at the end of the run.</p>
 */

public class JUnitJupiterTestClassLifecycleManager implements AutoCloseable,
    ExtensionContext.Store.CloseableResource {
    private static final String MANAGER_KEY = "testRunnerManager";

    private TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    private JUnitJupiterTestClassLifecycleManager() {
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getRoot().getStore(
            ExtensionContext.Namespace.create(
                JUnitJupiterTestClassLifecycleManager.class,
                context.getRequiredTestClass()));
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
