package org.jboss.arquillian.junit.scheduling.scheduler.suite.changedfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ChangedFilesSuiteSchedulerParams {
	public static final String DEFAULT_WORKING_DIR = "src/main/java";
	public static final String DEFAULT_TEST_DIR = "src/test/java";
	public static final boolean DEFAULT_RUN_ONLY_CHANGED_FILES_FLAG = false;
	
	String workingDir() default DEFAULT_WORKING_DIR;
	String testDir() default DEFAULT_TEST_DIR;
	boolean runOnlyChangedFiles() default DEFAULT_RUN_ONLY_CHANGED_FILES_FLAG;
}
