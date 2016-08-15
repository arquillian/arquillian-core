package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A run time storage for test method statistics.
 * <p>
 * A single test method's statistic information is
 * mapped using its name as a key and a <code>TestStatus</code>
 * object as a value.
 * 
 *@see TestStatus
 */
public class ClassStatus {
	
	private Map<String,TestStatus> classStatus = new HashMap<String,TestStatus>();
	
	/**
	 * Maps a <code>TestStatus</code> object to the given <code>methodName</code>
	 * 
	 * @param methodName a test method name
	 * @param testStatus the test method's <code>TestStatus</code>
	 */
	public void recordStatus(String methodName, TestStatus testStatus){
		classStatus.put(methodName, testStatus);
	}
	
	public TestStatus getStatus(String methodName){
		return classStatus.get(methodName);
	}
	
	public Set<String> getTestNames(){
		return classStatus.keySet();
	}
	
	public List<TestStatus> getTestStatuses(){
		return new ArrayList<TestStatus>(classStatus.values());
	}
}
