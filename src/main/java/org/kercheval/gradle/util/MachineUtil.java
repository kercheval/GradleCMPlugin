package org.kercheval.gradle.util;

import org.gradle.api.Project;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Date;

public class MachineUtil
{
	Project project;

	public MachineUtil(final Project project)
	{
		this.project = project;
	}

	//
	// Obtain the current machine and VM information. The VM is that running
	// Gradle, but is normally also the compilation VM. This is not guaranteed,
	// but appropriate for this use.
	//
	public SortedProperties getMachineInfo()
	{
		final SortedProperties props = new SortedProperties();
		InetAddress addr;

		try
		{
			addr = InetAddress.getLocalHost();
			props.addProperty("machine.hostname", addr.getHostName());
			props.addProperty("machine.hostaddress", addr.getHostAddress());
			props.addProperty("machine.time", new Date().toString());
		}
		catch (final UnknownHostException e)
		{
			project.getLogger().error(e.getMessage());
		}

		props.addProperty("machine.username", System.getProperty("user.name"));
		props.addProperty("machine.os.name", System.getProperty("os.name"));
		props.addProperty("machine.os.version", System.getProperty("os.version"));
		props.addProperty("machine.java.version", System.getProperty("java.version"));
		props.addProperty("machine.java.vendor", System.getProperty("java.vendor"));
		props.addProperty("machine.java.vm", System.getProperty("java.vm.name"));
		props.addProperty("machine.java.home", System.getProperty("java.home"));

		return props;
	}
}
