package org.kercheval.gradle.gradlecm;

import java.util.LinkedHashMap;

import org.gradle.api.*;
import org.kercheval.gradle.buildinfo.BuildInfoPlugin;
import org.kercheval.gradle.buildrelease.BuildReleasePlugin;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;

public class GradleCMPlugin implements Plugin<Project> {
    public static final Class<GradleCMPlugin> GRADLE_CM_PLUGIN = GradleCMPlugin.class;

    public static final Class<BuildInfoPlugin> BUILD_INFO_PLUGIN = BuildInfoPlugin.class;

    public static final Class<BuildVersionPlugin> BUILD_VERSION_PLUGIN = BuildVersionPlugin.class;

    public static final Class<BuildReleasePlugin> BUILD_RELEASE_PLUGIN = BuildReleasePlugin.class;

    public static final Class<BuildVCSPlugin> BUILD_VCS_PLUGIN = BuildVCSPlugin.class;

    @Override
    public void apply(final Project project) {
        //
        // This plugin is a simple container without tasks to pull in all
        // individual plugins in the group.
        //
        project.apply(new LinkedHashMap<String, Class<BuildVCSPlugin>>() {
            {
                put("plugin", BUILD_VCS_PLUGIN);
            }
        });
        project.apply(new LinkedHashMap<String, Class<BuildInfoPlugin>>() {
            {
                put("plugin", BUILD_INFO_PLUGIN);
            }
        });
        project.apply(new LinkedHashMap<String, Class<BuildVersionPlugin>>() {
            {
                put("plugin", BUILD_VERSION_PLUGIN);
            }
        });
        project.apply(new LinkedHashMap<String, Class<BuildReleasePlugin>>() {
            {
                put("plugin", BUILD_RELEASE_PLUGIN);
            }
        });
    }
}
