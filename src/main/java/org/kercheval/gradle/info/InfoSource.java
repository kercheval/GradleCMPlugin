package org.kercheval.gradle.info;

public interface InfoSource
{
	//
	// Return a description of this info source
	//
	public abstract String getDescription();

	//
	// Return a list of properties which describe this info source.
	// All properties will be prefixed with the infoPropertyPrefix
	//
	public abstract SortedProperties getInfo();

	//
	// The prefix used for properties
	//
	public abstract String getPropertyPrefix();

	//
	// Return true if this info source is active and the getInfo
	// method will return 'interesting' information
	//
	public abstract boolean isActive();
}
