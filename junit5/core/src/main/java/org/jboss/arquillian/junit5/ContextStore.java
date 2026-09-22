package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.TestResult;
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

    /**
     * The context of the test template itself, i.e. the parent of the context of a single template invocation. State
     * keyed off it is shared by all invocations of that template and discarded once the template is done.
     */
    private ExtensionContext getTemplateContext() {
        return context.getParent().orElse(context);
    }

    private ExtensionContext.Store getTemplateStore() {
        return getTemplateContext()
            .getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, INTERCEPTED_TEMPLATE_NAMESPACE_KEY));
    }

    boolean isRegisteredTemplate(Method method) {
        return getTemplateStore().get(method.toGenericString()) != null;
    }

    void registerTemplateResultMapper(Method method, TestResult result) {
        getTemplateStore().put(method.toGenericString(),
            new TemplateResultMapper(result != null ? result : TestResult.passed(), getTemplateContext().getUniqueId()));
    }

    TemplateResultMapper getTemplateResultMapper(Method method) {
        return getTemplateStore().get(method.toGenericString(), TemplateResultMapper.class);
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
