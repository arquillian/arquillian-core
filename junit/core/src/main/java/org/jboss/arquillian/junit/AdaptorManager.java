package org.jboss.arquillian.junit;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;

abstract class AdaptorManager {

    void initializeAdaptor() throws Exception {
        // first time we're being initialized
        if (!State.hasTestAdaptor()) {
            // no, initialization has been attempted before and failed, refuse
            // to do anything else
            if (State.hasInitializationException()) {
                // failed on suite level, ignore children
                // notifier.fireTestIgnored(getFailureDescription());
                handleSuiteLevelFailure(State.getInitializationException());
            } else {
                try {
                    // ARQ-1742 If exceptions happen during boot
                    TestRunnerAdaptor adaptor = TestRunnerAdaptorBuilder
                        .build();
                    // don't set it if beforeSuite fails
                    adaptor.beforeSuite();
                    State.testAdaptor(adaptor);
                } catch (Exception e) {
                    // caught exception during BeforeSuite, mark this as failed
                    State.caughtInitializationException(e);
                    handleBeforeSuiteFailure(e);
                }
            }
        }

        if (State.hasTestAdaptor()) {
            setAdaptor(State.getTestAdaptor());
        }
    }

    protected void shutdown(TestRunnerAdaptor adaptor) {
        State.runnerFinished();
        try {
            if (State.isLastRunner()) {
                try {
                    if (adaptor != null) {
                        adaptor.afterSuite();
                        adaptor.shutdown();
                    }
                } finally {
                    State.clean();
                }
            }
            setAdaptor(null);
        } catch (Exception e) {
            throw new RuntimeException("Could not run @AfterSuite", e);
        }
    }

    protected abstract void handleSuiteLevelFailure(Throwable initializationException);

    protected abstract void handleBeforeSuiteFailure(Exception e) throws Exception;

    protected abstract void setAdaptor(TestRunnerAdaptor testRunnerAdaptor);

    protected abstract TestRunnerAdaptor getAdaptor();
}
