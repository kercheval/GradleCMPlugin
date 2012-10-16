package org.kercheval.gradle.about;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

public class JenkinsUtilTest {
    @Test
    public void test() {
        JenkinsUtil jenkinsUtil = new JenkinsUtil();
        Properties props = jenkinsUtil.getJenkinsInfo();

        PropertyUtil.storeSorted(props, System.out, "\nJenkins Info\n");
        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
