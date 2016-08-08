package org.jboss.arquillian.junit.scheduling.scheduler.changedfiles.imports.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class ClassFileUtil {

	// Recursively add all files to set
	private void addClassFilesToSetRecursively(File[] listedFiles, Set<File> classFiles)
			throws FileNotFoundException {
		
		if (listedFiles.length == 0) {
			return;
		}
		
		for (File file : listedFiles) {
			if (!file.isDirectory()) {
				if (file.getName().endsWith(".java")) {
					classFiles.add(file);
				}
			} else {
				addClassFilesToSetRecursively(file.listFiles(), classFiles);
			}
		}

		return;
	}

	public Set<File> getClassFiles(String workingDir) throws Exception {
		File workingDirFile = new File(workingDir).getAbsoluteFile();
		if (!workingDirFile.isDirectory()) {
			throw new Exception("The specified path: " + workingDir + "does not denote a directory");
		}

		File[] listedFiles = workingDirFile.listFiles();
		Set<File> classFiles = new HashSet<File>();
		addClassFilesToSetRecursively(listedFiles, classFiles);

		return classFiles;
	}
}
