package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a class is annotated with <code>&#064;ChangedFilesSuiteSchedulerParams</code>
 * or extends a class annotated with <code>&#064;ChangedFilesSuiteSchedulerParams</code>,
 * <code>ChangedFilesSuiteScheduler</code> will use the referenced values as its configuration parameters.
 * This annotation can not be used with any other schedulers except the <code>ChangedFilesSuiteScheduler</code>.
 * For example to configure the scheduler use:
 * 
 * <pre>
 * &#064;RunWith(ArquillianSuiteSchduling.class)
 * &#064;ScheduleWith(ChangedFilesSuiteScheduler.class)
 * &#064;ChangedFilesSuiteSchedulerParams(
 * 		workingDir="some class file directory",
 * 		testDir = "some test class directory",
 * 		runOnlyChangedFiles = true
 * )
 * public class ScheduledSuite{
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ChangedFilesSuiteSchedulerParams {
	public static final String DEFAULT_WORKING_DIR = "src/main/java";
	public static final String DEFAULT_TEST_DIR = "src/test/java";
	public static final boolean DEFAULT_RUN_ONLY_CHANGED_FILES_FLAG = false;
	
	String workingDir() default DEFAULT_WORKING_DIR;
	String testDir() default DEFAULT_TEST_DIR;
	/**
	 * Controls whether tests which import unchanged files should execute.
	 * 
	 * @return the flag with which to control the scheduler
	 */
	boolean runOnlyChangedFiles() default DEFAULT_RUN_ONLY_CHANGED_FILES_FLAG;
}
