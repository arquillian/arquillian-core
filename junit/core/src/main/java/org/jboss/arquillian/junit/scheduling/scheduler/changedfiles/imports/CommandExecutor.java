package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.io.File;

import org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.utils.StreamReaderUtil;

public class CommandExecutor {

	private ProcessBuilder processBuilder;
	private Process process;
	private StreamReaderUtil streamReaderUtil;
	private boolean processHasStarted;
	private boolean terminatedNoramally;
	
	public CommandExecutor(String[] command, String workingDir) {
		processHasStarted = false;
		processBuilder = new ProcessBuilder(command);
		// Preset the current working directory
		processBuilder.directory(new File(workingDir).getAbsoluteFile());
	}

	public int executeCommand() throws Exception {
		process = processBuilder.start();
		processHasStarted = true;

		// Wait for the process to exit properly
		int exitValue = process.waitFor();
		if(0 == exitValue){
			terminatedNoramally = true;
		}else{
			terminatedNoramally = false;
		}
		streamReaderUtil = new StreamReaderUtil();
		
		return exitValue;
	}
	
	// Returns an empty string if no errors have occurred
	public String getError() throws Exception{
		if (!processHasStarted) {
			throw new Exception("Process has not been started yet! Try calling the executeCommand() method.");
		}
		
		if(terminatedNoramally){
			return new String();
		}
		
		// Add the exitValue of the process
		StringBuilder builder = new StringBuilder();
		builder.append("Process terminated with exit value: ");
		builder.append(process.exitValue());
		
		// Add the error message if such exists
		String result = streamReaderUtil.getContentAsString(process.getErrorStream());
		if(!result.isEmpty()){	
			builder.append("\nError message: ");
			builder.append(result);
		}
		
		return builder.toString();
	}
	
	// Returns an empty string if the process did not terminate normally 
	// Or if there is no standard output from the command
	// If the process did not terminate normally check the getError() result
	public String getResult() throws Exception{
		if (!processHasStarted) {
			throw new Exception("Process has not been started yet! Try calling the executeCommand() method.");
		}
		
		if(!terminatedNoramally){
			return new String();
		}
		
		// Read the process's standard output and return the result
		return streamReaderUtil.getContentAsString(process.getInputStream());
	}
}
