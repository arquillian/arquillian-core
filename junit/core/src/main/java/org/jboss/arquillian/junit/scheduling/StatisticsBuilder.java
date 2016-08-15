package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.FileStatisticsStorage;

/**
 * A builder for <code>Statistics</code> objects
 *
 */
public class StatisticsBuilder {
	private static Statistics statistics;
	
	// Invoke for test purposes only
	@SuppressWarnings("unused")
	private static void set(Statistics stats){
		statistics = stats;
	}
	
	/**
	 * Builds a <code>Statistics</code> object.
	 * <p>
	 * If the <code>statistics</code> object is already set
	 * it is returned and used for testing purposes only.
	 * 
	 * @param fileStorage the object to read file storage
	 * @return a <code>Statistics</code> object read from persistent storage 
	 * or an empty <code>Statistics</code> object if there is nothing on the persistent storage
	 * @throws Exception
	 * @see FileStatisticsStorage
	 */
	public static Statistics build(FileStatisticsStorage fileStorage) throws Exception {
		if(statistics != null){
			return statistics;
		}
		
		if( null != (statistics = fileStorage.retrieve())){
			return statistics;
		}
		
		return new Statistics();
				
	}
}
