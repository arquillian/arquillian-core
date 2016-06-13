package org.jboss.arquillian.junit.scheduling.statistics;

import org.jboss.arquillian.junit.scheduling.Statistics;

public interface StatisticsStorage {
	void store(Statistics stats) throws Exception;
	Statistics retrieve() throws Exception;
}
