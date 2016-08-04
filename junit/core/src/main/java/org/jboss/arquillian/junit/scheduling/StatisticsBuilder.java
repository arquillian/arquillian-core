package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.FileStatisticsStorage;

public class StatisticsBuilder {
	private static Statistics statistics;
	
	// Invoke for test purposes only
	@SuppressWarnings("unused")
	private static void set(Statistics stats){
		statistics = stats;
	}
	
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
