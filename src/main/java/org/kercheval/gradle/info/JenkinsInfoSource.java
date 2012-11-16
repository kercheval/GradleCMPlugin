package org.kercheval.gradle.info;

public class JenkinsInfoSource
	extends BuildServerInfoSource
{
	@Override
	public String getDescription()
	{
		return "Jenkins (http://jenkins-ci.org/) continuous integration server information";
	}

	//
	// The properties here are all found in the environment. Hudson supplies
	// many of these variables as well.
	//
	// Recommendation--- Move to Jenkins and bail on Hudson.
	//
	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = new SortedProperties();
		addProperty(props, "BASE");
		addProperty(props, "BUILD_ID");
		addProperty(props, "BUILD_NUMBER");
		addProperty(props, "BUILD_TAG");
		addProperty(props, "BUILD_URL");
		addProperty(props, "EXECUTOR_NUMBER");
		addProperty(props, "JENKINS_HOME");
		addProperty(props, "JENKINS_SERVER_COOKIE");
		addProperty(props, "JENKINS_URL");
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
		return super.getPropertyPrefix() + ".jenkins";
	}

	@Override
	public boolean isActive()
	{
		return null != System.getenv("JENKINS_SERVER_COOKIE");
	}
}
