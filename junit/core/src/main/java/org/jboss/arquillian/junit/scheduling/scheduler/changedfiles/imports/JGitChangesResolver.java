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
	
	/**
	 * Constructs a <code>Git</code> object using the root of the local git repository
	 * specified by the system property <code>user.dir</code> and sets this class's
	 * <code>workingDir</code> property.
	 * 
	 * @param workingDir the working directory of the git command
	 * @throws Exception
	 */
	public JGitChangesResolver(String workingDir) throws Exception {
		git = Git.open(new File(System.getProperty("user.dir")));
		this.workingDir = workingDir;
	}

	/**
	 * Finds all the changed files in the specified <code>workingDir</code> using JGit.
	 * <p>
	 * Changed files are considered to be all the files 
	 * that are tracked but changed either in the index or in the working tree
	 * and all the untracked files that are not ignored.
	 * 
	 * @return the changed files in <code>workingDir</code>
	 * @throws Exception
	 */
	private Set<String> getChangedFiles() throws Exception {
		try{
			StatusCommand status = git.status()
					.addPath(workingDir);
	
			Status commandStatus = status.call();
	
			// Add the untracked files
			Set<String> changedFiles = new HashSet<String>();
			changedFiles.addAll(commandStatus.getUntracked());
	
			if (commandStatus.hasUncommittedChanges()) {
				changedFiles.addAll(commandStatus.getUncommittedChanges());
			}
	
			git.close();

			return changedFiles;
		}catch(Exception err){
			throw new Exception("Error while getting changed files from: "+ workingDir,err);
		}
	}

	/**
	 * Resolves the changed class files in <code>workingDir</code>.
	 * <p>
	 * Every changed file name is parsed and if it represents
	 * a java class file it is converted to a canonical class name.
	 * 
	 * @return a set with the canonical names of all the changed classes
	 * @throws Exception
	 * @see #getChangedFiles
	 */
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
