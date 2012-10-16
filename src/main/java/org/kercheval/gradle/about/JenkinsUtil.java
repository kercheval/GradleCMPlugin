package org.kercheval.gradle.about;

import java.util.Properties;

public class JenkinsUtil {
    public Properties getJenkinsInfo() {
        Properties props = new Properties();

        PropertyUtil.addProperty(props, "jenkins.isbuiltby",
                                 new Boolean(System.getenv("JENKINS_URL") != null).toString());
        PropertyUtil.addProperty(props, "jenkins.buildnumber", System.getenv("BUILD_NUMBER"));
        PropertyUtil.addProperty(props, "jenkins.buildid", System.getenv("BUILD_ID"));
        PropertyUtil.addProperty(props, "jenkins.nodename", System.getenv("NODE_NAME"));
        PropertyUtil.addProperty(props, "jenkins.jobname", System.getenv("JOB_NAME"));
        PropertyUtil.addProperty(props, "jenkins.buildurl", System.getenv("BUILD_URL"));
        PropertyUtil.addProperty(props, "jenkins.masterurl", System.getenv("JENKINS_URL"));

        return props;
    }
}
