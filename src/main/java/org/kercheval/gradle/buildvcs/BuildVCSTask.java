package org.kercheval.gradle.buildvcs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.kercheval.gradle.vcs.IVCSAccess;

public class BuildVCSTask
	extends DefaultTask
{
	//
	// The type variable maps to one of the enumerations in IVCSAccess and represents
	// the VCS type.
	//
	private String type = "git";

	@TaskAction
	public void doTask()
	{
		getProject().getLogger().info(
			"Current VCS Type is set to " + getType() + ".  The task '" + getName()
				+ "' does not need direct execution and can be removed as a dependency.");
	}

	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		//
		// Validate the input type and give a nasty gram if specifying
		// an invalid VCS type.
		//
		final String desiredType = type.toLowerCase();
		boolean foundType = false;
		for (final IVCSAccess.Type iterType : IVCSAccess.Type.values())
		{
			if (desiredType.equals(iterType.toString().toLowerCase()))
			{
				foundType = true;
			}
		}
		if (!foundType)
		{
			final Set<IVCSAccess.Type> typeSet = new HashSet<IVCSAccess.Type>();
			Collections.addAll(typeSet, IVCSAccess.Type.values());

			throw new IllegalArgumentException("The type '" + type + "' is invalid for task "
				+ getName() + ".  Valid values are one of " + typeSet);
		}

		this.type = type;
	}
}
