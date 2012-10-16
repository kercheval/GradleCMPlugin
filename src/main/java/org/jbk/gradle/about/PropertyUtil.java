package org.jbk.gradle.about;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class PropertyUtil {

	public static void addProperty(Properties props, String key, String value)
	{
		String insertValue = value;
		if (null == insertValue) {
			insertValue = "";
		}
		props.setProperty(key, insertValue);
	}

	public static void storeSorted(Properties props, OutputStream out, String comment) {
		Properties sortedProps = new Properties() {
		    @Override
		    public Set<Object> keySet(){
		        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
		    }

		    @Override
		    public synchronized Enumeration<Object> keys() {
		        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		    }
		};
		sortedProps.putAll(props);
		try {
			sortedProps.store(out, comment);
		} catch (IOException e) {
			// TODO log exception
		}
	}
}
