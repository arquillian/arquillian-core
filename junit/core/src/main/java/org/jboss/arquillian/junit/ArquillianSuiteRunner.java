package org.jboss.arquillian.junit;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.junit.suite.Suite;
import org.jboss.arquillian.junit.suite.resolve.ResolveStrategy;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.event.suite.AfterSubSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSubSuite;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

class ArquillianSuiteRunner extends ParentRunner<Runner> {

    private TestRunnerAdaptor adaptor = null;

    private List<Runner> runners;

    public ArquillianSuiteRunner(Class<?> testClass) throws InitializationError {
        this(testClass, discoverChildren(testClass));
    }
    
    public ArquillianSuiteRunner(Class<?> testClass, List<Runner> runners) throws InitializationError {
        super(testClass);
        this.runners = runners;
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }
    
    @Override
    public void run(RunNotifier notifier) {
        State.runnerStarted();
        adaptor = State.getOrCreateTestAdaptor(notifier, getDescription());
        List<Throwable> exceptions = new ArrayList<Throwable>();
        try {
            adaptor.fireCustomEvent(new BeforeSubSuite(getTestClass().getJavaClass()));
            super.run(notifier);
        } catch(Throwable e) {
            exceptions.add(e);
        } finally {
            try {
                adaptor.fireCustomEvent(new AfterSubSuite(getTestClass().getJavaClass()));
            } catch(Throwable e2) {
                exceptions.add(e2);
            }
            if(exceptions.size() == 1) {
                notifier.fireTestFailure(new Failure(getDescription(), exceptions.get(0)));
            } else if(exceptions.size() > 1){
                notifier.fireTestFailure(new Failure(getDescription(), new MultipleFailureException(exceptions)));
            }
        }
        State.runnerFinished();
        State.shutdownIfLast(notifier, getDescription());
    }
    
    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }

    public static List<Runner> discoverChildren(Class<?> testClass) {
        if(!testClass.isAnnotationPresent(Suite.class)) {
            throw new IllegalArgumentException("Missing " + Suite.class.getName() + " annotation for given TestClass " + testClass.getName());
        }
        Suite suite = testClass.getAnnotation(Suite.class);
        try {
            List<Runner> result = new ArrayList<Runner>();
            
            Class<? extends ResolveStrategy> strategyClass = suite.strategy();
            ResolveStrategy strategy = strategyClass.newInstance();
            
            for(Class<?> resolved : strategy.resolve(suite.value())) {
                Runner runner = Runners.runners(resolved);
                result.add(runner);
            }
            return result;

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
