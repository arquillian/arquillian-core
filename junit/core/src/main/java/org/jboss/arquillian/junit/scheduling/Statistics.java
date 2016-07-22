package org.jboss.arquillian.junit.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.ClassStatus;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus;

public class Statistics {

	private Map<String,ClassStatus> runStatistics;
	
	public Statistics() {
		runStatistics = new HashMap<String,ClassStatus>();
	}

	// Maps the testStatus using the description's class name and method name
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
	
	// Returns null if the given key is not found
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
