package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;

public class JGitChangesResolver implements ChangesResolver {

	private Git git;
	private String workingDir;
	
	public JGitChangesResolver(Git git){
		this.git = git;
	}
	
	public JGitChangesResolver(String workingDir) throws Exception {
		git = Git.open(new File(System.getProperty("user.dir")));
		this.workingDir = workingDir;
	}

	private Set<String> getChangedFiles() throws Exception {
		try{
			StatusCommand status = git.status()
					.addPath(workingDir);
	
			Status commandStatus = status.call();
	
			// Add the untracked files
			Set<String> changedFiles = new HashSet<String>();
			changedFiles.addAll(commandStatus.getUntracked());
	
			// Add the know changed files for the repository
			if (commandStatus.hasUncommittedChanges()) {
				changedFiles.addAll(commandStatus.getUncommittedChanges());
			}
	
			git.close();

			return changedFiles;
		}catch(Exception err){
			throw new Exception("Error while getting changed files from: "+ workingDir,err);
		}
	}

	@Override
	public Set<String> resolveChangedClasses() throws Exception {
		Set<String> changedFiles = getChangedFiles();
		Set<String> classNames = new HashSet<String>();

		// Parse every java class
		for (String currentFile : changedFiles) {
			if (currentFile.endsWith(".java")) {
				if(currentFile.startsWith(workingDir)){
					// Remove the working dir from the file path
					currentFile = currentFile.substring(workingDir.length()+1);
				}
				// Remove the file extension and replaces the separator with '.'
				classNames.add(currentFile.substring(0, currentFile.lastIndexOf('.'))
						.replace(File.separatorChar, '.'));
			}
		}

		return classNames;
	}

}
