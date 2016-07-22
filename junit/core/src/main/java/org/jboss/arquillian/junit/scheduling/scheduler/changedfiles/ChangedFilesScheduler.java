package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles;

import java.util.Comparator;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.Scheduler;
import org.jboss.arquillian.junit.scheduling.scheduler.SchedulerListener;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.ImportsScanner;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

public class ChangedFilesScheduler implements Scheduler{
	private static final String FILTER_DESCRIPTION = "Filters only tests which are touched by changed files";
	
	private ChangedFilesSchedulerParamValues runtimeParams;
	private Set<String> changedClasses;
	private Set<String> importingTests;
	private boolean runFlagIsSet;
	
	public ChangedFilesScheduler(Class<?> testClass) throws Exception{
		ChangedFilesSchedulerParams annotation =
				testClass.getAnnotation(ChangedFilesSchedulerParams.class);
		
		// Set up params if any
		if(annotation != null){
			runtimeParams = new ChangedFilesSchedulerParamValues(
					annotation.workingDir()
					,annotation.testDir()
					,annotation.runOnlyChangedFiles());
		}else{
			runtimeParams = new ChangedFilesSchedulerParamValues(
					ChangedFilesSchedulerParams.DEFAULT_WORKING_DIR
					,ChangedFilesSchedulerParams.DEFAULT_TEST_DIR
					,ChangedFilesSchedulerParams.DEFAULT_RUN_ONLY_CHANGED_FILES_FLAG);
		}
		
		changedClasses = new ChangedFilesBuilder(runtimeParams.getWorkingDir()).build();

		importingTests = new ImportsScanner(runtimeParams.getTestDir())
				.getImportingClasses(changedClasses);
		
		runFlagIsSet = runtimeParams.getFilterFlag();
	}
	
	@Override
	public Filter getFilter() {
		// Check the filter flag (from the annotation params)
		if(runFlagIsSet){
			return new Filter() {
				@Override
				public boolean shouldRun(Description description) {
					if(importingTests.contains(description.getClassName())){
						return true;
					}
					return false;
				}
				
				@Override
				public String describe() {
					return FILTER_DESCRIPTION;
				}
			};
		}
		
		return Filter.ALL;
	}

	@Override
	public Sorter getSorter() {	
		// If the filter flag (from the annotation params) is set to true
		// There is no need to sort the tests
		if(runFlagIsSet){
			return Sorter.NULL;
		}
		
		return new Sorter(new ChangedFilesComparator());
	}

	@Override
	public SchedulerListener getSchedulerListener() {
		return new SchedulerListener() {
			
			@Override
			public void testStarted(String className, String testName) throws Exception {}
			
			@Override
			public void testRunStarted() throws Exception {}
			
			@Override
			public void testRunFinished() throws Exception {}
			
			@Override
			public void testFailure(String className, String testName, String reason) throws Exception {}
		};
	}
	
	private class ChangedFilesComparator implements Comparator<Description>{
		
		@Override
		public int compare(Description o1, Description o2) {
				
			// If there are no changed files or no importing test classes
			// No sorting is required
			if(changedClasses.isEmpty() || importingTests.isEmpty()){
				return 0;
			}
				
			boolean o1IsContained = importingTests.contains(o1.getClassName());
			boolean o2IsContained = importingTests.contains(o2.getClassName());
				
			// Tests both contained or not are considered equal
			if((o1IsContained && o2IsContained)	|| (!o1IsContained && !o2IsContained)){					
				return 0;
			}
			
			// Sort the contained tests upwards and the others downwards
			if(o1IsContained){
				return 1;
			}
			
			return -1;
		}
		
	}

}
