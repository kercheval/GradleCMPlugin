package org.kercheval.gradle.vcs;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Assert;
import org.junit.Test;

import org.kercheval.gradle.util.SortedProperties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import java.util.List;

public class VCSGitImplTest {
    @Test
    public void testGetInfo() {
        final Project project = ProjectBuilder.builder().build();
        final SortedProperties props = VCSAccessFactory.getCurrentVCS(new File("."), project.getLogger()).getInfo();

        try {
            props.store(System.out, "\nVCS Info\n");
        } catch (final IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }

    @Test
    public void testGetTags() {
        final VCSGitImpl git = (VCSGitImpl) VCSGitImpl.getInstance(new File("."), null);
        List<VCSTag> tagList = git.getAllTags();

        for (final VCSTag tag : tagList) {
            System.out.println(tag);
        }

        Assert.assertTrue(tagList.size() > 1);
        tagList = git.getTags("^JUNIT_Tag_Filter$");
        Assert.assertTrue(tagList.size() == 1);
    }
}
