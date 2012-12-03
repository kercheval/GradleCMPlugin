package org.kercheval.gradle.vcs.hg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;
import org.kercheval.gradle.console.ExecuteCommand;
import org.kercheval.gradle.vcs.VCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSInfoSource;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

public class VCSHgImpl
	extends VCSInfoSource
{
	public VCSHgImpl(final File srcRootDir, final Logger logger)
	{
		super(srcRootDir, logger);
	}

	@Override
	public void createBranch(final String branchName, final String remoteOrigin,
		final boolean ignoreOrigin)
		throws VCSException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("createBranch for Hg");
	}

	@Override
	public void createTag(final VCSTag tag)
		throws VCSException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("createTag for Hg");

	}

	@Override
	public void fetch(final String remoteOrigin)
		throws VCSException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("fetch for Hg");

	}

	@Override
	public String getBranchName()
		throws VCSException
	{
		String rVal = "";
		try
		{
			final ExecuteCommand command = new ExecuteCommand(getSrcRootDir(), "hg", "branch");
			if (command.getExitValue() != 0)
			{
				throw new VCSException("Unable to obtain tags", new IllegalStateException(
					"Process returned invalid exit code: " + command.getExitValue()));
			}
			final List<String> output = command.getOutput();
			validateOutputPresent(output);
			rVal = output.get(0);
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to obtain tags", e);
		}
		return rVal;
	}

	@Override
	public String getDescription()
	{
		return "Mercurial (http://mercurial.selenic.com/) environment information";
	}

	@Override
	public VCSStatus getStatus()
		throws VCSException
	{
		// TODO Auto-generated method stub
		return new VCSStatus();
	}

// status
//
// hg status [OPTION]... [FILE]...
// Show status of files in the repository. If names are given, only files that match are shown. Files that are clean or
// ignored or the source of a copy/move operation, are not listed unless -c/--clean, -i/--ignored, -C/--copies or
// -A/--all are given. Unless options described with "show only ..." are given, the options -mardu are used.
//
// Option -q/--quiet hides untracked (unknown and ignored) files unless explicitly requested with -u/--unknown or
// -i/--ignored.
//
// Note
//
// status may appear to disagree with diff if permissions have changed or a merge has occurred. The standard diff format
// does not report permission changes and diff only reports changes relative to one merge parent.
// If one revision is given, it is used as the base revision. If two revisions are given, the differences between them
// are shown. The --change option can also be used as a shortcut to list the changed files of a revision from its first
// parent.
//
// The codes used to show the status of files are:
//
// M = modified
// A = added
// R = removed
// C = clean
// ! = missing (deleted by non-hg command, but still tracked)
// ? = not tracked
// I = ignored
// = origin of the previous file listed as A (added)

	@Override
	public List<VCSTag> getTags(final String regexFilter)
		throws VCSException
	{
		final List<VCSTag> rVal = new ArrayList<VCSTag>();

		try
		{
			final ExecuteCommand command = new ExecuteCommand(getSrcRootDir(), "hg", "tags");
			if (command.getExitValue() != 0)
			{
				throw new VCSException("Unable to obtain tags", new IllegalStateException(
					"Process returned invalid exit code: " + command.getExitValue()));
			}
			for (final String line : command.getOutput())
			{
				final String[] lineSegments = line.split(" ");
				if (lineSegments.length <= 1)
				{
					continue;
				}
				final String name = lineSegments[0];
				final String commit = lineSegments[1];
				if (name.matches(regexFilter))
				{
					rVal.add(new VCSTag(name, commit));
// revTag
// .getFullMessage(), ident.getName(), ident.getEmailAddress(),
// ident.getWhen()));
				}
			}
		}
		catch (final IOException e)
		{
			throw new VCSException("Unable to obtain tags", e);
		}

		return rVal;
	}

// c:\projects\GitHub\GradleCMPlugin\src\test\gradle>hg log -r "tag()" --template
// "changeset: {rev}:{node|short}\nuser: {author|person}\nemail: {author|email}\ndate: {date}\nsummary: {desc|firstline}\n\n"
// changeset: 1:a4bf6d68a0e5
// user: John Kercheval
// email: kercheval@gmail.com
// date: 1353271974.028800
// summary: Testings
//
// changeset: 2:3e76858bf99f
// user: John Kercheval
// email: kercheval@gmail.com
// date: 1353292094.028800
// summary: Added tag 'fulltag' for changeset a4bf6d68a0e5
//
//
// c:\projects\GitHub\GradleCMPlugin\src\test\gradle>hg tags
// tip 3:ad1f9f73b57b
// anothertag 2:3e76858bf99f
// localtag 1:a4bf6d68a0e5
// 'fulltag' 1:a4bf6d68a0e5

	@Override
	public Type getType()
	{
		return VCSAccess.Type.MERCURIAL;
	}

	@Override
	public void merge(final String fromBranch, final String remoteOrigin)
		throws VCSException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("merge for Hg");
	}

	@Override
	public void push(final String from, final String remoteOrigin, final boolean pushTag)
		throws VCSException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("push for Hg");
	}

	private void validateOutputPresent(final List<String> output)
		throws VCSException
	{
		if (output.size() < 1)
		{
			throw new VCSException("Unable to run command", new IllegalStateException(
				"Expected output from 'hg' not found"));
		}
	}
}
