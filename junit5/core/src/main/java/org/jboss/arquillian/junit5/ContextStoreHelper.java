package org.jboss.arquillian.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

class ContextStoreHelper {
    private static final String NAMESPACE_KEY = "arquillianNamespace";

    private static final String INTERCEPTED_TEMPLATE_NAMESPACE_KEY = "interceptedTestTemplates";

    private static final String RESULT_NAMESPACE_KEY = "results";

    static ExtensionContext.Store getRootStore(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY));
    }

    private static ExtensionContext.Store getTemplateStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, INTERCEPTED_TEMPLATE_NAMESPACE_KEY));
    }

    private static ExtensionContext.Store getResultStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, RESULT_NAMESPACE_KEY));
    }

    static boolean isRegisteredTemplate(ExtensionContext context, Method method) {
        final ExtensionContext.Store templateStore = getTemplateStore(context);

        final boolean isRegistered = templateStore.getOrDefault(method.toGenericString(), boolean.class, false);
        if (!isRegistered) {
            templateStore.put(method.toGenericString(), true);
        }
        return isRegistered;
    }

    static void storeResult(ExtensionContext context, String uniqueId, Throwable throwable) {
        final ExtensionContext.Store resultStore = getResultStore(context);
        resultStore.put(uniqueId, throwable);
        // TODO: find source and unwrap it where it is thrown, not here.
        if (throwable instanceof InvocationTargetException) {
            resultStore.put(uniqueId, throwable.getCause());
        } else {
            resultStore.put(uniqueId, throwable);
        }
    }

    static Optional<Throwable> getResult(ExtensionContext context, String uniqueId) {
        final ExtensionContext.Store resultStore = getResultStore(context);
        return Optional.ofNullable(resultStore.getOrDefault(uniqueId, Throwable.class, null));
    }
}
