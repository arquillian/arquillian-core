package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.junit.JUnitTestBaseClass;
import org.jboss.arquillian.junit.scheduler.mocks.src.ChangedClassNoTest;
import org.jboss.arquillian.junit.scheduler.mocks.src.FirstChangedClass;
import org.jboss.arquillian.junit.scheduler.mocks.src.SecondChangedClass;
import org.jboss.arquillian.junit.scheduler.mocks.test.FirstChangedClassTest;
import org.jboss.arquillian.junit.scheduler.mocks.test.SecondChangedClassTest;
import org.jboss.arquillian.junit.scheduler.mocks.test.UnchangedClassTest;
import org.jboss.arquillian.junit.scheduling.ArquillianSuiteScheduling;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesSuiteScheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesSuiteSchedulerParams;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite.SuiteClasses;

public class ArquillianSuiteSchedulingTest extends JUnitTestBaseClass{
	
	@Test
	public void shouldRunChangedTestClassesFirst() throws Exception{
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
		
		// Set up changed classes
		Set<String> changedClasses = new HashSet<String>(3);
		changedClasses.add(FirstChangedClass.class.getName());
		changedClasses.add(SecondChangedClass.class.getName());
		changedClasses.add(ChangedClassNoTest.class.getName());
		
		// Invoke the builder's set method
		Class<?> changedFilesBuilderClass = ChangedFilesBuilder.class;
		Method method =changedFilesBuilderClass.getDeclaredMethod("set", Set.class);
		method.setAccessible(true);
		method.invoke(null, changedClasses);
				
		// Run the test
		final Set<String> pickedTests = new LinkedHashSet<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getClassName());
			}
		},TestSuite.class);
		
		List<String> pickedClasses = new ArrayList<String>(pickedTests);

		assertTrue(result.wasSuccessful());
		
		assertEquals(3, pickedClasses.size());
		assertEquals("Tests were not sorted properly!",
				"org.jboss.arquillian.junit.scheduler.files.test.UnchangedClassTest"
				,pickedClasses.get(2));
	}
	
	@Test
	public void shouldRunOnlyChangedTestClasses() throws Exception{
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
		
		// Set up changed classes
		Set<String> changedClasses = new HashSet<String>(3);
		changedClasses.add(FirstChangedClass.class.getName());
		changedClasses.add(SecondChangedClass.class.getName());
		changedClasses.add(ChangedClassNoTest.class.getName());
	
		// Invoke the builder's set method
		Class<?> changedFilesBuilderClass = ChangedFilesBuilder.class;
		Method method =changedFilesBuilderClass.getDeclaredMethod("set", Set.class);
		method.setAccessible(true);
		method.invoke(null, changedClasses);
				
		// Run the test
		final Set<String> pickedTests = new LinkedHashSet<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getClassName());
			}
		},TestSuiteWithFiltering.class);

		assertTrue(result.wasSuccessful());
		
		assertEquals(2, pickedTests.size());
		assertTrue("Tests were not filtered properly!"
				,pickedTests.contains("org.jboss.arquillian.junit.scheduler.files.test.FirstChangedClassTest"));
		assertTrue("Tests were not filtered properly!"
				,pickedTests.contains("org.jboss.arquillian.junit.scheduler.files.test.SecondChangedClassTest"));
	}

	@RunWith(ArquillianSuiteScheduling.class)
	@SuiteClasses({UnchangedClassTest.class,SecondChangedClassTest.class,FirstChangedClassTest.class })
	@ScheduleWith(ChangedFilesSuiteScheduler.class)
	@ChangedFilesSuiteSchedulerParams(
		workingDir="src/test/java/org/jboss/arquillian/junit/scheduler/files/src",
		testDir = "src/test/java/org/jboss/arquillian/junit/scheduler/files/test"
	)
	public static class TestSuite{
		
	}
	
	@RunWith(ArquillianSuiteScheduling.class)
	@SuiteClasses({UnchangedClassTest.class,SecondChangedClassTest.class,FirstChangedClassTest.class })
	@ScheduleWith(ChangedFilesSuiteScheduler.class)
	@ChangedFilesSuiteSchedulerParams(
		workingDir="src/test/java/org/jboss/arquillian/junit/scheduler/files/src",
		testDir = "src/test/java/org/jboss/arquillian/junit/scheduler/files/test",
		runOnlyChangedFiles = true
	)
	public static class TestSuiteWithFiltering{
		
	}
}
