package org.jboss.arquillian.junit.suite;

import org.jboss.arquillian.junit.Runners;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class Arquillian extends Runner {

    private Runner delegate;
    
    public Arquillian(Class<?> testClass) throws InitializationError {
        this.delegate = Runners.runners(testClass);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }
}
