package org.jboss.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class JUnitJupiterTestClassLifecycleManager implements ExtensionContext.Store.CloseableResource {
    private static final String NAMESPACE_KEY = "arquillianNamespace";

    private static final String MANAGER_KEY = "testRunnerManager";

    private static final String INTERCEPTED_TEMPLATE_NAMESPACE_KEY = "interceptedTestTemplates";

    private static final String RESULT_NAMESPACE_KEY = "results";

    private final ExtensionContext.Store templateStore;

    private final ExtensionContext.Store resultStore;

    private TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    JUnitJupiterTestClassLifecycleManager(ExtensionContext context) {
        templateStore = context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, INTERCEPTED_TEMPLATE_NAMESPACE_KEY));
        resultStore = context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, RESULT_NAMESPACE_KEY));
    }

    private static ExtensionContext.Store getRootStore(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY));
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = getRootStore(context);
        JUnitJupiterTestClassLifecycleManager instance = store.get(MANAGER_KEY, JUnitJupiterTestClassLifecycleManager.class);
        if (instance == null) {
            instance = new JUnitJupiterTestClassLifecycleManager(context);
            store.put(MANAGER_KEY, instance);
            instance.initializeAdaptor();
        }
        // no, initialization has been attempted before and failed, refuse
        // to do anything else
        if (instance.hasInitializationException())
            instance.handleSuiteLevelFailure();
        return instance;
    }

    private void initializeAdaptor() throws Exception {
        try {
            // ARQ-1742 If exceptions happen during boot
            adaptor = TestRunnerAdaptorBuilder
                .build();
            // don't set it if beforeSuite fails
            adaptor.beforeSuite();
        } catch (Exception e) {
            // caught exception during BeforeSuite, mark this as failed
            handleBeforeSuiteFailure(e);
        }
    }

    @Override
    public void close() {
        try {
            if (adaptor != null) {
                adaptor.afterSuite();
                adaptor.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not run @AfterSuite", e);
        }
    }

    protected void handleSuiteLevelFailure() {
        throw new RuntimeException(
            "Arquillian initialization has already been attempted, but failed. See previous exceptions for cause",
            caughtInitializationException);
    }

    private void handleBeforeSuiteFailure(Exception e) throws Exception {
        caughtInitializationException = e;
        throw e;
    }

    protected TestRunnerAdaptor getAdaptor() {
        return adaptor; 
    }

    boolean isRegisteredTemplate(final Method method) {
        final boolean isRegistered = templateStore.getOrDefault(method.toGenericString(), boolean.class, false);
        if (!isRegistered) {
            templateStore.put(method.toGenericString(), true);
        }
        return isRegistered;
    }

    void storeResult(String uniqueId, Throwable throwable) {
        resultStore.put(uniqueId, throwable);
        // TODO: find source and unwrap it where it is thrown, not here.
        if (throwable instanceof InvocationTargetException) {
            resultStore.put(uniqueId, throwable.getCause());
        } else {
            resultStore.put(uniqueId, throwable);
        }
    }

    Optional<Throwable> getResult(String uniqueId) {
        return Optional.ofNullable(resultStore.getOrDefault(uniqueId, Throwable.class, null));
    }

    private boolean hasInitializationException() {
        return caughtInitializationException != null;
    }
}
