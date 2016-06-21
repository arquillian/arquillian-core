package org.jboss.arquillian.junit.scheduling.statistics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassStatus {
	
	private Map<String,TestStatus> classStatus = new HashMap<String,TestStatus>();
	
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
