package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;

import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.List;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.statistics.AbstractStatisticsStorage;
import org.jboss.arquillian.junit.scheduling.statistics.model.ClassStatus;
import org.jboss.arquillian.junit.scheduling.statistics.model.TestStatus;
import org.junit.Test;
import org.junit.runner.Description;

public class MarshalingTest {
	
	@Test
	public void MarshalUnmarshalTest() throws Exception{
		
		Description test1Description = Description.createTestDescription(FirstTestCase.class, "test1");
		Description test2Description = Description.createTestDescription(FirstTestCase.class, "test2");
		
		Description test3Description = Description.createTestDescription(SecoundTestCase.class, "test3");
		Description test4Description = Description.createTestDescription(SecoundTestCase.class, "test4");
		
		// Initialize statistics
		Statistics stats = new Statistics();
		stats.recordTestStarted(test1Description);
		stats.recordTestStarted(test2Description);
		stats.recordTestStarted(test3Description);
		stats.recordTestStarted(test4Description);
		
		final PipedReader reader = new PipedReader();
		final Writer writer = new PipedWriter(reader);
		
		// Marshal the statistics to a StringWriter
		AbstractStatisticsStorage abstractStorage = new AbstractStatisticsStorage() {
			@Override
			public void store(Statistics stats) throws Exception {
				marshal(stats, writer);	
				writer.close();
			}
			@Override
			public Statistics retrieve() throws Exception {
				Statistics statistics = unmarshal(reader);
				reader.close();
				
				return statistics;
			}
		};
		
		abstractStorage.store(stats);
		
		// Unmarshal the result
		Statistics unmarshaledStats = 
				abstractStorage.retrieve();
		
		assertNotNull("Colud no read xml data", unmarshaledStats);
		assertEquals("Sizes are not equal",unmarshaledStats.size(),stats.size());
		assertEquals("Class names do not match",unmarshaledStats.getClassNames(),stats.getClassNames());
		
		List<ClassStatus> classStats = stats.getClasses();
		List<ClassStatus> unmarshalledClassStats = unmarshaledStats.getClasses();
		
		ClassStatus testClass1 = classStats.get(0);
		ClassStatus testClass2 = classStats.get(1);
		
		ClassStatus unmarshaledTestClass1 = unmarshalledClassStats.get(0);
		ClassStatus unmarshaledTestClass2 = unmarshalledClassStats.get(1);
		
		assertEquals("Test names for the first class do not match!",
				testClass1.getTestNames(),
				unmarshaledTestClass1.getTestNames());
		
		assertEquals("Test names for the secound class do not match!",
				testClass2.getTestNames(),
				unmarshaledTestClass2.getTestNames());
		
		List<TestStatus> testStatsClass1 = testClass1.getTestStatuses();
		List<TestStatus> testStatsClass2 = testClass2.getTestStatuses();
		
		List<TestStatus> unmarshaledTestStatsClass1 = unmarshaledTestClass1.getTestStatuses();
		List<TestStatus> unmarshaledTestStatsClass2 = unmarshaledTestClass2.getTestStatuses();
		
		assertEquals("Number of test status instances for the first class do not match!",
				testStatsClass1.size(),
				unmarshaledTestStatsClass1.size());
		
		assertEquals("Number of test status instances for the secound class do not match!",
				testStatsClass2.size(),
				unmarshaledTestStatsClass2.size());
		
		TestStatus test1TestStatus = testStatsClass1.get(0);
		TestStatus test2TestStatus = testStatsClass1.get(1);
		TestStatus test3TestStatus = testStatsClass2.get(0);
		TestStatus test4TestStatus = testStatsClass2.get(1);
		
		TestStatus unmarshaledTest1TestStatus = unmarshaledTestStatsClass1.get(0);
		TestStatus unmarshaledTest2TestStatus = unmarshaledTestStatsClass1.get(1);
		TestStatus unmarshaledTest3TestStatus = unmarshaledTestStatsClass2.get(0);
		TestStatus unmarshaledTest4TestStatus = unmarshaledTestStatsClass2.get(1);
		
		// Checks if the actual TestStatus objects are equal to the unmarshaled 
		assertEquals(test1TestStatus.getPassed(),unmarshaledTest1TestStatus.getPassed());
		assertEquals(test1TestStatus.getFailures(),unmarshaledTest1TestStatus.getFailures());
		
		assertEquals(test2TestStatus.getPassed(),unmarshaledTest2TestStatus.getPassed());
		assertEquals(test2TestStatus.getFailures(),unmarshaledTest2TestStatus.getFailures());
		
		assertEquals(test3TestStatus.getPassed(),unmarshaledTest3TestStatus.getPassed());
		assertEquals(test3TestStatus.getFailures(),unmarshaledTest3TestStatus.getFailures());
		
		assertEquals(test4TestStatus.getPassed(),unmarshaledTest4TestStatus.getPassed());
		assertEquals(test4TestStatus.getFailures(),unmarshaledTest4TestStatus.getFailures());		
	}
	
	public static class FirstTestCase{
		@Test
		public void test1() {
		}
		
		@Test
		public void test2() {
		}
	}
	
	public static class SecoundTestCase{
		@Test
		public void test3() {
		}
		
		@Test
		public void test4() {
		}
	}

}
