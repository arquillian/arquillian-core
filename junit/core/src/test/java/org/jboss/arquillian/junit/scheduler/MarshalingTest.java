package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.statistics.AbstractStatisticsStorage;
import org.jboss.arquillian.junit.scheduling.statistics.model.TestStatus;
import org.junit.Test;

public class MarshalingTest {
	
	@Test
	public void marshalTest() throws Exception {	
	
		//Initialize statistics
		Statistics stats = new Statistics();
		stats.recordTestStarted("FirstTestCase","test1");
		stats.recordTestFailure("FirstTestCase","test1");
		
		stats.recordTestStarted("FirstTestCase","test2");
		stats.recordTestStarted("FirstTestCase","test2");
		stats.recordTestStarted("FirstTestCase","test2");
		stats.recordTestFailure("FirstTestCase","test2");
		
		stats.recordTestStarted("SecondTestCase","test3");
		stats.recordTestStarted("SecondTestCase","test3");
		stats.recordTestStarted("SecondTestCase","test3");
		stats.recordTestFailure("SecondTestCase","test3");
		stats.recordTestFailure("SecondTestCase","test3");
		
		stats.recordTestStarted("SecondTestCase","test4");
				

		PipedReader reader = new PipedReader();
		final Writer writer = new PipedWriter(reader);
		
		// Marshal the statistics to a StringWriter
		AbstractStatisticsStorage abstractStorage = new AbstractStatisticsStorage() {
			@Override
			public void store(Statistics stats) throws Exception {
				marshal(stats, writer);
				writer.close();
			}
			@Override
			public Statistics retrieve() throws Exception { return null;}
		};
		abstractStorage.store(stats);
		
		byte[] testFileBytes =
				Files.readAllBytes(Paths.get(
						getClass()
						.getClassLoader()
						.getResource("org/jboss/arquillian/junit/scheduler/testData.xml")
						.toURI()));
		String testFileContent = new String(testFileBytes, Charset.defaultCharset());
		
		BufferedReader marshaledFile = new BufferedReader(reader);
		
		String nextLine;
		while(null != (nextLine = marshaledFile.readLine())){
			// The statistics are stored in random order
			// So testFileContent is checked to see if it contains
			// Every line of the marshaled file  
			assertTrue("Files do not match!", testFileContent.contains(nextLine));
		}
		
		marshaledFile.close();
		
	}
	
	@Test
	public void UnmarshalTest() throws Exception {
	
		final Reader reader = new InputStreamReader(
				getClass()
				.getClassLoader()
				.getResourceAsStream("org/jboss/arquillian/junit/scheduler/testData.xml"));
		
		// Marshal the statistics to a StringWriter
		AbstractStatisticsStorage abstractStorage = new AbstractStatisticsStorage() {
			@Override
			public void store(Statistics stats) throws Exception {}
			@Override
			public Statistics retrieve() throws Exception { 
				Statistics statistics = unmarshal(reader);
				reader.close();
				
				return statistics;
			}
		};
		
		// Retrieve status from storage
		Statistics stats = abstractStorage.retrieve();
		
		// Get test statuses
		TestStatus test1TestStatus = stats.getTestStatus("FirstTestCase", "test1");
		TestStatus test2TestStatus = stats.getTestStatus("FirstTestCase", "test2");
		TestStatus test3TestStatus = stats.getTestStatus("SecondTestCase", "test3");
		TestStatus test4TestStatus = stats.getTestStatus("SecondTestCase", "test4");
		
		assertEquals("Test 1 passes do not match", 0, test1TestStatus.getPasses());
		assertEquals("Test 1 failures do not match", 1, test1TestStatus.getFailures());
		
		assertEquals("Test 2 passes do not match", 2, test2TestStatus.getPasses());
		assertEquals("Test 2 failures do not match", 1, test2TestStatus.getFailures());
		
		assertEquals("Test 3 passes do not match", 1, test3TestStatus.getPasses());
		assertEquals("Test 3 failures do not match", 2, test3TestStatus.getFailures());
		
		assertEquals("Test 4 passes do not match", 1, test4TestStatus.getPasses());
		assertEquals("Test 4 failures do not match", 0, test4TestStatus.getFailures());
		
	}
	
	public static class FirstTestCase{
		@Test
		public void test1() {
		}
		
		@Test
		public void test2() {
		}
	}
	
	public static class SecondTestCase{
		@Test
		public void test3() {
		}
		
		@Test
		public void test4() {
		}
	}

}
