package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerBuilder;
import org.jboss.arquillian.junit.scheduling.statistics.FileStatisticsStorage;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/*
 * Scheduling Arquillian JUnit runner
 */

public class ArquillianScheduling extends Arquillian{
	
	private Statistics runtimeStatistics;
	private final FileStatisticsStorage fileStorage = new FileStatisticsStorage();

	public ArquillianScheduling(Class<?> testClass) throws Exception {
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
			
			@Override
			// Serialize recorded statistics information
			public void testRunFinished(Result result) throws Exception {
				try{
					fileStorage.store(runtimeStatistics);
				}catch(Exception err){
					// TODO Re throw exception
				}
			}
		});
		
		super.run(notifier);
	}

	public Scheduler getScheduler(Class<?> testClass) throws Exception{
		ScheduleWith annotation = testClass.getAnnotation(ScheduleWith.class);
		
		if(annotation != null){
			return SchedulerBuilder.buildScheduler(annotation.value(),runtimeStatistics);
		}
		
		return SchedulerBuilder.DEFAULT;
	}
}
