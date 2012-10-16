package org.jbk.gradle.about;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class MachineUtil {

	public Properties getMachineInfo() {
		Properties props = new Properties();
		
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();

			PropertyUtil.addProperty(props, "machine.hostname", addr.getHostName());
			PropertyUtil.addProperty(props, "machine.hostaddress", addr.getHostAddress());
		} catch (UnknownHostException e) {
			// TODO log exception
		}
		
		PropertyUtil.addProperty(props, "machine.username", System.getProperty("user.name")); 
		PropertyUtil.addProperty(props, "machine.os.name", System.getProperty("os.name")); 
		PropertyUtil.addProperty(props, "machine.os.version", System.getProperty("os.version")); 
		PropertyUtil.addProperty(props, "machine.java.version", System.getProperty("java.version")); 
		PropertyUtil.addProperty(props, "machine.java.vendor", System.getProperty("java.vendor")); 
		PropertyUtil.addProperty(props, "machine.java.vm", System.getProperty("java.vm.name")); 
		PropertyUtil.addProperty(props, "machine.java.home", System.getProperty("java.home")); 
		return props;
	}
}
