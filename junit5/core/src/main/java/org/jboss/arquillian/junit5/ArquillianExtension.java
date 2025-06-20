package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.jboss.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ExceptionUtils;

import static org.jboss.arquillian.junit5.ContextStore.getContextStore;
import static org.jboss.arquillian.junit5.JUnitJupiterTestClassLifecycleManager.getManager;

public class ArquillianExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, InvocationInterceptor, ParameterResolver {
    public static final String RUNNING_INSIDE_ARQUILLIAN = "insideArquillian";

    private static final Predicate<ExtensionContext> IS_INSIDE_ARQUILLIAN = (context -> Boolean.parseBoolean(context.getConfigurationParameter(RUNNING_INSIDE_ARQUILLIAN)
        .orElse("false")));

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getManager(context).getAdaptor().beforeClass(
            context.getRequiredTestClass(),
            LifecycleMethodExecutor.NO_OP);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getManager(context).getAdaptor().afterClass(
            context.getRequiredTestClass(),
            LifecycleMethodExecutor.NO_OP);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Get the adapter, test instance and method
        final TestRunnerAdaptor adapter = getManager(context)
            .getAdaptor();
        final Object instance = context.getRequiredTestInstance();
        final Method method = context.getRequiredTestMethod();
        // Create a new parameter holder
        final MethodParameters methodParameters = ContextStore.getContextStore(context).createMethodParameters();
        // Fired to set the MethodParameters on the producer
        adapter.fireCustomLifecycle(new MethodParameterProducerEvent(instance, method, methodParameters));
        adapter.before(
            instance,
            method,
            LifecycleMethodExecutor.NO_OP);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            getManager(context).getAdaptor().after(
                context.getRequiredTestInstance(),
                context.getRequiredTestMethod(),
                LifecycleMethodExecutor.NO_OP);
        } finally {
            ContextStore.getContextStore(context).removeMethodParameters();
        }
    }

    @Override
    public void beforeTestExecution(final ExtensionContext context) throws Exception {
        // Get the adapter, test instance and method
        final TestRunnerAdaptor adapter = getManager(context)
            .getAdaptor();
        final Object instance = context.getRequiredTestInstance();
        final Method method = context.getRequiredTestMethod();
        adapter.fireCustomLifecycle(new BeforeTestExecutionEvent(instance, method));
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            // run inside arquillian
            invocation.proceed();
        } else {
            ContextStore contextStore = getContextStore(extensionContext);
            if (isRunAsClient(extensionContext)) {
                // Run as client
                interceptInvocation(invocation, extensionContext);
            } else {
                // Run as container (but only once)
                if (!contextStore.isRegisteredTemplate(invocationContext.getExecutable())) {
                    interceptInvocation(invocation, extensionContext);
                }
            }
            contextStore.getResult(extensionContext.getUniqueId())
                .ifPresent(ExceptionUtils::throwAsUncheckedException);
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.proceed();
        } else {
            interceptInvocation(invocation, extensionContext);
            getContextStore(extensionContext).getResult(extensionContext.getUniqueId())
                .ifPresent(ExceptionUtils::throwAsUncheckedException);
        }
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext) || isRunAsClient(extensionContext)) {
            // Since the invocation is going to proceed, the invocation must happen within the context of SPI before()
            getManager(extensionContext).getAdaptor().before(
                extensionContext.getRequiredTestInstance(),
                extensionContext.getRequiredTestMethod(),
                invocation::proceed);
        } else {
            invocation.skip();
        }
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext) || isRunAsClient(extensionContext)) {
            getManager(extensionContext).getAdaptor().after(
                extensionContext.getRequiredTestInstance(),
                extensionContext.getRequiredTestMethod(),
                invocation::proceed);
        } else {
            invocation.skip();
        }
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.skip();
        } else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.skip();
        } else {
            invocation.proceed();
        }
    }

    private void interceptInvocation(Invocation<?> invocation, ExtensionContext extensionContext) throws Throwable {
        final AtomicBoolean proceedInvoked = new AtomicBoolean(false);
        TestResult result = getManager(extensionContext).getAdaptor().test(new TestMethodExecutor() {
            @Override
            public String getMethodName() {
                return extensionContext.getRequiredTestMethod().getName();
            }

            @Override
            public Method getMethod() {
                return extensionContext.getRequiredTestMethod();
            }

            @Override
            public Object getInstance() {
                return extensionContext.getRequiredTestInstance();
            }

            @Override
            public void invoke(Object... parameters) throws Throwable {
                proceedInvoked.set(true);
                invocation.proceed();
            }
        });
        populateResults(result, extensionContext);
        // Check if Invocation.proceed() was invoked. If it was, we don't need to execute any further. If it wasn't,
        // we need to check the test status and react to the status.
        if (!proceedInvoked.get()) {
            if (result.getStatus() == TestResult.Status.FAILED) {
                final Throwable throwable = result.getThrowable();
                if (throwable != null) {
                    // The test has failed and the exception was set.
                    throw throwable;
                }
                // This should likely not happen, but we do need to fail
                throw new AssertionError(String.format("The test %s.%s has failed with an unknown error.",
                    extensionContext.getTestClass().map(Class::getName).orElse("<unknown>"),
                    extensionContext.getTestMethod().map(Method::getName).orElse("<unknown>")));
            } else {
                // The test method is being invoked elsewhere. We will skip this invocation and allow it to be
                // proceeded elsewhere.
                invocation.skip();
            }
        }
    }

    private void populateResults(TestResult result, ExtensionContext context) {
        if (Optional.ofNullable(result.getThrowable()).isPresent()) {
            ContextStore contextStore = getContextStore(context);
            if (result.getThrowable() instanceof IdentifiedTestException) {
                ((IdentifiedTestException) result.getThrowable()).getCollectedExceptions()
                    .forEach(contextStore::storeResult);
            } else {
                contextStore.storeResult(context.getUniqueId(), result.getThrowable());
            }
        }
    }

    private boolean isRunAsClient(ExtensionContext extensionContext) throws Exception {
        RunModeEvent runModeEvent = new RunModeEvent(extensionContext.getRequiredTestInstance(), extensionContext.getRequiredTestMethod());
        final JUnitJupiterTestClassLifecycleManager manager = getManager(extensionContext);
        manager.getAdaptor().fireCustomLifecycle(runModeEvent);
        return runModeEvent.isRunAsClient();
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            // Get the parameter holder
            final MethodParameters holder = ContextStore.getContextStore(extensionContext).getMethodParameters();
            if (holder == null) {
                throw createParameterResolutionException(parameterContext, null);
            }
            return holder.get(parameterContext.getIndex()) != null;
        } catch (Exception e) {
            throw createParameterResolutionException(parameterContext, e);
        }
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            // Get the parameter holder
            final MethodParameters holder = ContextStore.getContextStore(extensionContext).getMethodParameters();
            if (holder == null) {
                throw createParameterResolutionException(parameterContext, null);
            }
            return holder.get(parameterContext.getIndex());
        } catch (Exception e) {
            throw createParameterResolutionException(parameterContext, e);
        }
    }

    private static ParameterResolutionException createParameterResolutionException(final ParameterContext parameterContext, final Throwable cause) {
        final String msg = String.format("Failed to resolve parameter %s", parameterContext.getParameter().getName());
        if (cause == null) {
            return new ParameterResolutionException(msg);
        }
        return new ParameterResolutionException(msg, cause);
    }
}
