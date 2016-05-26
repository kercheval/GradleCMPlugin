package org.kercheval.gradle.vcs.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.gradle.api.logging.Logger;
import org.kercheval.gradle.console.TextDevices;
import org.kercheval.gradle.info.SortedProperties;
import org.kercheval.gradle.vcs.VCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSInfoSource;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

//
// This class implements the VCSAccess interface for GIT.
//
public class VCSGitImpl
	extends VCSInfoSource
{
	public VCSGitImpl(final File srcRootDir, final Logger logger)
	{
		super(srcRootDir, logger);
	}

	@Override
	public void createBranch(final String branchName, final String remoteOrigin,
		final boolean ignoreOrigin)
		throws VCSException
	{
		final String refLocalBranch = "refs/heads/" + branchName;
		final String refRemote = "refs/remotes/" + remoteOrigin + "/master";
		final String refRemoteBranch = "refs/remotes/" + remoteOrigin + "/" + branchName;

		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();

			final Git git = new Git(repository);

			final Map<String, Ref> refMap = repository.getAllRefs();
			final boolean localBranchExists = refMap.containsKey(refLocalBranch);
			final boolean remoteExists = refMap.containsKey(refRemote);
			if (ignoreOrigin || !remoteExists)
			{
				if (!localBranchExists)
				{
					//
					// Go ahead and create the local branch
					//
					git.branchCreate().setName(branchName).setForce(false).call();
				}
			}
			else
			{
				final boolean remoteBranchExists = refMap.containsKey(refRemoteBranch);
				boolean doPush = false;
				if (!remoteBranchExists)
				{
					//
					// If the remote does not exist, we will need to push to remote after
					// creation or local branch validation
					//
					doPush = true;
				}

				if (!localBranchExists)
				{
					if (remoteBranchExists)
					{
						//
						// Remote branch does exist and local does not. Create a tracking
						// branch from remote.
						//
						git.branchCreate().setName(branchName).setStartPoint(refRemoteBranch)
							.setForce(false).call();
					}
					else
					{
						//
						// Neither branch exists, create the local branch
						//
						git.branchCreate().setName(branchName).setForce(false).call();
						doPush = true;
					}
				}

				if (doPush)
				{
					//
					// Need to push the local branch back to remote
					//
					git.push()
						.setRemote(remoteOrigin)
						.setRefSpecs(new RefSpec(refLocalBranch))
						.setCredentialsProvider(
							new VCSGitImplCredentialsProvider(TextDevices.defaultTextDevice()))
						.call();
				}
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		catch (final GitAPIException e)
		{
			throw new VCSException("Unable to create branch: " + branchName, e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
	}

	@Override
	public void createTag(final VCSTag tag)
		throws VCSException
	{
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();

			final Git git = new Git(repository);

			try
			{
				git.tag().setName(tag.getName()).setMessage(tag.getComment()).call();
			}
			catch (final GitAPIException e)
			{
				throw new VCSException("Unable to create tag: " + tag.getName(), e);
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
	}

	@Override
	public void fetch(final String remoteOrigin)
		throws VCSException
	{
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();
			new Git(repository)
				.fetch()
				.setRemote(remoteOrigin)
				.setCredentialsProvider(
					new VCSGitImplCredentialsProvider(TextDevices.defaultTextDevice())).call();
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		catch (final GitAPIException e)
		{
			throw new VCSException("Unable to fetch from origin: " + remoteOrigin, e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
	}

	@Override
	public String getBranchName()
		throws VCSException
	{
		String rVal = "";
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();
			rVal = repository.getBranch();
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}

		return rVal;
	}

	@Override
	public String getDescription()
	{
		return "Git (http://git-scm.com/) environment information";
	}

	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = super.getInfo();
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();
			props.addProperty(getPropertyPrefix() + ".basedir", repository.getDirectory()
				.getCanonicalPath());
			props.addProperty(getPropertyPrefix() + ".branch", repository.getBranch());

			final ObjectId head = repository.resolve("HEAD");
			if (null == head)
			{
				props.addProperty(getPropertyPrefix() + ".last.commit", "");
			}
			else
			{
				props.addProperty(getPropertyPrefix() + ".last.commit", head.getName());
			}

			final Config config = repository.getConfig();

			props.addProperty(getPropertyPrefix() + ".user.name",
				config.getString("user", null, "name"));
			props.addProperty(getPropertyPrefix() + ".user.email",
				config.getString("user", null, "email"));
			props.addProperty(getPropertyPrefix() + ".remote.origin",
				config.getString("remote", "origin", "url"));
		}
		catch (final IOException e)
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

	@Override
	public VCSStatus getStatus()
		throws VCSException
	{
		final VCSStatus rVal = new VCSStatus();
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();

			try
			{
				final Status status = new Git(repository).status().call();

				rVal.setAdded(status.getAdded());
				rVal.setChanged(status.getChanged());
				rVal.setMissing(status.getMissing());
				rVal.setRemoved(status.getRemoved());
				rVal.setUntracked(status.getUntracked());
				rVal.setConflicting(status.getConflicting());
				rVal.setModified(status.getModified());
			}
			catch (final GitAPIException e)
			{
				throw new VCSException("Unable to determine repository status", e);
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}

		return rVal;
	}

	@Override
	public List<VCSTag> getTags(final String regexFilter)
		throws VCSException
	{
		final List<VCSTag> rVal = new ArrayList<>();
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();

			final Map<String, Ref> tags = repository.getTags();

			for (final Entry<String, Ref> tag : tags.entrySet())
			{
				if (tag.getKey().matches(regexFilter))
				{
					final Ref ref = tag.getValue();
					final RevWalk revWalk = new RevWalk(repository);

					try
					{
						final RevTag revTag = revWalk.parseTag(ref.getObjectId());

						if (null != revTag)
						{
							final PersonIdent ident = revTag.getTaggerIdent();

							if (null != ident)
							{
								rVal.add(new VCSTag(revTag.getTagName(), revTag.getName(), revTag
									.getFullMessage(), ident.getName(), ident.getEmailAddress(),
									ident.getWhen()));
							}
						}
					}
					finally
					{
					    revWalk.dispose();
					}
				}
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}

		return rVal;
	}

	@Override
	public Type getType()
	{
		return VCSAccess.Type.GIT;
	}

	@Override
	public void merge(final String fromBranch, final String remoteOrigin,
		final boolean fastForwardOnly)
		throws VCSException
	{

		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();
			final Git git = new Git(repository);
			final MergeResult mergeResult;
			String refBranchName = "refs/heads/" + fromBranch;
			if (null != remoteOrigin)
			{
				refBranchName = remoteOrigin + "/" + fromBranch;
			}

			final Ref refBranch = repository.getRef(refBranchName);
			if (null == refBranch)
			{
				throw new VCSException("Unable to merge branch: " + refBranchName,
					new IllegalStateException("Branch does not exist"));
			}
			FastForwardMode ffMode = FastForwardMode.FF;
			if (fastForwardOnly)
			{
				ffMode = FastForwardMode.FF_ONLY;
			}
			mergeResult = git.merge().setFastForward(ffMode).include(refBranch).call();

			if (!mergeResult.getMergeStatus().isSuccessful())
			{
				//
				// Need to bail on merge failure.
				//
				git.reset().setMode(ResetType.HARD).setRef(repository.getFullBranch()).call();
				throw new VCSException("Unable to merge branch: " + mergeResult.getMergeStatus(),
					new IllegalStateException(
						"The branch must be merged or manually corrected before continuing due to collision or non-fast forward merge"));
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to find repository at: " + getSrcRootDir(), e);
		}
		catch (final GitAPIException e)
		{
			throw new VCSException("Unable to merge branch: " + fromBranch, e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
	}

	@Override
	public void push(final String from, final String remoteOrigin, final boolean pushTag)
		throws VCSException
	{
		String refLocalBranch = "refs/heads/" + from;
		if (pushTag)
		{
			refLocalBranch = "refs/tags/" + from;
		}
		Repository repository = null;

		try
		{
			repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir())
				.build();

			Iterable<PushResult> pushResult;
			pushResult = new Git(repository)
				.push()
				.setRemote(remoteOrigin)
				.setRefSpecs(new RefSpec(refLocalBranch))
				.setCredentialsProvider(
					new VCSGitImplCredentialsProvider(TextDevices.defaultTextDevice())).call();

			RemoteRefUpdate.Status refStatus = null;
			for (final PushResult result : pushResult)
			{
				refStatus = result.getRemoteUpdate(refLocalBranch).getStatus();
				if (refStatus != null)
				{
					break;
				}
			}
			if (null == refStatus)
			{
				throw new VCSException("Unable to push branch with reason: unknown",
					new IllegalStateException(
						"The branch must be merged or manually corrected before continuing"));
			}
			switch (refStatus)
			{
			case OK:
			case UP_TO_DATE:
				// Success, do nothing
				break;

			default:
				throw new VCSException("Unable to push branch with reason: " + refStatus,
					new IllegalStateException(
						"The branch must be merged or manually corrected before continuing"));

			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to push repository at: " + getSrcRootDir(), e);
		}
		catch (final GitAPIException e)
		{
			throw new VCSException("Unable to push branch " + from + " to " + remoteOrigin, e);
		}
		finally
		{
			if (null != repository)
			{
				repository.close();
			}
		}
	}
}
