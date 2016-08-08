package org.jboss.arquillian.junit.scheduling.sort;

import java.util.ArrayList;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus;
import org.junit.runner.Description;

public class AtomicTestSortingUtil {
	
	private int getTestClassFailToPassFactor(Statistics statistics,ArrayList<Description> atomicTests){
		int failToPassFactor = 0;
		for(Description atomicTest : atomicTests){
			TestStatus atomicTestStatus = statistics.getTestStatus(
					atomicTest.getClassName(), atomicTest.getMethodName());
			failToPassFactor+= atomicTestStatus.getFailures() - atomicTestStatus.getPasses();
		}
		
		return failToPassFactor;
	}
	
	public int sortByLatestFailed(Statistics statistics,Description o1, Description o2){
		TestStatus o1TestStatus;
		TestStatus o2TestStatus;

		// Gets the failures and passes of the specified test
		o1TestStatus = statistics.getTestStatus(o1.getClassName(),o1.getMethodName());
		o2TestStatus = statistics.getTestStatus(o2.getClassName(),o2.getMethodName());

		int o1FailToPassFactor = o1TestStatus.getFailures() - o1TestStatus.getPasses();
		int o2FailToPassFactor = o2TestStatus.getFailures() - o2TestStatus.getPasses();
		
		// Tests with more failures and less passes will be run first
		return o2FailToPassFactor - o1FailToPassFactor;
	}
	
	public int sortClassesByLatestFailed(Statistics statistics,Description o1,Description o2){
		// Sort the test classes by their atomic tests' fail-to-pass-factors
		return getTestClassFailToPassFactor(statistics,o2.getChildren())
				- getTestClassFailToPassFactor(statistics,o1.getChildren());
	}
}
