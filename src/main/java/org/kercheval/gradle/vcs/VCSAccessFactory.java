package org.kercheval.gradle.vcs;

import org.gradle.api.logging.Logger;

import java.io.File;

//
// This is a simple factory class that supports the return of a vcs access
// layer.
//
// NOTE: Currently there is only GIT support, but auto detection of any
// number of other revision control sources is very reasonable.
//
public class VCSAccessFactory {
    public static IVCSAccess getCurrentVCS(final File srcRootDir, final Logger logger) {
        return new VCSGitImpl(srcRootDir, logger);
    }
}
