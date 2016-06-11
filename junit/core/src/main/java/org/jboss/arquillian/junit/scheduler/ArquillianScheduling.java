package org.jboss.arquillian.junit.scheduler;

import java.util.Comparator;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
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
		
		// Sorting tests
		sort(new Sorter(new Comparator<Description>() {
			@Override
			public int compare(Description o1, Description o2) {
				TestStatus o1TestStatus;
				TestStatus o2TestStatus;
				
				// Gets the failures and passes of the specified test
				o1TestStatus = runtimeStatistics.getTestStatus(o1);
				o2TestStatus = runtimeStatistics.getTestStatus(o2);
				
				if(o1TestStatus == null || o2TestStatus == null){
					return 0;
				}
				
				int o1FailToPassFactor = o1TestStatus.getFailures() - o1TestStatus.getPasses();
				int o2FailToPassFactor = o2TestStatus.getFailures() - o2TestStatus.getPasses();
				
				// Tests with more failures and less passes will be run first
				return o2FailToPassFactor - o1FailToPassFactor;	
			}
		}));
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

	
}
