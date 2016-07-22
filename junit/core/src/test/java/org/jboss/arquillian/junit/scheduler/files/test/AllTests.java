package org.jboss.arquillian.junit.scheduler.files.test;

import org.jboss.arquillian.junit.scheduling.ArquillianScheduling;
import org.jboss.arquillian.junit.scheduling.scheduler.ScheduleWith;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesScheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.ChangedFilesSchedulerParams;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UnchangedClassTest.class,FirstChangedClassTest.class, SecondChangedClassTest.class })
public class AllTests {

}
