package org.jboss.arquillian.junit.scheduling.scheduler.suite.latestfailed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface LatestFailedSuiteSchedulerParams {
	public static final boolean STORE_LONG_TERM_DEFAULT_VALUE = true;
	public static final String STORAGE_PATH_DEFAULT_VALUE = "statistics.xml";
	
	boolean storeLongTerm() default STORE_LONG_TERM_DEFAULT_VALUE;
	String storagePath() default STORAGE_PATH_DEFAULT_VALUE;
}
