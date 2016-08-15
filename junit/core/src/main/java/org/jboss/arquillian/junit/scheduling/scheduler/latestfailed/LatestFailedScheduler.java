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

/**
 * Latest failed scheduling strategy
 * 
 * @author Dimcho Karpachev
 *
 */
public class LatestFailedScheduler implements Scheduler{
	/**
	 *  The <code>LatestFailedSchedulerParams</code> annotation's parameters
	 *  
	 *  @see LatestFailedSchedulerParams
	 */
	private LatestFailedSchedulerParamValues storageParams;
	private Statistics statistics;
	private FileStatisticsStorage fileStorage;
	
	/**
	 * Class constructor.
	 * <p>
	 * Creates a <code>LatestFailedScheduler</code> 
	 * using the parameters of <code>LataestFailedSchedulerParams</code> 
	 * and the stored statistics information.
	 * If the <code>LataestFailedSchedulerParams</code> annotation is null,
	 * default values for the <code>LatestFailedSchedulerParams</code> are used.
	 * 
	 * @param testClass the JUnit test class currently executed by a Scheduling runner
	 * @throws Exception
	 * @see LatestFailedSchedulerParamValues, FileStatisticsStorage, StatisticsBuilder
	 */
	public LatestFailedScheduler(Class<?> testClass) throws Exception {
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
		return new Sorter(new LatestFailedComparator());
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
	
	/**
	 * A latest failed strategy test comparator
	 *
	 */
	private class LatestFailedComparator implements Comparator<Description>{
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
