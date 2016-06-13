package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerBuilder;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/*
 * Scheduling Arquillian JUnit runner
 */

public class ArquillianScheduling extends Arquillian{
	
	private Statistics runtimeStatistics;

	public ArquillianScheduling(Class<?> testClass) throws NoTestsRemainException, InitializationError {
		super(testClass);
		
		runtimeStatistics = StatisticsBuilder.build();
		Scheduler scheduler = getScheduler(testClass);
		
		// Sorting tests
		filter(scheduler.getFilter());
		sort(scheduler.getSorter());
	}

	@Override
	public void run(RunNotifier notifier) {
		notifier.addListener(new RunListener(){
			
			// Stores test method runs
			@Override
			public void testStarted(Description description) throws Exception {
				// Assumes that all test will pass
				runtimeStatistics.recordTestStarted(description);
			}
			
			// Store information about the failed tests
			@Override
			public void testFailure(Failure failure) throws Exception {
				runtimeStatistics.recordTestFailure(failure.getDescription());
			}
		});
		
		super.run(notifier);
	}

	public Scheduler getScheduler(Class<?> testClass){
		ScheduleWith annotation = testClass.getAnnotation(ScheduleWith.class);
		
		if(annotation != null){
			try {
				return SchedulerBuilder.buildScheduler(annotation.value(),runtimeStatistics);
			} catch (Exception e) {
				return SchedulerBuilder.DEFAULT;
			}
		}
		
		return SchedulerBuilder.DEFAULT;
	}
}
