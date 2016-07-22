package org.jboss.arquillian.junit.scheduler;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.ImportsScanner;
import org.junit.Before;
import org.junit.Test;

public class ImportsScannerTest {
	private static final String TEST_CLASS_1_CONTENT ="package org.jboss.arquillian.junit.scheduler;\n"
			+ "import ChangedClass1;\n"
			+ "public class Test1{\n"
			+ "@Test public void test(){}\n}";
	
	private static final String TEST_CLASS_2_CONTENT ="package org.jboss.arquillian.junit.scheduler;\n"
			+ "import ChangedClass2;\n"
			+ "public class Test2{\n"
			+ "@Test public void test(){}\n}";
	
	private static final String TEST_CLASS_3_CONTENT ="package org.jboss.arquillian.junit.scheduler;\n"
			+ "import SomeClass;\n"
			+ "public class Test3{\n"
			+ "@Test public void test(){}\n}";

	private File resourceDir;
	
	@Before
	public void setUp() throws Exception{
		resourceDir = new File(
				getClass()
				.getClassLoader()
				.getResource("org/jboss/arquillian/junit/scheduler")
				.toURI());
		
		if(!resourceDir.exists()){
			throw new Exception("Resource directory not found!");
		}
		
		File test1File =  File.createTempFile("Test1", ".java", resourceDir);
		FileOutputStream test1Output = new FileOutputStream(test1File);
		
		File test2File =  File.createTempFile("Test2", ".java", resourceDir);
		FileOutputStream test2Output = new FileOutputStream(test2File);
		
		File test3File =  File.createTempFile("Test3", ".java", resourceDir);
		FileOutputStream test3Output = new FileOutputStream(test3File);
		
		try{
			test1Output.write(TEST_CLASS_1_CONTENT.getBytes());
			test2Output.write(TEST_CLASS_2_CONTENT.getBytes());
			test3Output.write(TEST_CLASS_3_CONTENT.getBytes());
		}catch(Exception err){
			throw err;
		}finally{
			if(test1Output != null) test1Output.close();
	
			if(test2Output != null)	test2Output.close();
		
			if(test3Output != null) test3Output.close();
		}
		
		test1File.deleteOnExit();
		test2File.deleteOnExit();
		test3File.deleteOnExit();
	}
	
	@Test
	public void shouldGetImportingClasses() throws Exception {
		ImportsScanner importsScanner = new ImportsScanner(resourceDir.getAbsolutePath());
		Set<String> changedClasses = new HashSet<String>(3);
		changedClasses.add("ChangedClass1");
		changedClasses.add("ChangedClass2");
		changedClasses.add("ChangedClass3");
		
		Set<String> importingClasses = importsScanner.getImportingClasses(changedClasses);
		
		assertNotNull("No imports for this class found!",importingClasses);
		assertEquals("Number of imports do not match!",2,importingClasses.size());
		assertTrue("Expected test class is not contained in the result!",
				importingClasses.contains("org.jboss.arquillian.junit.scheduler.Test1"));
		assertTrue("Expected test class is not contained in the result!",
				importingClasses.contains("org.jboss.arquillian.junit.scheduler.Test2"));	
	}
}
