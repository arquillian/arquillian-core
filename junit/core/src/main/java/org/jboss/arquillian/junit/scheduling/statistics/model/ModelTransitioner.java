package org.jboss.arquillian.junit.scheduling.statistics.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.statistics.model.valuetypes.ClassStatusValueType;
import org.jboss.arquillian.junit.scheduling.statistics.model.valuetypes.StatisticsValueType;
import org.jboss.arquillian.junit.scheduling.statistics.model.valuetypes.TestStatusValueType;

public class ModelTransitioner {
	
	public Statistics toStatistics(StatisticsValueType statisticsValueType) {
		
		Statistics statistics = new Statistics();
		
		for(ClassStatusValueType classValueType: statisticsValueType.getClasses()){
			ClassStatus classStatus = new ClassStatus();
			
			for(TestStatusValueType testStatusValueType : classValueType.getTests()) {
				// Convert the testStatusValueType to TestSatus
				TestStatus testStatus = new TestStatus();
				testStatus.setFailures(testStatusValueType.getFailures());
				testStatus.setPassed(testStatusValueType.getPassed());
				
				// Record each TestStatus in the classStatus map
				classStatus.recordStatus(testStatusValueType.getName(),testStatus);
			}
			// Record each classStatus in the statistics map
			statistics.addTestClass(classValueType.getName(), classStatus);
		}
		
		return statistics;
	}
	
	public StatisticsValueType toStatisticsValueType(Statistics stats) {
		
		List<ClassStatusValueType> classStatusValueTypes = new ArrayList<ClassStatusValueType>();
		
		Iterator<ClassStatus> classIterator = stats.getClasses().iterator();
		Iterator<String> classNamesIterator = stats.getClassNames().iterator();
		
		// Converts all ClasStatus classes to ClassStatusValueType
		while(classIterator.hasNext() && classNamesIterator.hasNext()){
			ClassStatus classStatus = classIterator.next();
			
			Iterator<TestStatus> testIterator = classStatus.getTestStatuses().iterator();
			Iterator<String> testNameIterator = classStatus.getTestNames().iterator();
			
			// Converts all the TestStatus instances (per test class)
			// in the ClassStatus Map to TestStatusValueType 
			// and stores them in a list
			TestStatusValueType testStatusValueType;
			List<TestStatusValueType> testStatusValueTypes = new ArrayList<TestStatusValueType>();
			while(testIterator.hasNext() && testNameIterator.hasNext()){
				TestStatus testStatus = testIterator.next();
				String testName =  testNameIterator.next();
				
				testStatusValueType = new TestStatusValueType();
				testStatusValueType.setName(testName);
				testStatusValueType.setPassed(testStatus.getPasses());
				testStatusValueType.setFailures(testStatus.getFailures());
				
				testStatusValueTypes.add(testStatusValueType);
			}
			
			ClassStatusValueType classStatusValueType = new ClassStatusValueType();
			String className = classNamesIterator.next();
			classStatusValueType.setName(className);
			classStatusValueType.setTests(testStatusValueTypes);
			
			classStatusValueTypes.add(classStatusValueType);
		}
		
		// Stores all the classStatusValueTypes in the StatisticsValueType wrapper class
		StatisticsValueType statisticsValueType = new StatisticsValueType();
		statisticsValueType.setClasses(classStatusValueTypes);
		
		return statisticsValueType;
	}
}
