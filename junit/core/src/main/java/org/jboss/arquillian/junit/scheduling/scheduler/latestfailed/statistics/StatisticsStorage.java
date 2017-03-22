package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import org.jboss.arquillian.junit.scheduling.Statistics;

public interface StatisticsStorage {
	/**
	 * Provides the means to store test statistic information
	 * to the storage type of choosing.
	 * 
	 * @param stats the statistics object containing statistics for various tests
	 * @throws Exception
	 */
	void store(Statistics stats) throws Exception;
	
	/**
	 * Provides the means to retrieve test statistic information from various storage types.
	 * 
	 * @return a statistic object containing the stored statistic information
	 * @throws Exception
	 */
	Statistics retrieve() throws Exception;
}
