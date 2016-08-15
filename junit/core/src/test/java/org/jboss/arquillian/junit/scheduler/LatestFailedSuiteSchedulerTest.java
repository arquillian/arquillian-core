package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.junit.JUnitTestBaseClass;
import org.jboss.arquillian.junit.scheduler.mocks.test.latestfailedsuite.FirstTestClass;
import org.jboss.arquillian.junit.scheduler.mocks.test.latestfailedsuite.SecondTestClass;
import org.jboss.arquillian.junit.scheduler.mocks.test.latestfailedsuite.ThirdTestClass;
import org.jboss.arquillian.junit.scheduling.ArquillianSuiteScheduling;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.LatestFailedSchedulerParams;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite.SuiteClasses;

public class LatestFailedSuiteSchedulerTest extends JUnitTestBaseClass{
	@Test
	public void shouldRunChangedTestClassesFirst() throws Exception{
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
				
		// Run the test
		final Set<String> pickedTestsSet = new LinkedHashSet<String>();
		final List<String> sortedTets = new ArrayList<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				sortedTets.add(description.getMethodName());
				pickedTestsSet.add(description.getClassName());
			}
		},TestSuite.class);
		
		assertTrue(!result.wasSuccessful());
		
		// Check atomic test sorting
		assertEquals("Number of run tests does not match", 8 ,sortedTets.size());
		assertEquals("Error in sorting tests","test1",sortedTets.get(0));
		assertEquals("Error in sorting tests","test2",sortedTets.get(1));
		assertEquals("Error in sorting tests","test3",sortedTets.get(2));
		
		assertEquals("Error in sorting tests","test7",sortedTets.get(3));
		assertEquals("Error in sorting tests","test6",sortedTets.get(4));
		assertEquals("Error in sorting tests","test8",sortedTets.get(5));
		
		assertEquals("Error in sorting tests","test4",sortedTets.get(6));
		assertEquals("Error in sorting tests","test5",sortedTets.get(7));
		
		// Tests suite sorting
		List<String> pickedClasses = new ArrayList<String>(pickedTestsSet);	
		
		assertEquals("Count of run classes does not match",3,pickedClasses.size());

		assertEquals("Error in sorting test classes",
				FirstTestClass.class.getName(), pickedClasses.get(0));
		assertEquals("Error in sorting test classes",
				ThirdTestClass.class.getName(), pickedClasses.get(1));
		assertEquals("Error in sorting test classes",
				SecondTestClass.class.getName(), pickedClasses.get(2));
	}
	
	@RunWith(ArquillianSuiteScheduling.class)
	@SuiteClasses({SecondTestClass.class,FirstTestClass.class,ThirdTestClass.class})
	@LatestFailedSchedulerParams(storagePath="statistics-suite.xml")
	public static class TestSuite{
		
	}
}
