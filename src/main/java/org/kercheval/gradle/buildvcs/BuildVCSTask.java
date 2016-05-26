package org.kercheval.gradle.buildvcs;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.kercheval.gradle.vcs.VCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSInfoSource;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildVCSTask
	extends DefaultTask
{
	//
	// The type variable maps to one of the enumerations in IVCSAccess and represents
	// the VCS type.
	//
	private String type = "git";

	public void createTag(final String tagName, final String tagComment)
		throws VCSException
	{
		final VCSTag tag = new VCSTag(tagName, tagComment);

		getVCS().createTag(tag);
	}

	@TaskAction
	public void doTask()
	{
		getProject().getLogger().info(
			"Current VCS Type is set to " + getType() + ".  The task '" + getName()
				+ "' does not need direct execution and can be removed as a dependency.");
	}

	public List<VCSTag> getAllTags()
		throws VCSException
	{
		return getVCS().getAllTags();
	}

	public String getBranchName()
		throws VCSException
	{
		return getVCS().getBranchName();
	}

	public Properties getInfo()
	{
		return getVCS().getInfo();
	}

	public VCSInfoSource getInfoSource()
	{
		return (VCSInfoSource) getVCS();
	}

	public VCSStatus getStatus()
		throws VCSException
	{
		return getVCS().getStatus();
	}

	public List<VCSTag> getTags(final String filter)
		throws VCSException
	{
		return getVCS().getTags(filter);
	}

	public String getType()
	{
		return type;
	}

	private VCSAccess getVCS()
	{
		final Project project = getProject();
		final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);
		return vcsUtil.getVCS();
	}

	public boolean isClean()
		throws VCSException
	{
		return getStatus().isClean();
	}

	public void setType(final String type)
	{
		//
		// Validate the input type and give a nasty gram if specifying
		// an invalid VCS type.
		//
		final String desiredType = type.toLowerCase();
		boolean foundType = false;
		for (final VCSAccess.Type iterType : VCSAccess.Type.values())
		{
			if (desiredType.equals(iterType.toString().toLowerCase()))
			{
				foundType = true;
			}
		}
		if (!foundType)
		{
			final Set<VCSAccess.Type> typeSet = new HashSet<>();
			Collections.addAll(typeSet, VCSAccess.Type.values());

			throw new IllegalArgumentException("The type '" + type + "' is invalid for task "
				+ getName() + ".  Valid values are one of " + typeSet);
		}

		this.type = type;
	}
}
