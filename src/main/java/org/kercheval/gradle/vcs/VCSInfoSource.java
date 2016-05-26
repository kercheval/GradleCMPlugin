package org.kercheval.gradle.vcs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.gradle.api.logging.Logger;
import org.kercheval.gradle.info.SortedProperties;

public abstract class VCSInfoSource
	implements VCSAccess
{
	private static final String VCS_TYPE_PREFIX = "vcs";
	private final File srcRootDir;
	private final Logger logger;

	public VCSInfoSource(final File srcRootDir, final Logger logger)
	{
		this.srcRootDir = srcRootDir;
		this.logger = logger;
	}

	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = new SortedProperties();
		Repository repository = null;
		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();
			final Status status = new Git(repository).status().call();

			props.addProperty(VCS_TYPE_PREFIX + ".type", getType().toString());

			props.addProperty(getPropertyPrefix() + ".workspace.clean",
				Boolean.toString(status.isClean()));
			props.addProperty(getPropertyPrefix() + ".workspace.files.added", status.getAdded()
				.toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.changed", status.getChanged()
				.toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.missing", status.getMissing()
				.toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.removed", status.getRemoved()
				.toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.untracked", status
				.getUntracked().toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.conflicting", status
				.getConflicting().toString());
			props.addProperty(getPropertyPrefix() + ".workspace.files.modified", status
				.getModified().toString());
		}
		catch (final NoWorkTreeException|GitAPIException|IOException e)
		{
			// Ignore
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
		return props;
	}

	public Logger getLogger()
	{
		return logger;
	}

	@Override
	public String getPropertyPrefix()
	{
		return VCS_TYPE_PREFIX + "." + getType().toString();
	}

	public File getSrcRootDir()
	{
		return srcRootDir;
	}

	@Override
	public boolean isActive()
	{
		boolean rVal = false;
		try
		{
			getStatus();
			rVal = true;
		}
		catch (final VCSException e)
		{
			// Ignore exception
		}
		return rVal;
	}

	@Override
	public List<VCSTag> getAllTags()
		throws VCSException
	{
		return getTags(".*");
	}
}
