package org.kercheval.gradle.info;

public class TeamCityInfoSource
	extends BuildServerInfoSource
{
	@Override
	public String getDescription()
	{
		return "TeamCity (http://www.jetbrains.com/teamcity/) continuuous integration server information";
	}

	//
	// The properties here are all found in the environment.
	//
	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = new SortedProperties();
		addProperty(props, "BUILD_NUMBER");
		addProperty(props, "TEAMCITY_BUILDCONF_NAME");
		addProperty(props, "TEAMCITY_BUILD_PROPERTIES_FILE");
		addProperty(props, "TEAMCITY_CAPTURE_ENV");
		addProperty(props, "TEAMCITY_DATA_PATH");
		addProperty(props, "TEAMCITY_GIT_PATH");
		addProperty(props, "TEAMCITY_JRE");
		addProperty(props, "TEAMCITY_PROCESS_FLOW_ID");
		addProperty(props, "TEAMCITY_PROCESS_PARENT_FLOW_ID");
		addProperty(props, "TEAMCITY_PROJECT_NAME");
		addProperty(props, "TEAMCITY_VERSION");

		return props;
	}

	@Override
	public String getPropertyPrefix()
	{
		return super.getPropertyPrefix() + ".teamcity";
	}

	@Override
	public boolean isActive()
	{
		return null != System.getenv("TEAMCITY_VERSION");
	}
}
