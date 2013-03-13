package org.kercheval.gradle.vcs.none;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;
import org.kercheval.gradle.info.SortedProperties;
import org.kercheval.gradle.vcs.VCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSInfoSource;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

public class VCSNoneImpl
	extends VCSInfoSource
{
	public VCSNoneImpl(final File srcRootDir, final Logger logger)
	{
		super(srcRootDir, logger);
	}

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
	public String getBranchName()
		throws VCSException
	{
		throw new VCSException("Unable to get branch name from workspace",
			new IllegalStateException("No VCS associated with this build"));
	}

	@Override
	public String getDescription()
	{
		return "No revision control system";
	}

	@Override
	public SortedProperties getInfo()
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
		return VCSAccess.Type.NONE;
	}

	@Override
	public boolean isActive()
	{
		return false;
	}

	@Override
	public void merge(final String fromBranch, final String remoteOrigin,
		final boolean fastForwardOnly)
		throws VCSException
	{
		throw new VCSException("Unable to merge from " + fromBranch, new IllegalStateException(
			"No VCS associated with this build"));

	}

	@Override
	public void push(final String from, final String remoteOrigin, final boolean pushTag)
		throws VCSException
	{
		throw new VCSException("Unable to push branch " + from + " to " + remoteOrigin,
			new IllegalStateException("No VCS associated with this build"));
	}

}
