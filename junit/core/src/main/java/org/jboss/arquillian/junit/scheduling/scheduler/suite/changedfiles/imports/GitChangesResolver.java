package org.jboss.arquillian.junit.scheduling.scheduler.suite.changedfiles.imports;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GitChangesResolver implements ChangesResolver {
	
	private final static String[] command = {
		"git","ls-files","--modified","--others","--exclude-standard"};
	private CommandExecutor commandExecutor;
	
	public GitChangesResolver(String workingDir) {
		commandExecutor = new CommandExecutor(command, workingDir);
	}
	
	public GitChangesResolver(CommandExecutor executor) {
		commandExecutor =  executor;
	}
	
	@Override
	public Set<String> resolveChangedClasses() throws Exception {
		// Execute the command
		commandExecutor.executeCommand();
		
		// Checks for errors during execution
		String error = commandExecutor.getError();
		if(!error.isEmpty()){
			throw new Exception("Git error while resolving changed classes" + error);
		}
		
		Set<String> classNames = new HashSet<String>();
		
		// Read the result of the executed command
		String result = commandExecutor.getResult();
		String[] changedFiles = result.split(System.lineSeparator());
		
		// Parse every java class
		for(String currentFile : changedFiles){
			if(currentFile.endsWith(".java")){
				// Removes the file extension and replaces the separator with '.'
				classNames.add(currentFile.substring(0, currentFile.lastIndexOf('.'))
					.replace(File.separatorChar,'.'));
			}
		}
			
		return classNames;
	}
}
