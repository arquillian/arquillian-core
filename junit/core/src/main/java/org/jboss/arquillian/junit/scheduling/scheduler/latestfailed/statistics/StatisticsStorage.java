package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import org.jboss.arquillian.junit.scheduling.Statistics;

public interface StatisticsStorage {
	void store(Statistics stats) throws Exception;
	Statistics retrieve() throws Exception;
}
