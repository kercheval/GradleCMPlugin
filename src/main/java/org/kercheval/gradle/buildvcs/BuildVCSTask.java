package org.kercheval.gradle.buildvcs;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

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
		System.out.println("vcstask has executed");
	}

	public String getType()
	{
		return type;
	}

	public void setType(final String type)
	{
		// TODO: Validate the type value here and throw illegal argument exception with acceptable values as message

		this.type = type;
	}
}
