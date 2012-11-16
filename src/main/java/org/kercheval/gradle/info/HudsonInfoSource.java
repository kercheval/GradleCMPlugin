package org.kercheval.gradle.info;

public class HudsonInfoSource
	extends BuildServerInfoSource
{
	@Override
	public String getDescription()
	{
		return "Hudson (http://hudson-ci.org/) continous integration server information";
	}

	//
	// The properties here are all found in the environment.
	//
	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = new SortedProperties();
		addProperty(props, "BUILD_ID");
		addProperty(props, "BUILD_NUMBER");
		addProperty(props, "BUILD_TAG");
		addProperty(props, "BUILD_URL");
		addProperty(props, "EXECUTOR_NUMBER");
		addProperty(props, "HUDSON_HOME");
		addProperty(props, "HUDSON_SERVER_COOKIE");
		addProperty(props, "HUDSON_URL");
		addProperty(props, "JOB_NAME");
		addProperty(props, "JOB_URL");
		addProperty(props, "NODE_LABELS");
		addProperty(props, "NODE_NAME");
		addProperty(props, "WORKSPACE");

		return props;
	}

	@Override
	public String getPropertyPrefix()
	{
		return super.getPropertyPrefix() + ".hudson";
	}

	@Override
	public boolean isActive()
	{
		return (null == System.getenv("JENKINS_SERVER_COOKIE"))
			&& (null != System.getenv("HUDSON_SERVER_COOKIE"));
	}
}
