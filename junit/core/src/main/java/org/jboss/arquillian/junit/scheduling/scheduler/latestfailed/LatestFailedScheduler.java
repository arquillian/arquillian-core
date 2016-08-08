package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed;

import java.util.Comparator;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.StatisticsBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerListener;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.FileStatisticsStorage;
import org.jboss.arquillian.junit.scheduling.sort.AtomicTestSortingUtil;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public class LatestFailedScheduler implements Scheduler{
	private LatestFailedSchedulerParamValues storageParams;
	private Statistics statistics;
	private FileStatisticsStorage fileStorage;
	
	public LatestFailedScheduler(Class<?> testClass) throws Exception {
		// Sets up the parameters passed by the annotation
		// TODO annotation
		LatestFailedSchedulerParams paramAnnotation =
				testClass.getAnnotation(LatestFailedSchedulerParams.class);
		
		// TODO change  ParamValue class
		if(paramAnnotation != null){
			storageParams = new LatestFailedSchedulerParamValues(
							paramAnnotation.storeLongTerm()
							,paramAnnotation.storagePath());
		}else{
			storageParams = new LatestFailedSchedulerParamValues(
					LatestFailedSchedulerParams.STORE_LONG_TERM_DEFAULT_VALUE
					, LatestFailedSchedulerParams.STORAGE_PATH_DEFAULT_VALUE);
		}
		
		fileStorage = new FileStatisticsStorage(storageParams.getStorageDir());
		// Read the stored statistics if any
		statistics = StatisticsBuilder.build(fileStorage);	
		
	}

	@Override
	public Filter getFilter() {
		return Filter.ALL;
	}

	@Override
	public Sorter getSorter() {
		return new Sorter(new LatestFailedSuiteComparator());
	}

	// No scheduler listener is required
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
	
	private class LatestFailedSuiteComparator implements Comparator<Description>{
		private final AtomicTestSortingUtil sorter = new AtomicTestSortingUtil();
		
		@Override
		public int compare(Description o1, Description o2) {
			// Sorts atomic tests if any
			if(o1.isTest() && o2.isTest()){
				return sorter.sortByLatestFailed(statistics, o1, o2);			
			}
			
			// Sorts test suites if any
			return sorter.sortClassesByLatestFailed(statistics, o1, o2);
		}
	}
}
