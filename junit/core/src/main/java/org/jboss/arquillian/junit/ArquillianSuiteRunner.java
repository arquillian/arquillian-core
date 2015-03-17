package org.jboss.arquillian.junit;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

class ArquillianSuiteRunner extends ParentRunner<Runner> {

	private TestRunnerAdaptor adaptor = null;
    private TestClass suiteTestClass;
    private List<Runner> runners = null;
    private ChildProvider provider;

    public ArquillianSuiteRunner(Class<?> testClass, ChildProvider provider) throws InitializationError {
        this(new TestClass(null, testClass), provider);
    }

    public ArquillianSuiteRunner(TestClass suiteTestClass, ChildProvider provider) throws InitializationError {
        super(suiteTestClass.getJavaClass());
        this.suiteTestClass = suiteTestClass;
        this.provider = provider;
    }

    private List<Runner> discover() {
        if(runners == null) {
            runners = provider.children(suiteTestClass);
        }
        return runners;
    }

    @Override
    protected List<Runner> getChildren() {
        return discover();
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
            adaptor.beforeSubSuite(suiteTestClass);
            super.run(notifier);
        } catch(Throwable e) {
            exceptions.add(e);
        } finally {
            try {
                adaptor.afterSubSuite(suiteTestClass);
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
}
