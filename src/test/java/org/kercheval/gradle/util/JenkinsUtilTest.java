package org.kercheval.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class JenkinsUtilTest {
    @Test
    public void test() {
        final JenkinsUtil jenkinsUtil = new JenkinsUtil();
        final SortedProperties props = jenkinsUtil.getJenkinsInfo();

        try {
            props.store(System.out, "\nJenkins Info\n");
        } catch (final IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
