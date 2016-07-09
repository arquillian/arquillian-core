package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;

import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.imports.GitChangesResolver;
import org.junit.Test;

public class GitChangesResolverTest {

	@Test
	public void shouldGetChangedClasses() throws Exception{
		GitChangesResolver classFileUtil = new GitChangesResolver();
		
		Set<String> changedClasses = classFileUtil.getChangedClasses();
		
		assertNotNull(changedClasses);
		assertEquals("Unexpected size!",3, changedClasses.size());
	}

}
