package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.util.Set;

/**
 * Provides the user the ability to implement his own changes resolving
 * mechanism.
 */
public interface ChangesResolver {
	Set<String> resolveChangedClasses() throws Exception;
}
