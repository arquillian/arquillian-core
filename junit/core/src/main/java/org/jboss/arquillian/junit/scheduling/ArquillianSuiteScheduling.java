package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;

/**
 * Arquillian Suite Scheduling JUnit runner
 * 
 * @author Dimcho Karpachev
 * @version 1.0
 * 
 */
public class ArquillianSuiteScheduling extends Suite {

	private SchedulerListener schedulerListener;

	public ArquillianSuiteScheduling(Class<?> klass, RunnerBuilder builder) throws Exception {
		super(klass, builder);

		Scheduler suiteScheduler = getSuiteScheduler(klass);

		filter(suiteScheduler.getFilter());
		sort(suiteScheduler.getSorter());

		// Get the scheduler specific listener
		schedulerListener = suiteScheduler.getSchedulerListener();
		schedulerListener.testRunStarted();
	}

	@Override
	public void run(RunNotifier notifier) {
		notifier.addListener(new RunListener() {

			// Stores test method runs
			@Override
			public void testStarted(Description description) throws Exception {
				schedulerListener.testStarted(description.getClassName(), description.getMethodName());
			}

			// Store information about the failed tests
			@Override
			public void testFailure(Failure failure) throws Exception {
				Description description = failure.getDescription();
				schedulerListener.testFailure(description.getClassName(), description.getMethodName(),
						failure.getMessage());
			}

			@Override
			// Serialize recorded statistics information
			public void testRunFinished(Result result) throws Exception {
				try {
					schedulerListener.testRunFinished();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		super.run(notifier);
	}
	
	/**
	 * Returns a Scheduler object that can be used to sort and filter JUnit tests methods.
	 * On the various Schedulers the sort and filter methods can have different implementations.
	 * The class argument must only specify a JUnit test class.
	 * 
	 * @param testClass the JUnit test class currently executed by <code>ArquillianScheduling</code> 
	 * @return a scheduler based on the annotations on <code>testClass</code> 
	 * @throws Exception
	 * @see Scheduler, SchedulerBuilder, RunWith
	 */
	private Scheduler getSuiteScheduler(Class<?> testClass) throws Exception {
		return SchedulerBuilder.buildScheduler(testClass,testClass
				.getAnnotation(ScheduleWith.class));
	}
}
