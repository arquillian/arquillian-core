package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.junit.JUnitTestBaseClass;
import org.jboss.arquillian.junit.scheduling.ArquillianScheduling;
import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.StatisticsBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.LatestFailedScheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

public class ArquillianSchedulingTest extends JUnitTestBaseClass {
	
	@Test
	public void shouldInvokeTestsInTheGivenInorder() throws Exception {
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
		
		// Initialize test description
		Description test1Description = Description.createTestDescription(FirstTestCase.class, "test1");
		Description test2Description = Description.createTestDescription(FirstTestCase.class, "test2");
		Description test3Description = Description.createTestDescription(FirstTestCase.class, "test3");
		
		// Initialize statistics
		Statistics stats = new Statistics();
		stats.recordTestStarted(test1Description);
		stats.recordTestFailure(test1Description);
	
		stats.recordTestStarted(test2Description);
		stats.recordTestFailure(test2Description);
		stats.recordTestStarted(test2Description);
		stats.recordTestFailure(test2Description);
		
		stats.recordTestStarted(test3Description);
		stats.recordTestFailure(test3Description);
		stats.recordTestStarted(test3Description);
		stats.recordTestFailure(test3Description);
		stats.recordTestStarted(test3Description);
		stats.recordTestFailure(test3Description);
			

		// Initialize the runtime statistics
		// using the StatisticsBuilder's set method
		Method method = StatisticsBuilder.class.getDeclaredMethod("set", Statistics.class);
		method.setAccessible(true);
		method.invoke(null, stats);
		
		// Run the test
		final List<String> pickedTests = new ArrayList<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getMethodName());
			}
		}, FirstTestCase.class);

		assertTrue(result.wasSuccessful());
		
		assertEquals("Unexpected size!", 3, pickedTests.size());			
		assertEquals("Error in sorting tests!","test3", pickedTests.get(0));
		assertEquals("Error in sorting tests!","test2", pickedTests.get(1));
		assertEquals("Error in sorting tests!","test1", pickedTests.get(2));
	}
	

	@RunWith(ArquillianScheduling.class)
	@ScheduleWith(LatestFailedScheduler.class)
	public static class FirstTestCase {
		@Test
		public void test1() {
		}
		
		@Test
		public void test2() {
		}

		@Test
		public void test3() {
		}
	}
	
}
