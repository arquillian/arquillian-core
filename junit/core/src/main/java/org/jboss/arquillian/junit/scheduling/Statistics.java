package org.jboss.arquillian.junit.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.ClassStatus;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus;

/**
 * Runtime storage for test statistics.
 * <p>
 * A single test method's statistic information is
 * mapped using both the name of the class that contains 
 * the method and the method name itself.
 * This is achieved by a map associating a test class's name (the key)
 * with another map (the value), containing statistics information
 * on the individual test methods inside the class which name was used as key.
 * For example a test named test1 part of class TestClass1 would be mapped like so:
 * <p>
 * Statistics map: key - TestClass1 with  value - TestMap (encapsulated in {@link ClassStatus})
 * TestMap: key - test1 with value - some test statistics (encapsulated in {@link TestStatus}) 
 *
 */
public class Statistics {

	private Map<String,ClassStatus> runStatistics;
	
	public Statistics() {
		runStatistics = new HashMap<String,ClassStatus>();
	}

	/**
	 * Maps test method statistics (<code>TestStatus</code>)
	 * by the specified class name and method name.
	 * 
	 * @param className a test class containing test methods
	 * @param methodName the name of the test method contained in the test class
	 * @param testStatus the method's statistics information
	 */
	private void recordTestMethod(String className, String methodName, TestStatus testStatus){			
		ClassStatus classStatus = new ClassStatus();
		classStatus.recordStatus(methodName, testStatus);
		
		runStatistics.put(className, classStatus);
	}
	
	private TestStatus getPassStatus(){
		TestStatus testStatus = new TestStatus();
		testStatus.recordPass();
		
		return testStatus;
	}
	
	/**
	 * Maps a test method with a single pass in a <code>TestStatus</code> object.
	 * <p>
	 * Note that this method assumes all started tests pass. 
	 * If the test fails the <code>recordTestFailure</code> method removes
	 * the initial pass and adds a failure.
	 * If a map for the class already exists a new TestStatus with one pass
	 * is created. If an element is already mapped its <code>TestStatus</code> 
	 * is updated.
	 * 
	 * @param className a test class containing test methods
	 * @param methodName the name of the test method contained in the test class
	 * @throws Exception
	 * @see #recordTestMethod
	 */
	public void recordTestStarted(String className, String methodName)throws Exception{
		if(!runStatistics.containsKey(className)){
			recordTestMethod(className,methodName,getPassStatus());
		}else{
			TestStatus testStatus = getTestStatus(className,methodName);
			if(testStatus == null){
				runStatistics.get(className).recordStatus(methodName, getPassStatus());	
			}else{
				testStatus.recordPass();
			}	
		}	
	}
	
	/**
	 * Maps a test method with a single failure in a <code>TestStatus</code> object.
	 * <p>
	 * Note that this method assumes that the <code>recordTestStarted</code> method
	 * has been called once, before this method, with the same list of parameters.
	 * For example:
	 * <p>
	 * First start a test method <code>recordTestStarted</code>.
	 * Then call this method to record a failure to the same test method (using the same parameters)
	 * 
	 * 
	 * @param className a test class containing test methods
	 * @param methodName the name of the test method contained in the test class
	 * @throws Exception
	 */
	public void recordTestFailure(String className, String methodName) throws Exception{
		TestStatus testStatus = getTestStatus(className,methodName);
		
		if(testStatus != null){
			// Remove the initial pass given to all started methods
			testStatus.removePass();
			testStatus.recordFailure();
		}else{
			throw new Exception("Test method: " + methodName
			+ " from class:" + className
			+" not found in runtime statistics!");
		}
	}
	
	/**
	 * Finds a mapped <code>TestStatus</code> specified by 
	 * a test class name and a test method name within the same class.
	 * 
	 * @param className a test class containing test methods
	 * @param methodName the name of the test method contained in the test class
	 * @return the specified <code>TestStatus</code>, an empty <code>TestStatus</code>
	 * if the given <code>methodName</code> does not correspond to an existing statistics entry
	 * or null if the <code>className</code> does not
	 * correspond to an existing statistics entry. 
	 */
	public TestStatus getTestStatus(String className, String methodName){
		
		if(runStatistics.containsKey(className)){
			return runStatistics.get(className).getStatus(methodName);
		}
		
		return null;	
	}
	
	public void reset(){
		runStatistics.clear();
	}
	
	public int size(){
		return runStatistics.size();
	}
	
	/**
	 * Maps an entire test class including the test methods.
	 * 
	 * @param className a test class containing test methods
	 * @param classStatus a class containing a map with method names as keys 
	 * and <code>TestStatus</code>s for values 
	 */
	public void addTestClass(String className, ClassStatus classStatus){
		runStatistics.put(className, classStatus);
	}
	
	public Set<String> getClassNames(){
		return runStatistics.keySet();
	}
	
	public List<ClassStatus> getClasses(){
		return new ArrayList<ClassStatus>(runStatistics.values());
	}
}
