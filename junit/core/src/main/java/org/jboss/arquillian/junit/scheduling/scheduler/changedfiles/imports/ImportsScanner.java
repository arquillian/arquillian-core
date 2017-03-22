package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.util.Set;

/**
 * This interface provides the user the ability to
 * find classes which import other classes.
 *
 */
public interface ImportsScanner {
	Set<String> getImportingClasses(Set<String> classNames) throws Exception;
}
