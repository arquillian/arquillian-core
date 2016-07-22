package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaSource;

public class ImportsScanner {
	private JavaProjectBuilder builder;

	public ImportsScanner(String targetDir) throws Exception {
		builder = new JavaProjectBuilder();
		builder.addSourceTree(new File(targetDir).getAbsoluteFile());
	}
	
	public Set<String> getImportingClasses(Set<String> classNames) throws Exception{
				
		Set<String> importingClassNames = new HashSet<String>();
		
		// Skip iteration over sources
		if (classNames.isEmpty()) {
			return importingClassNames;
		}
		
		// Get all the source objects from the builder
		Collection<JavaSource> sources = builder.getSources();
		
		for(JavaSource source : sources){
			List<String> currentSourceImports = source.getImports();
	
			// If the current source objects import the specified class
			// And have at least one test method
			// Then the source object's class name is stored
			for(String className : classNames){
				
				if(currentSourceImports.contains(className)){
					// Get the primary class in the source
					JavaClass importingClass = source.getClasses().get(0);
					String importingClassName = importingClass.getCanonicalName();
					if(importingClassName == null){
						throw new Exception("Class has no canonical name!");
					}
					
					// If a class was already added in the set
					// There is no need to check if the other provided class names were
					// Imported by the same class
					if(!importingClassNames.isEmpty() && importingClassNames.contains(importingClassName)){
						break;
					}
					
					// Checks if the classe's methods have a @Test annotation
					// Otherwise the class won't be recorded
					for(JavaMethod method : importingClass.getMethods()){
						List<JavaAnnotation> annotations = method.getAnnotations();
						if(annotations.isEmpty()){
							continue;
						}
						
						boolean methodHasTestAnnotation = false;
						for(JavaAnnotation annotation: annotations){
							if(annotation.getType().getName().equals("Test")){
								methodHasTestAnnotation = true;
								// Stop the annotation search if a @Test annotation is found
								break;
							}
						}
						
						// Record only test classes
						if(methodHasTestAnnotation){
							importingClassNames.add(importingClassName);
							// Stop the method search if at least one method is annotated
							break;
						}
					}
				}
			}
		}
		
		return importingClassNames;
	}
	
}
