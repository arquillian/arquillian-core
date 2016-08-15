package org.jboss.arquillian.junit.scheduling.scheduler;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

/**
 * A test scheduler
 * <p>
 * The <code>Scheduler</code> interface provides the ability to sort and filter tests and test suites. 
 * The user can also define and use his own listener.
 * 
 * @see SchedulerListener
 */
public interface Scheduler {
	/**
	 * Filters tests and test suites using a given scheduling strategy.
	 * 
	 * @return the filter with which to filter tests and test suites
	 * @see LatestFailedScheduler, ChnagedFilesSuiteScheduler
	 */
	public Filter getFilter();
	/**
	 * Sorts tests and test suites using a given scheduling strategy
	 * 
	 * @return the sorter with which to sort tests and test suites
	 */
	public Sorter getSorter();
	/**
	 * Gets a user implemented listener.
	 * The user can define custom events for different scheduling strategies.
	 * 
	 * @return  a scheduler specific listener
	 */
	public SchedulerListener getSchedulerListener();
}
