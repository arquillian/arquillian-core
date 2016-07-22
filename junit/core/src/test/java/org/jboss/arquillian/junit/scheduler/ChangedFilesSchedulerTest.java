package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.junit.JUnitTestBaseClass;
import org.jboss.arquillian.junit.scheduler.files.src.FirstChangedClass;
import org.jboss.arquillian.junit.scheduler.files.src.SecondChangedClass;
import org.jboss.arquillian.junit.scheduler.files.test.AllTests;
import org.jboss.arquillian.junit.scheduler.files.test.FirstChangedClassTest;
import org.jboss.arquillian.junit.scheduler.files.test.SecondChangedClassTest;
import org.jboss.arquillian.junit.scheduler.files.test.UnchangedClassTest;
import org.jboss.arquillian.junit.scheduling.ArquillianScheduling;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesBuilder;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesScheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesSchedulerParams;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.GitChangesResolver;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

public class ChangedFilesSchedulerTest extends JUnitTestBaseClass{
	
//	private static final String TEST_CLASS_1_CONTENT ="package test;\n"
//			+ "import ChangedClass1;\n"
//			+ "@RunWith(ArquillianScheduling.class)\n"
//			+ "@ScheduleWith(ChangedFilesScheduler.class)\n"
//			+ "@ChangedFilesSchedulerParams(workingDir=\"org/jboss/arquillian/junit/scheduler/src\","
//			+ "testDir = \"org/jboss/arquillian/junit/scheduler/test\")\n"
//			+ "public class Test1{\n"
//			+ "@Test public void test(){}\n}";
//	
//	private Class<?> testClass;
// 	
//	@Before
//	public void setUp() throws Exception{
//		URL resourceURL = getClass().getClassLoader()
//				.getResource("org/jboss/arquillian/junit/scheduler");
//		
//		File resourceDir = new File(resourceURL.toURI());
//		
//		if(!resourceDir.exists()){
//			throw new Exception("Resource directory not found!");
//		}
//		
//		File sourceDir = new File(resourceDir, "src");
//		sourceDir.mkdir();
//		//sourceDir.deleteOnExit();
//		
//		File testDir = new File(resourceDir, "test");
//		testDir.mkdir();
//		//testDir.deleteOnExit();
//		
//		File changedClass1 = new File(sourceDir,"ChangedClass1.java");//File.createTempFile("ChangedCLass1", ".java", sourceDir);
//		changedClass1.createNewFile();
//		//changedClass1.deleteOnExit();
//		
//		File importingTest1 = new File(testDir,"Test1.java");
//		//importingTest1.deleteOnExit();
//		FileOutputStream test1Output = new FileOutputStream(importingTest1);
//		
//		try{
//			test1Output.write(TEST_CLASS_1_CONTENT.getBytes());
//		}catch(Exception err){
//			throw err;
//		}finally{
//			if(test1Output != null) test1Output.close();
//		}
//		
//	}
	
	@Test
	public void schouldTestChangedClassesFirst() throws Exception  {
		
		TestRunnerAdaptor adaptor = mock(TestRunnerAdaptor.class);
		executeAllLifeCycles(adaptor);
		
		Set<String> changedClasses = new HashSet<String>(3);
		changedClasses.add("org.jboss.arquillian.junit.scheduler.files.src.FirstChangedClass");
		changedClasses.add("org.jboss.arquillian.junit.scheduler.files.src.SecondChangedClass");
		changedClasses.add("org.jboss.arquillian.junit.scheduler.files.src.ChangedClassNoTest");
		
		// Invoke te builder's set method
		Class<?> changedFilesBuilderClass = ChangedFilesBuilder.class;
		Method method =changedFilesBuilderClass.getDeclaredMethod("set", Set.class);
		method.setAccessible(true);
		method.invoke(null, changedClasses);
				
		// Run the test
		final List<String> pickedTests = new ArrayList<String>();
		Result result = run(adaptor, new RunListener() {
			@Override
			public void testStarted(Description description) throws Exception {
				pickedTests.add(description.getClassName());
			}
		},AllTests.class);

		assertTrue(result.wasSuccessful());
		for(String testName: pickedTests){
			System.out.println(testName);
		}
		
		assertEquals(3, pickedTests.size());
		assertEquals("Tests were not sorted properly!",
				"org.jboss.arquillian.junit.scheduler.files.test.UnchangedClassTest"
				,pickedTests.get(2));
	}
}
