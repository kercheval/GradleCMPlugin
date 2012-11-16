package org.kercheval.gradle.info;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.gradle.api.Project;

public class MachineInfoSource
	implements InfoSource
{
	Project project;

	public MachineInfoSource(final Project project)
	{
		this.project = project;
	}

	@Override
	public String getDescription()
	{

		return "Machine environment information";
	}

	//
	// Obtain the current machine and VM information. The VM is that running
	// Gradle, but is normally also the compilation VM. This is not guaranteed,
	// but appropriate for this use.
	//
	@Override
	public SortedProperties getInfo()
	{
		final SortedProperties props = new SortedProperties();
		InetAddress addr;

		try
		{
			addr = InetAddress.getLocalHost();
			props.addProperty(getPropertyPrefix() + ".hostname", addr.getHostName());
			props.addProperty(getPropertyPrefix() + ".hostaddress", addr.getHostAddress());
			props.addProperty(getPropertyPrefix() + ".time", new Date().toString());
		}
		catch (final UnknownHostException e)
		{
			project.getLogger().error(e.getMessage());
		}

		props.addProperty(getPropertyPrefix() + ".username", System.getProperty("user.name"));
		props.addProperty(getPropertyPrefix() + ".os.name", System.getProperty("os.name"));
		props.addProperty(getPropertyPrefix() + ".os.version", System.getProperty("os.version"));
		props
			.addProperty(getPropertyPrefix() + ".java.version", System.getProperty("java.version"));
		props.addProperty(getPropertyPrefix() + ".java.vendor", System.getProperty("java.vendor"));
		props.addProperty(getPropertyPrefix() + ".java.vm", System.getProperty("java.vm.name"));
		props.addProperty(getPropertyPrefix() + ".java.home", System.getProperty("java.home"));

		return props;
	}

	@Override
	public String getPropertyPrefix()
	{
		return "machine";
	}

	@Override
	public boolean isActive()
	{
		return true;
	}
}
