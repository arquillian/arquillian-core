package org.jboss.arquillian.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

abstract class AdaptorManagerWithNotifier extends AdaptorManager {

    private final RunNotifier notifier;

    AdaptorManagerWithNotifier(RunNotifier notifier){
        this.notifier = notifier;
    }

    void initializeAdaptor() {
        try {
            super.initializeAdaptor();
        } catch (Exception e) {
            // this never happens
        }
    }

    void prepareDestroyAdaptorProcess(){
        notifier.addListener(new RunListener() {
            @Override
            public void testRunFinished(Result result) throws Exception {
                shutdown(getAdaptor());
            }
        });
    }

    protected void handleSuiteLevelFailure(Throwable initializationException) {
        notifier.fireTestFailure(
            new Failure(getFailureDescription(),
                new RuntimeException(
                    "Arquillian initialization has already been attempted, but failed. See previous exceptions for cause",
                    initializationException)));
    }

    protected void handleBeforeSuiteFailure(Exception e) throws Exception {
        notifier.fireTestFailure(new Failure(getFailureDescription(), e));
    }

    protected abstract Description getFailureDescription();
}
