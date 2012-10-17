package org.kercheval.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class JenkinsUtilTest {
    @Test
    public void test() {
        JenkinsUtil jenkinsUtil = new JenkinsUtil();
        SortedProperties props = jenkinsUtil.getJenkinsInfo();

        try {
            props.store(System.out, "\nJenkins Info\n");
        } catch (IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
