package org.jboss.arquillian.junit.scheduling.statistics;

import org.jboss.arquillian.junit.scheduling.Statistics;

public class FileStatisticsStorage implements StatisticsStorage{

	@Override
	public void store(Statistics stats) throws Exception {
		// TODO Store Statistics to persistent storage
		
	}

	@Override
	public Statistics retrieve() throws Exception {
		// TODO retrieve statistics from persistent storage
		return null;
	}

}
