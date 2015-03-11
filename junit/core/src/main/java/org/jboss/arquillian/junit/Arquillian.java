package org.jboss.arquillian.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class Arquillian extends Runner implements Filterable, Sortable {

    private Runner delegate;
    
    public Arquillian(Class<?> testClass) throws InitializationError {
        this.delegate = Runners.runners(testClass);
    }

    @Override
    public void sort(Sorter sorter) {
       if(delegate instanceof Sortable) {
          ((Sortable)delegate).sort(sorter);
       }
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
       if(delegate instanceof Filterable) {
          ((Filterable)delegate).filter(filter);
       }
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
