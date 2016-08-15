package org.jboss.arquillian.junit.scheduling.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a class is annotated with <code>&#064;ScheduleWith</code>
 * or extends a class annotated with <code>&#064;ScheduleWith</code>,
 * <code>ArquillianScheduling</code> or <code>ArquillianSuiteSchedulling</code>
 * runner will invoke the class it references and apply
 * its filtering and sorting on tests or respectively on test suites.
 * To schedule a suite you will also need to specify the suite test classes with
 * <code>&#064;SuiteClasses</code> annotation like so:
 * 
 * <pre>
 * &#064;RunWith(ArquillianSuiteSchduling.class)
 * &#064;ScheduleWith(LatestFailedScheduler.class)
 * &#064;SuiteClasses({ATest.class,BTest.class,CTest.class})
 * public class ScheduledSuite{
 * }
 * </pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ScheduleWith {
	/**
	 * 
	 * @return a Scheduler class
	 */
	Class<? extends Scheduler> value();
}
