package org.jboss.arquillian.junit.scheduler.files.test;

import org.jboss.arquillian.junit.scheduling.ArquillianScheduling;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesScheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesSchedulerParams;
import org.junit.Test;
import org.junit.runner.RunWith;

// Used for test purposes only
// Tests the ChangedFilesScheduler
@RunWith(ArquillianScheduling.class)
@ScheduleWith(ChangedFilesScheduler.class)
@ChangedFilesSchedulerParams(
	workingDir="src/test/java/org/jboss/arquillian/junit/scheduler/files/src",
	testDir = "src/test/java/org/jboss/arquillian/junit/scheduler/files/test"
)
public class UnchangedClassTest {
	@Test
	public void test() {}
}
