package org.jboss.arquillian.junit.scheduling.scheduler;

import java.lang.reflect.Constructor;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.LatestFailedScheduler;

/**
 * Builder for schedulers
 * 
 * @author Dimcho Karpachev
 */
public class SchedulerBuilder {

	/**
	 * Builds a scheduler using the annotation's value.
	 * The LatestFailedScheduler is used if <code>annotation</code> is null 
	 * 
	 * @param testClass the JUnit test class currently executed by a Scheduling runner
	 * @param annotation the <code>ScheduleWith</code> annotation in the <code>testClass</code> 
	 * @return the instantiated <code>Scheduler</code>
	 * @throws Exception
	 * @see ScheduleWith, Scheduler, LatestFailedScheduler
	 */
	public static Scheduler buildScheduler(Class<?> testClass,
			ScheduleWith annotation) throws Exception {
		
		Class<? extends Scheduler> schedulerClass;
		
		if(annotation != null){
			schedulerClass = annotation.value();
		}else{
			// Use a default scheduler
			schedulerClass = LatestFailedScheduler.class;
		}
		
		Constructor<? extends Scheduler> schedulerConstructor =
				schedulerClass.getConstructor(Class.class);
		
		return schedulerConstructor.newInstance(testClass);	
	}
	
}
