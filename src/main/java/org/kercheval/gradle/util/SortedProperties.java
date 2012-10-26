package org.kercheval.gradle.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

//
//This is a simple helper class to ensure that enumerations of the properties
//collected are simple alpha sort.  This is mainly for repeatability.
//
public class SortedProperties extends Properties {
    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }

    public void addProperty(final String key, final Object value) {
        String insertValue = "";

        if (null != value) {
            insertValue = value.toString();
        }

        setProperty(key, insertValue);
    }
}
