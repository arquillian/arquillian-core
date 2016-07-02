package org.jboss.arquillian.junit.scheduling.scheduler;

import java.lang.reflect.Constructor;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public class SchedulerBuilder {
	public static final Scheduler DEFAULT = new Scheduler() {
		
		@Override
		public Sorter getSorter() {
			return Sorter.NULL;
		}
		
		@Override
		public Filter getFilter() {
			return Filter.ALL;
		}
		
		@Override
		public SchedulerListener getSchedulerListener() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public static Scheduler buildScheduler(Class<?> testClass,
			Class<? extends Scheduler> schedulerClass) throws Exception {
		
		Constructor<? extends Scheduler> schedulerConstructor =
				schedulerClass.getConstructor(Class.class);
		
		return schedulerConstructor.newInstance(testClass);	
	}
	
}
