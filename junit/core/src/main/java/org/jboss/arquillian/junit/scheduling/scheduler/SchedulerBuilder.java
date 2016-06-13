package org.jboss.arquillian.junit.scheduling.scheduler;

import java.lang.reflect.Constructor;

import org.jboss.arquillian.junit.scheduling.Statistics;
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
	};
	
	public static Scheduler buildScheduler(Class<? extends Scheduler> schedulerClass
			, Statistics statistics) throws Exception {
		
		Constructor<? extends Scheduler> schedulerConstructor =
				schedulerClass.getConstructor(Statistics.class);
		
		return schedulerConstructor.newInstance(statistics);	
	}
	
}
