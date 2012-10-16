package org.jbk.gradle.about;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.junit.Test;

public class PropertyUtilTest {

	@Test
	public void testAddProperty() {
		Properties props = new Properties();
		
		PropertyUtil.addProperty(props, "test", null);
		assertTrue(props.size() == 1);
		assertEquals("", props.getProperty("test"));
		
		PropertyUtil.addProperty(props, "test", "foo");
		assertTrue(props.size() == 1);
		assertEquals("foo", props.getProperty("test"));
	}
	
	@Test 
	public void testStoreSorted() {
		Properties props = new Properties();
		props.setProperty("1", "foo");
		props.setProperty("2", "bar");
		props.setProperty("3", "baz");
		props.setProperty("4", "quux");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PropertyUtil.storeSorted(props, baos, "My Comment");
		
		String sortedString = baos.toString();
		System.out.println(sortedString);
		assertTrue(sortedString.indexOf("1=foo") < sortedString.indexOf("2=bar"));
		assertTrue(sortedString.indexOf("2=bar") < sortedString.indexOf("3=baz"));
		assertTrue(sortedString.indexOf("3=baz") < sortedString.indexOf("4=quux"));
	}
}
