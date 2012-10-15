package org.jbk.gradle.about;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class JGitUtil {

	public String getStatus() {
		String rVal = "Unknown";
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository;
		try {
			File repoDir = new File(".");
			System.out.println(repoDir.getCanonicalPath());
			repository = builder.setGitDir(new File("."))
			  .readEnvironment() // scan environment GIT_* variables
			  .findGitDir() // scan up the file system tree
			  .build();
			rVal = repository.getBranch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rVal;
	}
	
	
}
