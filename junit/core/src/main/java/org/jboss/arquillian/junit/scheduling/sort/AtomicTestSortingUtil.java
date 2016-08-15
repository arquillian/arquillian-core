package org.jboss.arquillian.junit.scheduling.sort;

import java.util.ArrayList;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus;
import org.junit.runner.Description;

/**
 * Provides sorting of test classes and 
 * atomic tests by the <code>LatestFailedScheduling</code> strategy.
 *
 */
public class AtomicTestSortingUtil {
	
	/**
	 * Calculates a class's fail-to-pass factor.
	 * <p>
	 * This method uses every individual atomic test's fail-to-pass factor
	 * to calculate a sum which forms a class's fail-to-pass factor by which
	 * a test class is compared and sorted.
	 * If a test method has a null <code>TestStatus</code> its fail-to-pass factor is 0.
	 * 
	 * @param a statistics object providing a method's <code>TestStatus</code> 
	 * @param atomicTests the children of the test class by which to calculate the fail-to-pass factor
	 * @return the class's fail-to-pass factor
	 */
	private int getTestClassFailToPassFactor(Statistics statistics,ArrayList<Description> atomicTests){
		int failToPassFactor = 0;
		for(Description atomicTest : atomicTests){
			TestStatus atomicTestStatus = statistics.getTestStatus(
					atomicTest.getClassName(), atomicTest.getMethodName());
			
			if(atomicTestStatus != null){
				failToPassFactor+= atomicTestStatus.getFailures() -
						atomicTestStatus.getPasses();
			}
			
		}
		
		return failToPassFactor;
	}
	
	/**
	 * Sorts the given atomic tests using based on their <code>TestStatus</code>.
	 * <p>
	 * This method subtracts the successes from the failures of the specified method
	 * and forms its fail-to-pass factor by which it is sorted.
	 * If a method has a null <code>TestStatus</code> the methods retain their
	 * original sorting.
	 * 
	 *  
	 * @param statistics a statistics object providing a method's <code>TestStatus</code>
	 * @param o1 the first object to be compared
	 * @param o2 the first object to be compared
	 * @return a negative integer, zero, or a positive integer
	 *  as the first argument is less than, equal to, or greater than the second
	 */
	public int sortByLatestFailed(Statistics statistics,Description o1, Description o2){
		TestStatus o1TestStatus;
		TestStatus o2TestStatus;

		// Gets the failures and passes of the specified test
		o1TestStatus = statistics.getTestStatus(o1.getClassName(),o1.getMethodName());
		o2TestStatus = statistics.getTestStatus(o2.getClassName(),o2.getMethodName());
		
		if(o1TestStatus == null && o2TestStatus == null){
			return 0;
		}
		
		int o1FailToPassFactor = o1TestStatus.getFailures() - o1TestStatus.getPasses();
		int o2FailToPassFactor = o2TestStatus.getFailures() - o2TestStatus.getPasses();
		
		// Tests with more failures and less passes will be run first
		return o2FailToPassFactor - o1FailToPassFactor;
	}
	
	/**
	 * Sorts the given test classes based on their individual test methods'<code>TestStatus</code>.
	 *  
	 * @param statistics a statistics object providing a method's <code>TestStatus</code>
	 * @param o1 the first object to be compared
	 * @param o2 the first object to be compared
	 * @return a negative integer, zero, or a positive integer
	 *  as the first argument is less than, equal to, or greater than the second
	 * @see #getTestClassFailToPassFactor
	 */
	public int sortClassesByLatestFailed(Statistics statistics,Description o1,Description o2){
		// Sort the test classes by their atomic tests' fail-to-pass-factors
		return getTestClassFailToPassFactor(statistics,o2.getChildren())
				- getTestClassFailToPassFactor(statistics,o1.getChildren());
	}
}
