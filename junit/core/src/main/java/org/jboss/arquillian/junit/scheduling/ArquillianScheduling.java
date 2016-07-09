package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/*
 * Scheduling Arquillian JUnit runner
 */

public class ArquillianScheduling extends Arquillian{
	
	private SchedulerListener schedulerListener;
	
	public ArquillianScheduling(Class<?> testClass) throws Exception {
		super(testClass);
		
		Scheduler scheduler = getScheduler(testClass);
		
		// Sorting tests
		filter(scheduler.getFilter());
		sort(scheduler.getSorter());
		
		// Get the scheduler specific listener
		schedulerListener = scheduler.getSchedulerListener();
		schedulerListener.testRunStarted();
	}

	@Override
	public void run(RunNotifier notifier) {
		notifier.addListener(new RunListener(){
						
			// Stores test method runs
			@Override
			public void testStarted(Description description) throws Exception {
				schedulerListener.testStarted(
						description.getClassName(), description.getMethodName());
			}
			
			// Store information about the failed tests
			@Override
			public void testFailure(Failure failure) throws Exception {
				Description description = failure.getDescription();
				schedulerListener.testFailure(description.getClassName(),
						description.getMethodName(), failure.getMessage());
			}
			
			@Override
			// Serialize recorded statistics information
			public void testRunFinished(Result result) throws Exception {
				try {
					schedulerListener.testRunFinished();
				} catch (Exception e) {
					// TODO 
					e.printStackTrace();
				}
				
			}
		});
		
		super.run(notifier);
	}

	public Scheduler getScheduler(Class<?> testClass) throws Exception{
		
		return SchedulerBuilder.buildScheduler(testClass,testClass
				.getAnnotation(ScheduleWith.class));
	}
}
