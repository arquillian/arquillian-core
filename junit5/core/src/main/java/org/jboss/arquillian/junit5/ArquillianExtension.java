package org.jboss.arquillian.junit5;

import java.lang.reflect.Method;
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
import org.opentest4j.TestAbortedException;

import static org.jboss.arquillian.junit5.JUnitJupiterTestClassLifecycleManager.getManager;

/**
 * Implments serveral Junit5 extension API interfaces to adapt Juni5 tests into Arquillian.
 */
public class ArquillianExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, InvocationInterceptor, ParameterResolver {
    public static final String RUNNING_INSIDE_ARQUILLIAN = "insideArquillian";

    private static final Predicate<ExtensionContext> IS_INSIDE_ARQUILLIAN = (context -> Boolean.parseBoolean(context.getConfigurationParameter(RUNNING_INSIDE_ARQUILLIAN)
        .orElse("false")));

    /**
     * Called before all tests in the test class are executed.
     *
     * @param context the current extension context
     * @throws Exception if any error occurs
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getManager(context).getAdaptor().beforeClass(
            context.getRequiredTestClass(),
            LifecycleMethodExecutor.NO_OP);
    }

    /**
     * Called after all tests in the test class have been executed.
     *
     * @param context the current extension context
     * @throws Exception if any error occurs
     */
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getManager(context).getAdaptor().afterClass(
            context.getRequiredTestClass(),
            LifecycleMethodExecutor.NO_OP);
    }

    /**
     * Called before each test method is executed.
     *
     * @param context the current extension context
     * @throws Exception if any error occurs
     */
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

    /**
     * Called after each test method has been executed.
     *
     * @param context the current extension context
     * @throws Exception if any error occurs
     */
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

    /**
     * Called before the execution of a test method (but after beforeEach).
     *
     * @param context the current extension context
     * @throws Exception if any error occurs
     */
    @Override
    public void beforeTestExecution(final ExtensionContext context) throws Exception {
        // Get the adapter, test instance and method
        final TestRunnerAdaptor adapter = getManager(context)
            .getAdaptor();
        final Object instance = context.getRequiredTestInstance();
        final Method method = context.getRequiredTestMethod();
        adapter.fireCustomLifecycle(new BeforeTestExecutionEvent(instance, method));
    }

    /**
     * Intercepts the execution of a test template method (e.g., @RepeatedTest, @ParameterizedTest).
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            // run inside arquillian
            invocation.proceed();
        } else {
            ContextStore contextStore = ContextStore.getContextStore(extensionContext);
            TestResult result = null;
            if (isRunAsClient(extensionContext)) {
                // Run as client
                result = interceptInvocation(invocation, extensionContext);
            } else {
                // Run as container (but only once)
                if (!contextStore.isRegisteredTemplate(invocationContext.getExecutable())) {
                    result = interceptInvocation(invocation, extensionContext);
                }
            }
            if (result != null && result.getStatus() != TestResult.Status.PASSED) {
                if(result.getThrowable() != null) {
                    throw result.getThrowable();
                } else {
                    throw new TestAbortedException(result.getDescription());
                }
            }
        }
    }

    /**
     * Intercepts the execution of a test method.
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.proceed();
        } else {
            TestResult result = interceptInvocation(invocation, extensionContext);
            if (result.getStatus() != TestResult.Status.PASSED) {
                if(result.getThrowable() != null) {
                    throw result.getThrowable();
                } else {
                    throw new TestAbortedException(result.getDescription());
                }
            }
        }
    }

    /**
     * Intercepts the execution of a @BeforeEach method.
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
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

    /**
     * Intercepts the execution of an @AfterEach method.
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
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

    /**
     * Intercepts the execution of a @BeforeAll method.
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.skip();
        } else {
            invocation.proceed();
        }
    }

    /**
     * Intercepts the execution of an @AfterAll method.
     *
     * @param invocation the invocation to proceed or skip
     * @param invocationContext the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (IS_INSIDE_ARQUILLIAN.test(extensionContext)) {
            invocation.skip();
        } else {
            invocation.proceed();
        }
    }

    /**
     * Intercepts the invocation of a test method, running it through the Arquillian TestRunnerAdaptor.
     *
     * @param invocation the reflective invocation context
     * @param extensionContext the current extension context
     * @throws Throwable if any error occurs
     */
     private TestResult interceptInvocation(Invocation<?> invocation, ExtensionContext extensionContext) throws Throwable {
            final AtomicBoolean proceedInvoked = new AtomicBoolean(false);
        TestRunnerAdaptor adaptor = getManager(extensionContext).getAdaptor();
        TestResult result = adaptor.test(new TestMethodExecutor() {
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
         // Check if Invocation.proceed() was invoked. If it was, we don't need to execute any further. If it wasn't,
         // we will skip the rest of the interceptors as the interceptors should have been run in the container.
         if (!proceedInvoked.get()) {
             invocation.skip();
         }
         return result;
    }

    /**
     * Determines if the current test should be run as client.
     *
     * @param extensionContext the current extension context
     * @return true if the test should be run as client, false otherwise
     * @throws Exception if any error occurs
     */
    private boolean isRunAsClient(ExtensionContext extensionContext) throws Exception {
        RunModeEvent runModeEvent = new RunModeEvent(extensionContext.getRequiredTestInstance(), extensionContext.getRequiredTestMethod());
        final JUnitJupiterTestClassLifecycleManager manager = getManager(extensionContext);
        manager.getAdaptor().fireCustomLifecycle(runModeEvent);
        return runModeEvent.isRunAsClient();
    }

    /**
     * Determines if the given parameter is supported for injection.
     *
     * @param parameterContext the parameter context
     * @param extensionContext the current extension context
     * @return true if the parameter is supported, false otherwise
     * @throws ParameterResolutionException if parameter resolution fails
     */
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

    /**
     * Resolves the value for the given parameter.
     *
     * @param parameterContext the parameter context
     * @param extensionContext the current extension context
     * @return the resolved parameter value
     * @throws ParameterResolutionException if parameter resolution fails
     */
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

    /**
     * Creates a ParameterResolutionException with a detailed message and optional cause.
     *
     * @param parameterContext the parameter context
     * @param cause the cause of the exception, may be null
     * @return a new ParameterResolutionException
     */
    private static ParameterResolutionException createParameterResolutionException(final ParameterContext parameterContext, final Throwable cause) {
        final String msg = String.format("Failed to resolve parameter %s", parameterContext.getParameter().getName());
        if (cause == null) {
            return new ParameterResolutionException(msg);
        }
        return new ParameterResolutionException(msg, cause);
    }
}
