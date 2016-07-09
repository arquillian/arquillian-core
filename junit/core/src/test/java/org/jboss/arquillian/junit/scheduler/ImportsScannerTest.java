package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;

import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.imports.GitChangesResolver;
import org.jboss.arquillian.junit.scheduling.scheduler.imports.ImportsScanner;
import org.junit.Test;

public class ImportsScannerTest {

	@Test
	public void shouldGetImportingClasses() throws Exception {
		ImportsScanner importsScanner = new ImportsScanner();
		
		GitChangesResolver classFileUtil = new GitChangesResolver();
		Set<String> importingClasses = importsScanner.getImportingClasses(classFileUtil.getChangedClasses());
		
		assertNotNull("No imports for this class found!",importingClasses);
		assertEquals("Number of imports do not match!",2,importingClasses.size());
		
		for (String importingClass : importingClasses) {
			System.out.println(importingClass);
		}		
	}
}
