package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;

class ContextStore {
    private static final String NAMESPACE_KEY = "arquillianNamespace";

    private static final String INTERCEPTED_TEMPLATE_NAMESPACE_KEY = "interceptedTestTemplates";

    private static final String PARAMETER_NAMESPACE_KEY = "methodParameters";

    private final ExtensionContext context;

    private ContextStore(ExtensionContext context) {
        this.context = context;
    }

    static ContextStore getContextStore(ExtensionContext context) {
        return new ContextStore(context);
    }

    ExtensionContext.Store getRootStore() {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY));
    }

    private ExtensionContext.Store getTemplateStore() {
        return context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, INTERCEPTED_TEMPLATE_NAMESPACE_KEY));
    }

    boolean isRegisteredTemplate(Method method) {
        final ExtensionContext.Store templateStore = getTemplateStore();

        final boolean isRegistered = templateStore.getOrDefault(method.toGenericString(), boolean.class, false);
        if (!isRegistered) {
            templateStore.put(method.toGenericString(), true);
        }
        return isRegistered;
    }

    /**
     * Creates a new method parameter holder and stores it in the current context.
     *
     * @return the method parameters holder
     */
    MethodParameters createMethodParameters() {
        final MethodParameters methodParameters = new MethodParameters();
        context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, PARAMETER_NAMESPACE_KEY))
            .put(PARAMETER_NAMESPACE_KEY, methodParameters);
        return methodParameters;
    }

    /**
     * Gets the method parameters holder.
     *
     * @return the method parameters holder or {@code null} if one was not created
     */
    MethodParameters getMethodParameters() {
        return context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, PARAMETER_NAMESPACE_KEY))
            .get(PARAMETER_NAMESPACE_KEY, MethodParameters.class);
    }

    /**
     * Removes the method parameters holder.
     */
    void removeMethodParameters() {
        context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, PARAMETER_NAMESPACE_KEY))
            .remove(PARAMETER_NAMESPACE_KEY);
    }
}
