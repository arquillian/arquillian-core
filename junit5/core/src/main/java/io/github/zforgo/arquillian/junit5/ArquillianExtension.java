package io.github.zforgo.arquillian.junit5;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

import io.github.zforgo.arquillian.junit5.extension.RunModeEvent;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;

public class ArquillianExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, InvocationInterceptor, TestExecutionExceptionHandler {
	private static final String CHAIN_EXCEPTION_MESSAGE_PREFIX = "Chain of InvocationInterceptors never called invocation";
	public static final String RUNNING_INSIDE_ARQUILLIAN = "insideArquillian";

	private JUnitJupiterTestClassLifecycleManager lifecycleManager;

	private static Predicate<ExtensionContext> isInsideArquillian = (context -> Boolean.parseBoolean(context.getConfigurationParameter(RUNNING_INSIDE_ARQUILLIAN).orElse("false")));

	private JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) {
		if (lifecycleManager == null) {
			lifecycleManager = new JUnitJupiterTestClassLifecycleManager(context);
		}
		return lifecycleManager;
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		getManager(context).beforeTestClassPhase(context.getRequiredTestClass());
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		getManager(context).afterTestClassPhase(context.getRequiredTestClass());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		getManager(context).getAdaptor().before(
				context.getRequiredTestInstance(),
				context.getRequiredTestMethod(),
				LifecycleMethodExecutor.NO_OP);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		getManager(context).getAdaptor().after(
				context.getRequiredTestInstance(),
				context.getRequiredTestMethod(),
				LifecycleMethodExecutor.NO_OP);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		if (isInsideArquillian.test(extensionContext)) {
			// run inside arquillian
			invocation.proceed();
		} else {
			RunModeEvent runModeEvent = new RunModeEvent(extensionContext.getRequiredTestInstance(), extensionContext.getRequiredTestMethod());
			getManager(extensionContext).getAdaptor().fireCustomLifecycle(runModeEvent);
			if (runModeEvent.isRunAsClient()) {
				// Run as client
				interceptInvocation(invocationContext, extensionContext);
			} else {
				// Run as container (but only once)
				if (!getManager(extensionContext).isRegisteredTemplate(invocationContext.getExecutable())) {
					interceptInvocation(invocationContext, extensionContext);
				}
				// Otherwise get result
				getManager(extensionContext)
						.getResult(extensionContext.getUniqueId())
						.ifPresent(ExceptionUtils::throwAsUncheckedException);
			}
		}
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		if (isInsideArquillian.test(extensionContext)) {
			invocation.proceed();
		} else {
			interceptInvocation(invocationContext, extensionContext);
		}
	}

	private void interceptInvocation(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
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
			public void invoke(Object... parameters) throws InvocationTargetException, IllegalAccessException {
				getMethod().invoke(getInstance(), invocationContext.getArguments().toArray());
			}
		});
		populateResults(result, extensionContext);
	}

	private void populateResults(TestResult result, ExtensionContext context) {
		if (Optional.ofNullable(result.getThrowable()).isPresent()) {
			if (result.getThrowable() instanceof IdentifiedTestException) {
				((IdentifiedTestException) result.getThrowable()).getCollectedExceptions()
						.forEach((id, throwable) -> getManager(context).storeResult(id, throwable));
			} else {
				getManager(context).storeResult(context.getUniqueId(), result.getThrowable());
			}
		}
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		if (throwable instanceof JUnitException && throwable.getMessage().startsWith(CHAIN_EXCEPTION_MESSAGE_PREFIX)) {
			return;
		}
		throw throwable;
	}
}
