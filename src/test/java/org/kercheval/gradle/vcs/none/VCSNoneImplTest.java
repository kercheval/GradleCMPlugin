package org.kercheval.gradle.vcs.none;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.vcs.VCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

public class VCSNoneImplTest
{
	@Test
	public void testVCSNoneInterface()
		throws VCSException
	{
		final VCSNoneImpl vcs = new VCSNoneImpl(new File("FooBlah"), null);

		Assert.assertNotNull(vcs.getDescription());
		Assert.assertEquals("vcs.NONE", vcs.getPropertyPrefix());
		Assert.assertFalse(vcs.isActive());
		Assert.assertEquals(VCSAccess.Type.NONE, vcs.getType());

		final VCSStatus status = vcs.getStatus();
		Assert.assertNotNull(status);
		Assert.assertTrue(status.isClean());

		List<VCSTag> tagList = vcs.getAllTags();
		Assert.assertNotNull(tagList);
		Assert.assertTrue(tagList.isEmpty());

		tagList = vcs.getTags(".*");
		Assert.assertNotNull(tagList);
		Assert.assertTrue(tagList.isEmpty());

		final Properties props = vcs.getInfo();
		Assert.assertNotNull(props);
		Assert.assertTrue(props.isEmpty());

		try
		{
			vcs.createBranch("release", "origin", false);
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
		try
		{
			vcs.createTag(new VCSTag("name", "comment"));
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
		try
		{
			vcs.fetch("origin");
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
		try
		{
			vcs.getBranchName();
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
		try
		{
			vcs.merge("master", "origin", true);
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
		try
		{
			vcs.push("release", "origin", true);
			Assert.fail("Expected exception");
		}
		catch (final VCSException e)
		{
			// expected
		}
	}

}
