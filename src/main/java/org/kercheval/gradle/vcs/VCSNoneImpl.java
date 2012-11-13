package org.kercheval.gradle.vcs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;
import org.kercheval.gradle.util.SortedProperties;

public class VCSNoneImpl
	implements IVCSAccess
{
	public VCSNoneImpl(final File srcRootDir, final Logger logger)
	{}

	@Override
	public void createBranch(final String branchName, final String remoteOrigin,
		final boolean ignoreOrigin)
		throws VCSException
	{
		throw new VCSException("Unable to create branch " + branchName, new IllegalStateException(
			"No VCS associated with this build"));
	}

	@Override
	public void createTag(final VCSTag tag)
		throws VCSException
	{
		throw new VCSException("Unable to create tag " + tag, new IllegalStateException(
			"No VCS associated with this build"));
	}

	@Override
	public void fetch(final String remoteOrigin)
		throws VCSException
	{
		throw new VCSException("Unable to fetch from origin: " + remoteOrigin,
			new IllegalStateException("No VCS associated with this build"));
	}

	@Override
	public List<VCSTag> getAllTags()
		throws VCSException
	{
		return new ArrayList<VCSTag>();
	}

	@Override
	public String getBranchName()
		throws VCSException
	{
		throw new VCSException("Unable to get branch name from workspace",
			new IllegalStateException("No VCS associated with this build"));
	}

	@Override
	public SortedProperties getInfo()
		throws VCSException
	{
		return new SortedProperties();
	}

	@Override
	public VCSStatus getStatus()
		throws VCSException
	{
		return new VCSStatus();
	}

	@Override
	public List<VCSTag> getTags(final String regexFilter)
		throws VCSException
	{
		return new ArrayList<VCSTag>();
	}

	@Override
	public Type getType()
	{
		return IVCSAccess.Type.NONE;
	}

	@Override
	public void mergeBranch(final String fromBranch, String remoteOrigin)
		throws VCSException
	{
		throw new VCSException("Unable to merge from " + fromBranch, new IllegalStateException(
			"No VCS associated with this build"));

	}

	@Override
	public void pushBranch(final String fromBranch, final String remoteOrigin,
		final boolean pushTags)
		throws VCSException
	{
		throw new VCSException("Unable to push branch " + fromBranch + " to " + remoteOrigin,
			new IllegalStateException("No VCS associated with this build"));
	}

}
