package org.kercheval.gradle.about;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildInfoPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("%%%%%%%%%%%%%  Apply Called for " + project.getName());

//      project.t
//      target.task("buildInfo", )
        // TODO Auto-generated method stub
    }
}
