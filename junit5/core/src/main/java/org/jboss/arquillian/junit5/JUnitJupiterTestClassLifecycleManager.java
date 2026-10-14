package org.jboss.arquillian.junit5;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Owns the Arquillian {@link TestRunnerAdaptor} that the JUnit Jupiter
 * extension drives.
 *
 * <p>How widely one adaptor - and the Arquillian {@code Manager} behind it -
 * is shared is chosen by the JUnit configuration parameter
 * {@value #MANAGER_SCOPE_PROPERTY}.</p>
 *
 * <ul>
 *   <li><b>{@code suite}</b> (default) - one {@code TestRunnerAdaptor} for
 *   the whole JUnit run, kept in the root store. {@code BeforeSuite} /
 *   {@code AfterSuite} fire once per JUnit Platform execution - for an
 *   ordinary Maven or Gradle run, once per JVM. This matches the classic
 *   Arquillian model and is what a shared external container (WildFly,
 *   Payara, GlassFish) that boots once and serves every test class
 *   needs.</li>
 *
 *   <li><b>{@code class}</b> - one {@code TestRunnerAdaptor} per top-level
 *   test class, kept in that class's store, so each one is closed as soon as
 *   its class finishes rather than at the end of the run.
 *   {@code BeforeSuite} / {@code AfterSuite} fire once per class. This is
 *   what makes parallel class execution possible: a shared manager would
 *   leak thread-local context activations between classes running on
 *   different threads. A {@code @Nested} class shares its enclosing class's
 *   manager, however deeply it is nested.</li>
 * </ul>
 *
 * <p><b>Class scope is experimental.</b> It only works with some container
 * types, and both the parameter and its behaviour may change. See the
 * "Parallel Test Class Execution" section of the Arquillian Core guide.</p>
 *
 * <p>Implementing {@link ExtensionContext.Store.CloseableResource} lets JUnit
 * invoke {@link #close()} (and therefore {@code afterSuite}) when it tears
 * down the store holding this manager.</p>
 */

@SuppressWarnings("deprecation") // CloseableResource is deprecated since JUnit 5.13; remove when dropping JUnit 5.13 support
public class JUnitJupiterTestClassLifecycleManager implements AutoCloseable, ExtensionContext.Store.CloseableResource {
    /**
     * JUnit configuration parameter choosing how widely the
     * {@link TestRunnerAdaptor} is shared. Accepts {@code suite} (the
     * default) or {@code class}; any other value fails the run. Resolved via
     * {@link ExtensionContext#getConfigurationParameter(String)}, so it can
     * come from a {@code junit-platform.properties} file on the test
     * classpath, from the build's JUnit configuration parameters, from a JVM
     * system property, or from anything else the JUnit Platform Launcher
     * supports.
     *
     * @see JUnitJupiterTestClassLifecycleManager
     */
    public static final String MANAGER_SCOPE_PROPERTY = "arquillian.junit.jupiter.manager.scope";

    private static final String MANAGER_KEY = "testRunnerManager";

    private TestRunnerAdaptor adaptor;

    private Throwable caughtInitializationException;

    private JUnitJupiterTestClassLifecycleManager() {
    }

    static JUnitJupiterTestClassLifecycleManager getManager(ExtensionContext context) throws Exception {
        ManagerScope scope = ManagerScope.parse(context.getConfigurationParameter(MANAGER_SCOPE_PROPERTY));
        ExtensionContext.Store store = ContextStore.getContextStore(context).getManagerStore(scope);
        // Tracks whether this call is the one that attempted initialization. Caching the failed manager ourselves,
        // rather than letting the exception escape the computation, keeps the behaviour the same on JUnit 5 and 6,
        // which disagree on whether a failed getOrComputeIfAbsent computation is remembered.
        AtomicBoolean attemptedHere = new AtomicBoolean();
        JUnitJupiterTestClassLifecycleManager instance = store.getOrComputeIfAbsent(
            MANAGER_KEY,
            key -> {
                attemptedHere.set(true);
                JUnitJupiterTestClassLifecycleManager mgr = new JUnitJupiterTestClassLifecycleManager();
                try {
                    mgr.initializeAdaptor();
                } catch (VirtualMachineError e) {
                    // A JVM that is out of memory or out of stack is not a failed boot to be remembered and
                    // reported class by class.
                    throw e;
                } catch (Throwable t) {
                    // Errors too, not just exceptions: a NoClassDefFoundError or an ExceptionInInitializerError
                    // out of a container boot is ordinary. Letting one escape the computation would cache nothing,
                    // so every later class would boot Arquillian again instead of being told that it already
                    // failed, and the half-built adaptor would never be closed.
                    mgr.caughtInitializationException = t;
                    mgr.discardFailedAdaptor();
                }
                return mgr;
            },
            JUnitJupiterTestClassLifecycleManager.class);
        // initialization was attempted before and failed; don't retry (the failed instance stays cached)
        if (instance.hasInitializationException()) {
            instance.throwInitializationFailure(attemptedHere.get());
        }
        return instance;
    }

    /**
     * The manager already created for this context, if Arquillian is actually running.
     *
     * <p>Empty when no manager was ever created, and when the one that was failed to initialize. A tear-down
     * callback then has nothing to tear down: the boot failure was reported to the class that triggered it, and
     * asking for the manager a second time would add that class a wrapper naming its own failure as the cause -
     * which JUnit suppresses onto the original, printing a report that points back at itself.</p>
     *
     * @param context the current extension context
     *
     * @return the running manager, or empty if there is none
     */
    static Optional<JUnitJupiterTestClassLifecycleManager> findRunningManager(ExtensionContext context) {
        ManagerScope scope = ManagerScope.parse(context.getConfigurationParameter(MANAGER_SCOPE_PROPERTY));
        ExtensionContext.Store store = ContextStore.getContextStore(context).getManagerStore(scope);
        JUnitJupiterTestClassLifecycleManager instance =
            store.get(MANAGER_KEY, JUnitJupiterTestClassLifecycleManager.class);
        if (instance == null || instance.hasInitializationException()) {
            return Optional.empty();
        }
        return Optional.of(instance);
    }

    private void initializeAdaptor() throws Exception {
        // ARQ-1742 If exceptions happen during boot. Whatever comes out of here is recorded by the caller, which
        // then discards the adaptor if one was already built.
        adaptor = TestRunnerAdaptorBuilder.build();
        adaptor.beforeSuite();
    }

    /**
     * Releases an adaptor whose initialization failed, so that a Manager built before the failure does not stay
     * running for the rest of the JVM.
     *
     * <p>Only {@code shutdown()}, deliberately: {@code BeforeSuite} never completed, so firing {@code AfterSuite}
     * would run undeploy and stop logic against containers that never started, piling misleading failures on top
     * of the real one. Dropping the adaptor also makes {@link #close()} a no-op for a manager that failed.</p>
     */
    private void discardFailedAdaptor() {
        if (adaptor == null) {
            return;
        }
        try {
            adaptor.shutdown();
        } catch (RuntimeException | Error e) {
            caughtInitializationException.addSuppressed(e);
        } finally {
            adaptor = null;
        }
    }

    @Override
    public void close() {
        if (adaptor == null) {
            return;
        }

        RuntimeException failure = null;
        try {
            adaptor.afterSuite();
        } catch (Exception e) {
            failure = new RuntimeException("Could not run @AfterSuite", e);
        }
        try {
            // Always shut down: a failing afterSuite is no reason to leave the manager, and the container behind
            // it, running for the rest of the JVM.
            adaptor.shutdown();
        } catch (RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else {
                // The @AfterSuite failure stays the reported one - it says what went wrong first.
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Reports the failure of an earlier initialization to a caller that asked for this manager: the original
     * failure, with its real type and stack trace, to the caller whose attempt failed, and a fresh wrapper naming
     * that failure as its cause to every caller after it.
     *
     * @param attemptedHere whether this caller is the one whose attempt failed
     */
    private void throwInitializationFailure(boolean attemptedHere) throws Exception {
        if (attemptedHere) {
            // The caller that triggered the failure sees the real exception type and stack trace.
            if (caughtInitializationException instanceof Exception) {
                throw (Exception) caughtInitializationException;
            }
            if (caughtInitializationException instanceof Error) {
                throw (Error) caughtInitializationException;
            }
            throw new RuntimeException(caughtInitializationException);
        }
        // A new wrapper per caller: sharing one instance would give every class the same stack trace and let
        // suppressed exceptions pile up on it as the run goes on.
        throw new RuntimeException(
            "Arquillian initialization has already been attempted, but failed. See previous exceptions for cause",
            caughtInitializationException);
    }

    private boolean hasInitializationException() {
        return caughtInitializationException != null;
    }

    /**
     * Retrieves the TestRunnerAdaptor instance managed by this lifecycle manager.
     *
     * @return the TestRunnerAdaptor instance, or null if it was never built. Callers reach a manager only through
     *         {@link #getManager(ExtensionContext)}, which throws rather than hand out one that failed to
     *         initialize.
     */
    TestRunnerAdaptor getAdaptor() {
        return adaptor;
    }
}
