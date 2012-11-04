package org.kercheval.gradle.vcs;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class VCSTagTest {
    @Test
    public void testVcsTag() {
        final Date now = new Date();
        final VCSTag tag = new VCSTag("a", "b", "c", "d", "e", now);

        Assert.assertEquals("[a, b, c, d, e, " + now.toString() + "]", tag.toString());
    }
}
