package org.jboss.arquillian.junit.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.statistics.model.ClassStatus;
import org.jboss.arquillian.junit.scheduling.statistics.model.TestStatus;
import org.junit.runner.Description;

public class Statistics {

	private Map<String,ClassStatus> runStatistics;
	
	public Statistics() {
		runStatistics = new HashMap<String,ClassStatus>();
	}

	// Maps the testStatus using the description's class name and method name
	private void recordTestMethod(Description description, TestStatus testStatus){			
		ClassStatus classStatus = new ClassStatus();
		classStatus.recordStatus(description.getMethodName(), testStatus);
		
		runStatistics.put(description.getClassName(), classStatus);
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
				runStatistics.get(className).recordStatus(methodName, getPassStatus());	
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
			return runStatistics.get(className).getStatus(description.getMethodName());
		}
		
		return null;	
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
