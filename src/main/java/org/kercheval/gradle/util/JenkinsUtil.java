package org.kercheval.gradle.util;


public class JenkinsUtil {
    public SortedProperties getJenkinsInfo() {
        SortedProperties props = new SortedProperties();

        props.addProperty("jenkins.isbuiltby", new Boolean(System.getenv("JENKINS_URL") != null).toString());
        props.addProperty("jenkins.buildnumber", System.getenv("BUILD_NUMBER"));
        props.addProperty("jenkins.buildid", System.getenv("BUILD_ID"));
        props.addProperty("jenkins.nodename", System.getenv("NODE_NAME"));
        props.addProperty("jenkins.jobname", System.getenv("JOB_NAME"));
        props.addProperty("jenkins.buildurl", System.getenv("BUILD_URL"));
        props.addProperty("jenkins.masterurl", System.getenv("JENKINS_URL"));

        return props;
    }
}
