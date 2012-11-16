package org.kercheval.gradle.info;

public abstract class BuildServerInfoSource
	implements InfoSource
{
	@Override
	public String getPropertyPrefix()
	{
		return "ci";
	}
}
