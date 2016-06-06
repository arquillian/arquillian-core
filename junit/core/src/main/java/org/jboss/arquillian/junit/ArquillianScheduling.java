package org.jboss.arquillian.junit;

import java.util.Comparator;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runners.model.InitializationError;

/*
 * Scheduling Arquillian JUnit runner
 */

public class ArquillianScheduling extends Arquillian {

	public ArquillianScheduling(Class<?> testClass)throws NoTestsRemainException, InitializationError {
		super(testClass);
		
		// Filtering test methods
		filter(new Filter() {
			@Override
			public boolean shouldRun(Description description) {
				System.out.println("Entered the shouldRunMethod");
				
				if (description.getMethodName().equalsIgnoreCase("test1") ||
						description.getMethodName().contains("test2")) {
					return true;
				}

				return false;
			}

			@Override
			public String describe() {
				return "Runs test1 and test2";
			}
		});
		
		// Sorting tests
		sort(new Sorter(new Comparator<Description>() {
			@Override
			public int compare(Description o1, Description o2) {
				System.out.println("Entered the sorter");
				
				return o1.getMethodName().compareTo(o2.getMethodName());		
			}
			
		}));
	}
	
//	@Override
//	public void run(RunNotifier notifier) {
//		super.run(notifier);
//	}
}
