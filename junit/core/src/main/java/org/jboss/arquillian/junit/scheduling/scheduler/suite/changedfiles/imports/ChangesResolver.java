package org.jboss.arquillian.junit.scheduling.scheduler.suite.changedfiles.imports;

import java.util.Set;

public interface ChangesResolver {
	Set<String> resolveChangedClasses() throws Exception;
}
