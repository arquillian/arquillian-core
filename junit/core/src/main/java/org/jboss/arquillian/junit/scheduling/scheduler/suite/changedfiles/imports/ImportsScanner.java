package org.jboss.arquillian.junit.scheduling.scheduler.suite.changedfiles.imports;

import java.util.Set;

public interface ImportsScanner {
	Set<String> getImportingClasses(Set<String> classNames) throws Exception;
}
