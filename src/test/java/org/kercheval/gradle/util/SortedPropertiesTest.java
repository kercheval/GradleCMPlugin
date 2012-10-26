package org.kercheval.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SortedPropertiesTest {
    @Test
    public void testAddProperty() {
        final SortedProperties props = new SortedProperties();

        props.addProperty("test", null);
        assertTrue(props.size() == 1);
        assertEquals("", props.getProperty("test"));
        props.addProperty("test", "foo");
        assertTrue(props.size() == 1);
        assertEquals("foo", props.getProperty("test"));
    }

    @Test
    public void testStoreSorted() {
        final SortedProperties props = new SortedProperties();

        props.setProperty("2", "bar");
        props.setProperty("1", "foo");
        props.setProperty("4", "quux");
        props.setProperty("3", "baz");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            props.store(baos, "My Comment");
        } catch (final IOException e) {
            fail();
        }

        final String sortedString = baos.toString();

        System.out.println(sortedString);
        assertTrue(sortedString.indexOf("1=foo") < sortedString.indexOf("2=bar"));
        assertTrue(sortedString.indexOf("2=bar") < sortedString.indexOf("3=baz"));
        assertTrue(sortedString.indexOf("3=baz") < sortedString.indexOf("4=quux"));
    }
}
