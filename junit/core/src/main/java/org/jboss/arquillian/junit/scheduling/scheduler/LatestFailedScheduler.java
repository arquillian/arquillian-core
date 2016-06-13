package org.jboss.arquillian.junit.scheduling.scheduler;

import java.util.Comparator;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.TestStatus;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public class LatestFailedScheduler implements Scheduler {
	private Statistics statistics;

	public LatestFailedScheduler(Statistics statistics) {
		this.statistics = statistics;
	}

	@Override
	public Filter getFilter() {
		return Filter.ALL;
	}

	@Override
	public Sorter getSorter() {
		return new Sorter(new LatestFailedComparator());
	}

	private class LatestFailedComparator implements Comparator<Description> {
		@Override
		public int compare(Description o1, Description o2) {
			TestStatus o1TestStatus;
			TestStatus o2TestStatus;

			// Gets the failures and passes of the specified test
			o1TestStatus = statistics.getTestStatus(o1);
			o2TestStatus = statistics.getTestStatus(o2);

			if (o1TestStatus == null || o2TestStatus == null) {
				return 0;
			}

			int o1FailToPassFactor = o1TestStatus.getFailures() - o1TestStatus.getPasses();
			int o2FailToPassFactor = o2TestStatus.getFailures() - o2TestStatus.getPasses();

			// Tests with more failures and less passes will be run first
			return o2FailToPassFactor - o1FailToPassFactor;
		}
	}
}
