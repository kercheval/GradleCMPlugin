package org.kercheval.gradle.vcs;

import java.io.File;

import org.gradle.api.logging.Logger;

//
// This is a simple factory class that supports the return of a vcs access
// layer.
//
// NOTE: Currently there is only GIT support, but auto detection of any
// number of other revision control sources is very reasonable.
//
public class VCSAccessFactory
{
	public static VCSAccess getCurrentVCS(final String type, final File srcRootDir,
		final Logger logger)
	{
		final VCSAccess rVal = new VCSNoneImpl(srcRootDir, logger);
		final String desiredType = type.toLowerCase();
		if (desiredType.equals(VCSAccess.Type.GIT.toString().toLowerCase()))
		{
			return new VCSGitImpl(srcRootDir, logger);
		}
		return rVal;
	}
}
