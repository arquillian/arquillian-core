package org.jboss.arquillian.junit.scheduling.scheduler;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public interface Scheduler {
	public Filter getFilter();
	public Sorter getSorter();
}
