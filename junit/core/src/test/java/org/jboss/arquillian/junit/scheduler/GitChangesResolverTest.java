package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.CommandExecutor;
import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.GitChangesResolver;
import org.junit.Test;

public class GitChangesResolverTest {

	public final static String workingDir = "src/main/java";
	private static final String changedFiles = 
			"org/jboss/arquillian/junit/scheduling/scheduler/imports/ImportsScanner.java\n"
			+ "test.xml\n"
			+ "Test.java";
	
	@Test
	public void shouldGetChangedClasses() throws Exception{
		
		CommandExecutor mockedCommandExecitor = mock(CommandExecutor.class);
		when(mockedCommandExecitor.getError()).thenReturn("");
		when(mockedCommandExecitor.getResult())
		.thenReturn(changedFiles);
		
		GitChangesResolver gitChangesResolver = new GitChangesResolver(mockedCommandExecitor);
		
		Set<String> changedClasses = gitChangesResolver.resolveChangedClasses();
		
		assertTrue("No changed classes were found",!changedClasses.isEmpty());
		assertEquals("Unexpected size!",2, changedClasses.size());
		assertTrue("ImortsScanner class is missing",changedClasses.contains("org.jboss.arquillian.junit.scheduling.scheduler.imports.ImportsScanner"));
		assertTrue("test.test class is missing",changedClasses.contains("Test"));
		
	}

}
