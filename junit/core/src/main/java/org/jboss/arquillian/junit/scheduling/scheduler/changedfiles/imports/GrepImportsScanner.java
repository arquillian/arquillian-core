package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.utils.ClassFileUtil;
import org.junit.Test;

public class GrepImportsScanner implements ImportsScanner {
	
	private static final String CLASS_IMPORT_REG_EXP = "import\\s+[\\w\\.]+\\s*\\;";
	private static final String CLASS_DECALRATION_REG_EXP = "[\\w\\s]*class\\s\\w\\s*[\\w\\s+]*\\{";
	
	private String workingDir;
	
	public GrepImportsScanner(String workingDir) {
		this.workingDir = workingDir;
	}
	
	/**
	 * Builds a standard class canonical name using a 
	 * standard package name and a class file name.
	 * <p>
	 * Note that any trailing white spaces will be removed.
	 * 
	 * @param builder a <code>StringBuilder</code> to build the canonical name with
	 * @param packageDeclaration the <code>String</code> which represents the package declaration
	 * within the class file
	 * @param className the name of a class file
	 * @return the canonical name built from the <code>packageDeclaration</code> and a <code>className</code>
	 */
	private String buildClassCanonicalName(StringBuilder builder, String packageDeclaration, String className){
		builder.append(packageDeclaration.substring("package".length(),
				packageDeclaration.length() - 1).trim());
		builder.append('.');
		// Removes the file extension
		builder.append(className.substring(0,className.lastIndexOf(".")));
		
		return builder.toString();
	}
	
	/**
	 * Finds all test classes which import a recently changed class file.
	 * <p>
	 * To determine which test classes are eligible this method
	 * scans through their import statement to see if any references to the classes in
	 * <code>classNames</code> are made and also if a <code>&#064;Test</code>
	 * annotation is present. If a <code>&#064;Test</code> annotation is found 
	 * and a class in <code>classNames</code> is imported,
	 * then the test class is eligible and is added to a <code>Set</code>.
	 * Note that the this method stops parsing the class file
	 * after a class declaration statement is read.
	 * 
	 * @param classNames the changed classes's names 
	 * @return the test classes which import the <code>classNames</code>
	 * @throws Exception
	 * @see ClassFileUtil
	 */
	@Override
	public Set<String> getImportingClasses(Set<String> classNames) throws Exception {
		ClassFileUtil classFilesUtil = new ClassFileUtil();
		// Get the only the test classes inside the working directory
		Set<File> classFiles = classFilesUtil.getClassFiles(workingDir);
		
		// Store the tests that import classNames here
		Set<String> importingClasses = new HashSet<String>();
		
		StringBuilder canonicalNameBuilder = new StringBuilder();
		for (File file : classFiles) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Set<String> importsList = new HashSet<String>();
			String currentLine = "";
			String classCanonicalName="";
			
			// Reads the file line by line until a class declaration is read
			while ((currentLine = reader.readLine()) != null) {
				// Trim any unwanted spaces
				currentLine = currentLine.trim();
				
				if (currentLine.startsWith("package")) {
					classCanonicalName = 
							buildClassCanonicalName(canonicalNameBuilder, currentLine, file.getName());
				}
				// Matches an import and stores it
				if (currentLine.matches(CLASS_IMPORT_REG_EXP)) {
					importsList.add(currentLine);
				}

				// Stops the reading
				// All imports have been read
				if (currentLine.matches(CLASS_DECALRATION_REG_EXP)) {
					break;
				}
			}
			reader.close();
			
			// Stores only test classes (which import @Test)
			if(importsList.remove("import " + Test.class.getName() + ";")){
				for(String className : classNames){
					if(importsList.contains("import " + className + ";")){
						importingClasses.add(classCanonicalName);
						break;
					}
				}
			}
			// Clear string builder
			canonicalNameBuilder.delete(0, canonicalNameBuilder.length());
		}
		
		return importingClasses;
	}

}
