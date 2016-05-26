package org.kercheval.gradle.vcs;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class VCSStatusTest
{
	@Test
	public void testIsClean()
	{
		final VCSStatus status = new VCSStatus();

		Assert.assertTrue(status.isClean());

		final Set<String> emptySet = new HashSet<>();
		final Set<String> notEmptySet = new HashSet<>();

		notEmptySet.add("not empty");
		status.setAdded(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setAdded(emptySet);
		Assert.assertTrue(status.isClean());
		status.setChanged(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setChanged(emptySet);
		Assert.assertTrue(status.isClean());
		status.setRemoved(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setRemoved(emptySet);
		Assert.assertTrue(status.isClean());
		status.setMissing(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setMissing(emptySet);
		Assert.assertTrue(status.isClean());
		status.setModified(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setModified(emptySet);
		Assert.assertTrue(status.isClean());
		status.setUntracked(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setUntracked(emptySet);
		Assert.assertTrue(status.isClean());
		status.setConflicting(notEmptySet);
		Assert.assertFalse(status.isClean());
		status.setConflicting(emptySet);
		Assert.assertTrue(status.isClean());
	}
}
