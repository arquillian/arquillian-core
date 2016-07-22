package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed;

import java.util.Comparator;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.StatisticsBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerListener;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.FileStatisticsStorage;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public class LatestFailedScheduler implements Scheduler{
	
	private LatestFailedSchedulerParamValues storageParams;
	private Statistics statistics;
	private FileStatisticsStorage fileStorage;
	
	public LatestFailedScheduler(Class<?> testClass) throws Exception {
		setupParams(testClass);
		fileStorage = new FileStatisticsStorage(storageParams.getStorageDir());
		statistics = StatisticsBuilder.build(fileStorage);
	}
	
	private void setupParams(Class<?> testClass) throws Exception{
		// Sets up the parameters passed by the annotation
		LatestFailedSchedulerParams paramAnnotation =
				testClass.getAnnotation(LatestFailedSchedulerParams.class);
		
		if(paramAnnotation != null){
			storageParams = new LatestFailedSchedulerParamValues(
							paramAnnotation.storeLongTerm()
							,paramAnnotation.storagePath());
		}else{
			storageParams = new LatestFailedSchedulerParamValues(
					LatestFailedSchedulerParams.STORE_LONG_TERM_DEFAULT_VALUE
					, LatestFailedSchedulerParams.STORAGE_PATH_DEFAULT_VALUE);
		}
	}

	@Override
	public Filter getFilter() {
		// No filter is required with this scheduler
		return Filter.ALL;
	}

	@Override
	public Sorter getSorter() {
		return new Sorter(new LatestFailedComparator());
	}
	
	@Override
	public SchedulerListener getSchedulerListener() {
		return new SchedulerListener() {
			@Override
			public void testRunStarted() {
				if(!storageParams.isStoredLongTerm()){
					// Reset the statistics read from storage if present
					statistics.reset();
				}
			}
			
			@Override
			public void testStarted(String className, String testName) throws Exception {
				// Assumes that all test will pass
				statistics.recordTestStarted(className,testName);
			}
			
			@Override
			public void testFailure(String className, String testName, String reason) throws Exception {
				statistics.recordTestFailure(className,testName);
			}
			
			@Override
			public void testRunFinished() throws Exception {	
				fileStorage.store(statistics);
			}
		};
	}

	private class LatestFailedComparator implements Comparator<Description> {
		
		@Override
		public int compare(Description o1, Description o2) {
			TestStatus o1TestStatus;
			TestStatus o2TestStatus;

			// Gets the failures and passes of the specified test
			o1TestStatus = statistics.getTestStatus(o1.getClassName(),o1.getMethodName());
			o2TestStatus = statistics.getTestStatus(o2.getClassName(),o2.getMethodName());

			if (o1TestStatus == null || o2TestStatus == null) {
				return 0;
			}

			int o1FailToPassFactor = o1TestStatus.getFailures() - o1TestStatus.getPasses();
			int o2FailToPassFactor = o2TestStatus.getFailures() - o2TestStatus.getPasses();

			// Tests with more failures and less passes will be run first
			return o2FailToPassFactor - o1FailToPassFactor;
		}
	}
}
