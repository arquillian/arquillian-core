package org.jboss.arquillian.junit.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Description;

public class Statistics {
		
	private Map<String, Map<String, TestStatus>> runStatistics =
			new HashMap<String, Map<String, TestStatus>>();	
	
	// Maps the testStatus using the description's class name and method name
	private void recordTestMethod(Description description, TestStatus testStatus){			
		HashMap<String, TestStatus> testStatistics = new HashMap<String,TestStatus>();
		testStatistics.put(description.getMethodName(), testStatus);
		
		runStatistics.put(description.getClassName(), testStatistics);
	}
	
	private TestStatus getPassStatus(){
		TestStatus testStatus = new TestStatus();
		testStatus.recordPass();
		
		return testStatus;
	}
	
	public void recordTestStarted(Description description)throws Exception{
		String className = description.getClassName();
		String methodName = description.getMethodName();
		
		if(!runStatistics.containsKey(className)){
			recordTestMethod(description, getPassStatus());
		}else{
			TestStatus testStatus = getTestStatus(description);
			if(testStatus == null){
				runStatistics.get(className).put(methodName, getPassStatus());	
			}else{
				testStatus.recordPass();
			}	
		}	
	}
	
	public void recordTestFailure(Description description) throws Exception{
		TestStatus testStatus = getTestStatus(description);
		
		if(testStatus != null){
			// Remove the initial pass given to all started methods
			testStatus.removePass();
			testStatus.recordFailure();
		}else{
			throw new Exception("Test method: " + description.getMethodName() 
			+ " from class:" + description.getClassName() 
			+" not found in runtime statistics!");
		}
	}
	
	// Returns null if the given key is not found
	public TestStatus getTestStatus(Description description){
		String className = description.getClassName();
		
		if(runStatistics.containsKey(className)){
			return runStatistics.get(className).get(description.getMethodName());	
		}
		
		return null;
				
	}
	
}
