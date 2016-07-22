package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.util.Set;

public interface ChangesResolver {
	Set<String> resolveChangedClasses() throws Exception;
}
