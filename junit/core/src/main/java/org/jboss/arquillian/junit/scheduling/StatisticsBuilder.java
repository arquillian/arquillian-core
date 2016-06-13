package org.jboss.arquillian.junit.scheduling;

import org.jboss.arquillian.junit.scheduling.statistics.FileStatisticsStorage;

public class StatisticsBuilder {
	private static Statistics statistics;
	private static final FileStatisticsStorage fileStorage = new FileStatisticsStorage();
	
	// Invoke for test purposes only
	private static void set(Statistics stats){
		statistics = stats;
	}
	
	public static Statistics build() {
		if(statistics != null){
			return statistics;
		}
		
		try {
			return fileStorage.retrieve();
		} catch (Exception e) {
			return new Statistics(); 
		}
	}
}
