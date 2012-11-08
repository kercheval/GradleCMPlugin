package org.kercheval.gradle.vcs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class JGitTestRepository
{

	public static final String JUNIT_REPOSITORY_LOCATION = "./build/junit_temp/vcs";

	private static final String ORIGIN_LOCATION = JUNIT_REPOSITORY_LOCATION + "/origin";
	private static final String STANDARD_LOCATION = JUNIT_REPOSITORY_LOCATION + "/mainline";

	private static final File originFile = new File(ORIGIN_LOCATION);
	private static final File standardFile = new File(STANDARD_LOCATION);

	private Repository originRepo = null;
	private Repository standardRepo = null;

	public JGitTestRepository()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		deleteTemporaryDirectories();
		initRepositories();
	}

	public void close()
	{
		closeRepositories();
		deleteTemporaryDirectories();
	}

	private void closeRepositories()
	{
		if (null != getOriginRepo())
		{
			getOriginRepo().close();
		}
		if (null != getStandardRepo())
		{
			getStandardRepo().close();
		}
	}

	private void deleteTemporaryDirectories()
	{

		try
		{
			FileUtils.deleteDirectory(new File(ORIGIN_LOCATION));
		}
		catch (final Exception e)
		{
			// Ignore errors
		}
		try
		{
			FileUtils.deleteDirectory(new File(STANDARD_LOCATION));
		}
		catch (final Exception e)
		{
			// Ignore errors
		}
	}

	public File getOriginfile()
	{
		return originFile;
	}

	public Repository getOriginRepo()
	{
		return originRepo;
	}

	public File getStandardfile()
	{
		return standardFile;
	}

	public Repository getStandardRepo()
	{
		return standardRepo;
	}

	private void initRepositories()
		throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		deleteTemporaryDirectories();

		//
		// Create the origin
		//
		final Repository originRepository = new RepositoryBuilder().setWorkTree(getOriginfile())
			.build();
		originRepository.create();
		originRepo = originRepository;

		//
		// Create an empty file and commit it
		//
		final Git originGit = new Git(originRepository);
		final File newFile = new File(getOriginfile().getAbsolutePath() + "/EmptyFile.txt");
		newFile.createNewFile();
		System.out.println(newFile);
		originGit.add().addFilepattern(".").call();
		originGit.commit().setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
			.setMessage("First commit into origin repository").call();

		//
		// Drop a few test tags at the empty head
		//
		originGit.tag().setName("JUNIT_InitialTag").setMessage("Origin repo initial tag.").call();
		originGit.tag().setName("3.0-2012110101-123456").setMessage("Test version tag").call();

		//
		// Create a file for history in the origin
		//

		//
		// Clone the origin
		//
		standardRepo = Git.cloneRepository().setURI(getOriginfile().getCanonicalPath())
			.setDirectory(getStandardfile()).call().getRepository();
// final Repository repository = new RepositoryBuilder().readEnvironment()
// .findGitDir(getSrcRootDir()).build();
//
// final Git git = new Git(repository);
// git.branchCreate().setName(branchName).setForce(false).call();
	}

}
