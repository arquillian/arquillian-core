package io.github.zforgo.arquillian.junit5.container;

import io.github.zforgo.arquillian.junit5.ArquillianExtension;
import io.github.zforgo.arquillian.junit5.IdentifiedTestException;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JUnitJupiterTestRunner implements TestRunner {

	@Override
	public TestResult execute(Class<?> testClass, String methodName) {
		TestResult testResult;
		ArquillianTestMethodExecutionListener listener = new ArquillianTestMethodExecutionListener();
		try {
			final AtomicInteger matchCounter = new AtomicInteger(0);
			Launcher launcher = LauncherFactory.create();
			launcher.registerTestExecutionListeners(listener);
			LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
					.selectors(DiscoverySelectors.selectClass(testClass.getCanonicalName()))
					.configurationParameter(ArquillianExtension.RUNNING_INSIDE_ARQUILLIAN, "true")
					.filters((PostDiscoveryFilter) object -> {
						Method m = ((MethodBasedTestDescriptor) object).getTestMethod();
						if (m.getName().equals(methodName)) {
							matchCounter.incrementAndGet();
							return FilterResult.included("Matched method name");
						}
						return FilterResult.excluded("Not matched");
					})
					.build();
			TestPlan plan = launcher.discover(request);

			if (matchCounter.get() > 1) {
				throw new JUnitException("Method name must be unique");
			}
			if (plan.containsTests()) {
				launcher.execute(request);
				testResult = listener.getTestResult();
			} else {
				throw new JUnitException("No test method found");
			}
		} catch (Throwable t) {
			testResult = TestResult.failed(t);
		}
		testResult.setEnd(System.currentTimeMillis());
		return testResult;
	}

	private static class ArquillianTestMethodExecutionListener implements TestExecutionListener {
		private final Map<String, Throwable> exceptions = new HashMap<>();

		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			exceptions.put(testIdentifier.getUniqueId(), new TestAbortedException(reason));
		}

		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			TestExecutionResult.Status status = testExecutionResult.getStatus();

			if (!testIdentifier.isTest()) {
				return;
			}
			switch (status) {
				case FAILED:
					exceptions.put(
							testIdentifier.getUniqueId(),
							testExecutionResult.getThrowable().orElseGet(() -> new Exception("Failed"))
					);
					break;
				case ABORTED:
					exceptions.put(
							testIdentifier.getUniqueId(),
							testExecutionResult.getThrowable().orElseGet(() -> new TestAbortedException("Aborted"))
					);
					break;
			}
		}

		private TestResult getTestResult() {
			return exceptions.isEmpty() ? TestResult.passed() : TestResult.failed(new IdentifiedTestException(exceptions));
		}
	}

}
