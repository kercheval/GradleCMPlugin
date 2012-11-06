package org.kercheval.gradle.vcs;

import java.util.Date;

public class VCSTag
{
	private final String name;
	private final String comment;
	private final String commit;
	private final String commitName;
	private final String commitEmail;
	private final Date commitDate;

	//
	// Used when creating a tag in VCS
	//
	public VCSTag(final String name, final String comment)
	{
		this.name = name;
		this.comment = comment;
		commit = null;
		commitName = null;
		commitEmail = null;
		commitDate = null;
	}

	//
	// Used when obtaining a tag from VCS
	//
	public VCSTag(final String name,
		final String commit,
		final String comment,
		final String commitName,
		final String commitEmail,
		final Date creationDate)
	{
		this.name = name;
		this.comment = comment;
		this.commit = commit;
		this.commitName = commitName;
		this.commitEmail = commitEmail;
		this.commitDate = creationDate;
	}

	public String getComment()
	{
		return comment;
	}

	public String getCommitName()
	{
		return commitName;
	}

	public String getCommitEmail()
	{
		return commitEmail;
	}

	public String getName()
	{
		return name;
	}

	public String getCommit()
	{
		return commit;
	}

	public Date getCommitDate()
	{
		return commitDate;
	}

	@Override
	public String toString()
	{
		final StringBuilder buildStr = new StringBuilder("[");

		buildStr.append(getName());
		buildStr.append(", ");
		buildStr.append(getCommit());
		buildStr.append(", ");
		buildStr.append(getComment());
		buildStr.append(", ");
		buildStr.append(getCommitName());
		buildStr.append(", ");
		buildStr.append(getCommitEmail());
		buildStr.append(", ");
		buildStr.append(getCommitDate());
		buildStr.append("]");

		return buildStr.toString();
	}
}
