package org.kercheval.gradle.vcs;

import java.util.List;

import org.kercheval.gradle.util.SortedProperties;

//
// This interface supports the specific types of operation required by
// programmatic access to the VCS system in use.
//
// NOTE: GIT is the only supported system at the moment.
//
public interface IVCSAccess
{
	//
	// This type refers to the VCS type in use for an access object.
	// Currently only GIT is implemented, but Mercurial, Perforce and
	// SVN are all likely candidates.
	//
	public enum Type
	{
		GIT
	}

	//
	// Create a new branch in the current system. If ignoreOrigin is true
	// the branch will be created/verified on the local repository. If
	// ignoreOrigin is false, the branch will be first checked on the origin
	// and pulled if present on the origin and not on the current local. If
	// a local branch is created because the origin is not present, it will
	// be pushed to origin.
	//
	public void createBranch(final String branchName, final String remoteOrigin,
		final boolean ignoreOrigin)
		throws VCSException;

	//
	// Write a tag into the repository
	//
	public void createTag(final VCSTag tag)
		throws VCSException;

	//
	// Obtain from the branch and origin the current content for that branch.
	//
	public void fetchBranch(final String branchName, final String remoteOrigin)
		throws VCSException;

	//
	// Get tags from repository.
	//
	public List<VCSTag> getAllTags()
		throws VCSException;

	//
	// Obtain the current branch name
	//
	public String getBranchName()
		throws VCSException;

	//
	// Obtain 'interesting' information about the current VCS usage
	// and return that as property information.
	//
	public SortedProperties getInfo()
		throws VCSException;

	//
	// Return the current status of the VCS system (all workspace changes)
	//
	public VCSStatus getStatus()
		throws VCSException;

	public List<VCSTag> getTags(final String regexFilter)
		throws VCSException;

	//
	// Get the current VCSType.
	//
	public Type getType();

	//
	// Merge one branch into another. This will fail if there are
	// any conflicts or merge changes required. To succeed the branch
	// update must be the equivalent of a git fast-forward merge.
	// Failure will throw a VCSException.
	//
	public void mergeBranch(final String fromBranch)
		throws VCSException;

	//
	// Push the a branch back into the origin. Push failures will result in
	// an exception being thrown.
	//
	public void pushBranch(final String fromBranch, final String remoteOrigin)
		throws VCSException;
}
