package org.jboss.arquillian.junit.scheduling.scheduler;

import java.lang.reflect.Constructor;

import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.LatestFailedScheduler;

public class SchedulerBuilder {

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
