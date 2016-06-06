package org.jboss.arquillian.junit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

public class ArquillianSchedulingTest extends JUnitTestBaseClass {
	
	@Test
	public void shouldInvokeOnlyTest1And2Inorder() throws Exception {
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);

		final List<String> pickedTests = new ArrayList<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getMethodName());
			}
		}, FirstTestCase.class);

		assertTrue(result.wasSuccessful());

		assertEquals("Unexpected size!", 2, pickedTests.size());
		assertEquals("Error in filtering and sorting tests!","test1", pickedTests.get(0));
		assertEquals("Error in filtering and sorting tests!","test2", pickedTests.get(1));
	}
	
	@Test
	public void executeTestsInSuite() throws Exception{
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
		
		final List<String> pickedTests = new ArrayList<String>(0);
		Result result = run(adaptor,new RunListener(){
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getMethodName());
			}
		},TestSuite.class);
		
		assertTrue(result.wasSuccessful());
		
		assertEquals("Unexpected size!", 4, pickedTests.size());
		assertEquals("Error in filtering and sorting tests!","test1", pickedTests.get(0));
		assertEquals("Error in filtering and sorting tests!","test2", pickedTests.get(1));
		assertEquals("Error in filtering and sorting tests!","test21", pickedTests.get(2));
		assertEquals("Error in filtering and sorting tests!","test22", pickedTests.get(3));
		
	}
	

	@RunWith(ArquillianScheduling.class)
	public static class FirstTestCase {
		@Test
		public void test2() {
		}
		
		@Test
		public void test1() {
		}

		@Test
		public void test3() {
		}
		
	}
	
	@RunWith(ArquillianScheduling.class)
	public static class SecondTestCase{
		@Test
		public void test21() {
		}

		@Test
		public void test3() {
		}
		
		@Test
		public void test22() {
		}
		
	}
	
	@RunWith(Suite.class)
	@SuiteClasses({
		FirstTestCase.class,
		SecondTestCase.class
	})
	public static class TestSuite{
		
	}
}
