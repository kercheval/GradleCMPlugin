package org.kercheval.gradle.vcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

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

	private static AtomicInteger currentIndex = new AtomicInteger((int) (Math.random() * 100000));
	private final int testIndex = currentIndex.incrementAndGet();
	private final String ORIGIN_LOCATION = JUNIT_REPOSITORY_LOCATION + "/origin" + testIndex;
	private final String STANDARD_LOCATION = JUNIT_REPOSITORY_LOCATION + "/mainline" + testIndex;

	private final File originFile = new File(ORIGIN_LOCATION);
	private final File standardFile = new File(STANDARD_LOCATION);

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

		//
		// Note that close is not perfect, so some artifacts may remain after this
		// recursive delete. This should normally not be a problem in a clean system
		// since we increment the depot name index and wipe on entry and exit.
		//
		// Comment out this line if you want to validate the test repositories.
		//
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
			FileUtils.deleteDirectory(getOriginFile());
		}
		catch (final Exception e)
		{
			// Ignore errors
		}
		try
		{
			FileUtils.deleteDirectory(getStandardFile());
		}
		catch (final Exception e)
		{
			// Ignore errors
		}
	}

	public File getOriginFile()
	{
		return originFile;
	}

	public Repository getOriginRepo()
	{
		return originRepo;
	}

	public File getStandardFile()
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
		originRepo = new RepositoryBuilder().setWorkTree(getOriginFile()).build();
		originRepo.create();

		//
		// Create an empty file and commit it
		//
		final Git originGit = new Git(originRepo);
		File newFile = new File(getOriginFile().getAbsolutePath() + "/EmptyFile.txt");
		writeRandomContentFile(newFile);
		originGit.add().addFilepattern(".").call();
		originGit.commit().setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
			.setMessage("First commit into origin repository").call();

		//
		// Drop a few test tags at the empty head
		//
		originGit.tag().setName("JUNIT_InitialTag").setMessage("Origin repo initial tag.").call();
		originGit.tag().setName("3.0-2012110101-123456").setMessage("Test version tag").call();

		//
		// Need branches on the origin
		//
		originGit.branchCreate().setName("OriginBranch1").setForce(false).call();
		originGit.branchCreate().setName("OriginBranch2").setForce(false).call();
		originGit.branchCreate().setName("OriginBranch3").setForce(false).call();
		originGit.branchCreate().setName("OriginBranch4").setForce(false).call();

		//
		// Clone the origin
		//
		standardRepo = Git.cloneRepository().setURI(getOriginFile().getCanonicalPath())
			.setDirectory(getStandardFile()).setRemote("myOrigin").call().getRepository();
		final Git standardGit = new Git(standardRepo);
		standardGit.branchCreate().setName("OriginBranch1")
			.setStartPoint("refs/remotes/myOrigin/OriginBranch1").setForce(false).call();
		standardGit.branchCreate().setName("OriginBranch2")
			.setStartPoint("refs/remotes/myOrigin/OriginBranch2").setForce(false).call();
		standardGit.branchCreate().setName("StandardBranch1").setForce(false).call();
		standardGit.branchCreate().setName("StandardBranch2").setForce(false).call();

		//
		// Add another file to the origin
		//
		newFile = new File(getOriginFile().getAbsolutePath() + "/EmptySecondFile.txt");
		writeRandomContentFile(newFile);
		originGit.add().addFilepattern(".").call();
		originGit.commit().setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
			.setMessage("First commit into origin repository").call();
	}

	public void writeRandomContentFile(final File file)
		throws IOException
	{
		final FileOutputStream fileOutputStream = new FileOutputStream(file);

		if (!file.exists())
		{
			file.createNewFile();
		}

		fileOutputStream.write(("FileCreated: " + new Date().getTime() + Math.random()).getBytes());
		fileOutputStream.flush();
		fileOutputStream.close();
	}
}
