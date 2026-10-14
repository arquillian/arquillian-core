package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;

import org.jboss.arquillian.test.spi.TestResult;
import org.junit.jupiter.api.extension.ExtensionContext;

class ContextStore {
    private static final String NAMESPACE_KEY = "arquillianNamespace";

    private static final String INTERCEPTED_TEMPLATE_NAMESPACE_KEY = "interceptedTestTemplates";

    private static final String PARAMETER_NAMESPACE_KEY = "methodParameters";

    private static final String LIFECYCLE_METHODS_SKIPPED_KEY = "lifecycleMethodsSkipped";

    private static final String MANAGER_NAMESPACE_KEY = "testRunnerManagers";

    private final ExtensionContext context;

    private ContextStore(ExtensionContext context) {
        this.context = context;
    }

    static ContextStore getContextStore(ExtensionContext context) {
        return new ContextStore(context);
    }

    /**
     * The store that owns the Arquillian manager for the given scope. The store, not a key within it, is what
     * scopes the manager: a manager kept in a class-level store is closed by JUnit as soon as that class finishes,
     * while one kept in the root store lives for the whole run.
     *
     * @param scope how widely the manager is shared
     *
     * @return the store to cache the manager in
     */
    ExtensionContext.Store getManagerStore(ManagerScope scope) {
        ExtensionContext.Namespace namespace =
            ExtensionContext.Namespace.create(NAMESPACE_KEY, MANAGER_NAMESPACE_KEY);
        if (scope == ManagerScope.CLASS) {
            return outermostTestClassContext().getStore(namespace);
        }
        return context.getRoot().getStore(namespace);
    }

    /**
     * The context of the outermost enclosing test class: the highest context above this one that still belongs to
     * a test class. For a class outside any {@code @Nested} hierarchy that is simply its own class context, and
     * for a method, template invocation or nested class it is the top-level class they run under - the class that
     * owns the deployment and fires {@code BeforeClass}.
     *
     * <p>Resolving this explicitly, rather than taking the nearest context and letting JUnit's store hierarchy
     * find the manager in an ancestor store, keeps the owner of the manager obvious: it is created, found and
     * closed in one place, no matter which context asks for it. Walking the context chain, rather than reflecting
     * over enclosing classes, is what makes that hold for a {@code @Nested} class declared in a superclass, whose
     * enclosing class has no context of its own.</p>
     */
    private ExtensionContext outermostTestClassContext() {
        ExtensionContext outermost = null;
        for (ExtensionContext candidate = context; candidate != null;
             candidate = candidate.getParent().orElse(null)) {
            if (candidate.getTestClass().isPresent()) {
                outermost = candidate;
            }
        }
        if (outermost == null) {
            throw new IllegalStateException("No test class context at or above " + context.getUniqueId());
        }
        return outermost;
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

    /**
     * Records whether the @BeforeEach and @AfterEach methods of the current test should be skipped.
     */
    void setLifecycleMethodsSkipped(boolean skipped) {
        context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY))
            .put(LIFECYCLE_METHODS_SKIPPED_KEY, skipped);
    }

    /**
     * Returns whether the @BeforeEach and @AfterEach methods of the current test should be skipped.
     *
     * @return {@code true} if they should be skipped, {@code false} if they should run or nothing was recorded
     */
    boolean isLifecycleMethodsSkipped() {
        return context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY))
            .getOrDefault(LIFECYCLE_METHODS_SKIPPED_KEY, Boolean.class, false);
    }
}
