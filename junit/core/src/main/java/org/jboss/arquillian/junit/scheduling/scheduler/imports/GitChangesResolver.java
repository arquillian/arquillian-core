package org.jboss.arquillian.junit.scheduling.scheduler.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class GitChangesResolver {
	private final static String[] command = {
		"git","ls-files","--modified","--others","--exclude-standard"};
		
	
	public Set<String> getChangedClasses() throws Exception {
		
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		// Preset the current working directory
		processBuilder.directory(new File("src/main/java").getAbsoluteFile());
			
		//Process process = Runtime.getRuntime().exec(command);
		Process process = processBuilder.start();
		
		// Wait for the process to exit properly
		if(0 != process.waitFor()){
			throw new Exception("Process terminated unexpectedly!");
		}
		
		// Handle any errors that may have occurred
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		if(errorReader.ready()){
			StringBuilder builder = new StringBuilder();
			String nextLine;
			while (null != (nextLine = errorReader.readLine())) {
				builder.append(nextLine);
			}
			
			throw new Exception(builder.toString());
		}
		
		// Read the result of the executed command
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String currentLine;
		Set<String> classNames = new HashSet<String>();
		while (null != (currentLine = reader.readLine())) {
			// Parse every java class
			if(currentLine.endsWith(".java")){
				// Removes the file extension and replaces the separator with '.'
				classNames.add(currentLine.substring(0, currentLine.lastIndexOf('.'))
						.replace(File.separatorChar,'.'));
			}
		}
		
		if (classNames.isEmpty()) {
			System.out.println("No changed Java files found!");
			return null;
		}
		
		return classNames;
	}
}
