package org.kercheval.gradle.info;

public abstract class BuildServerInfoSource
	implements InfoSource
{
	@Override
	public String getPropertyPrefix()
	{
		return "ci";
	}

	protected void addProperty(final SortedProperties props, final String name)
	{
		props.addProperty(getPropertyPrefix() + "." + name, System.getenv(name));
	}
}
