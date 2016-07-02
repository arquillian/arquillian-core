package org.jboss.arquillian.junit.scheduling.scheduler;

public interface SchedulerListener {
	public void testRunStarted() throws Exception;
	public void testStarted(String className, String testName) throws Exception;
	public void testFailure(String className, String testName, String reason) throws Exception;
	public void testRunFinished() throws Exception;
}
