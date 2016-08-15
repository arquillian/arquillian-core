package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a class is annotated with <code>&#064;LatestFailedSchedulerParams</code>
 * or extends a class annotated with <code>&#064;LatestFailedSchedulerParams</code>,
 * <code>LatestFailedScheduler</code> will use the referenced values as its configuration parameters.
 * This annotation can not be used with any other schedulers except the <code>LatestFailedScheduler</code>.
 * For example to configure the scheduler use:
 * 
 * <pre>
 * &#064;RunWith(ArquillianSchduling.class)
 * &#064;ScheduleWith(LatestFailedScheduler.class)
 * &#064;LatestFailedScheedulerParams(storeLongTerm=false,storagePath="output/output.xml")
 * public class ScheduledTest{
 * }
 * </pre>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface LatestFailedSchedulerParams {
	public static final boolean STORE_LONG_TERM_DEFAULT_VALUE = true;
	public static final String STORAGE_PATH_DEFAULT_VALUE = "statistics.xml";
	
	boolean storeLongTerm() default STORE_LONG_TERM_DEFAULT_VALUE;
	String storagePath() default STORAGE_PATH_DEFAULT_VALUE;
}
