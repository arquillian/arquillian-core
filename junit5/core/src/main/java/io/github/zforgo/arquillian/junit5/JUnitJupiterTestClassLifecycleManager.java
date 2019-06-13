package io.github.zforgo.arquillian.junit5;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;

public class JUnitJupiterTestClassLifecycleManager extends ArquillianTestClassLifecycleManager {
	private static final String NAMESPACE_KEY = "arquillianNamespace";
	private static final String ADAPTOR_KEY = "testRunnerAdaptor";
	private static final String INTERCEPTED_TEMPLATE_NAMESPACE_KEY = "interceptedTestTemplates";
	private static final String RESULT_NAMESPACE_KEY = "results";

	private ExtensionContext.Store store;
	private ExtensionContext.Store templateStore;
	private ExtensionContext.Store resultStore;

	JUnitJupiterTestClassLifecycleManager(ExtensionContext context) {
		store = context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY));
		templateStore = context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, INTERCEPTED_TEMPLATE_NAMESPACE_KEY));
		resultStore = context.getStore(ExtensionContext.Namespace.create(NAMESPACE_KEY, RESULT_NAMESPACE_KEY));
	}

	@Override
	protected void setAdaptor(TestRunnerAdaptor testRunnerAdaptor) {
		store.put(ADAPTOR_KEY, testRunnerAdaptor);
	}

	@Override
	protected TestRunnerAdaptor getAdaptor() {
		return store.get(ADAPTOR_KEY, TestRunnerAdaptor.class);
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
	}

	Optional<Throwable> getResult(String uniqueId) {
		return Optional.ofNullable(resultStore.getOrDefault(uniqueId, Throwable.class, null));
	}
}
