package org.jboss.arquillian.junit.scheduling.scheduler;


/**
 * A listener used for Schedulers
 * <p>
 * This listener provides all the methods specified in {@link org.junit.runner.notification.RunListener}
 * The user can implement scheduler specific behavior in each of the interface's methods. 
 *
 */
public interface SchedulerListener {
	public void testRunStarted() throws Exception;
	public void testStarted(String className, String testName) throws Exception;
	public void testFailure(String className, String testName, String reason) throws Exception;
	public void testRunFinished() throws Exception;
}
