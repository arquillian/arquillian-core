package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.jboss.arquillian.junit5.ContextStore.getContextStore;

public class JUnitJupiterTestClassLifecycleManager implements ExtensionContext.Store.CloseableResource {
    private static final String MANAGER_KEY = "testRunnerManager";

    private volatile TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    private JUnitJupiterTestClassLifecycleManager() throws Exception {
        initializeAdaptor();
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = getContextStore(context).getRootStore();
        JUnitJupiterTestClassLifecycleManager instance = store.get(MANAGER_KEY, JUnitJupiterTestClassLifecycleManager.class);
        if (instance == null) {
            instance = new JUnitJupiterTestClassLifecycleManager();
            store.put(MANAGER_KEY, instance);
        }
        // no, initialization has been attempted before and failed, refuse
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

    private void handleBeforeSuiteFailure(Exception e) throws Exception {
        caughtInitializationException = e;
        throw e;
    }

    TestRunnerAdaptor getAdaptor() {
        return adaptor;
    }
}
