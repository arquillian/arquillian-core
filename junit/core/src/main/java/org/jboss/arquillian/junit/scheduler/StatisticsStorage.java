package org.jboss.arquillian.junit.scheduler;

public interface StatisticsStorage {
	void store(Statistics stats) throws Exception;
	Statistics retrieve() throws Exception;
}
