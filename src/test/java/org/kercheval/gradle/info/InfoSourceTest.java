package org.kercheval.gradle.info;

import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Assert;

public class InfoSourceTest
{
	protected void validatePropertyPrefix(final Properties props, final String prefix)
	{
		for (final Entry<Object, Object> entry : props.entrySet())
		{
			Assert.assertTrue(entry.getKey().toString().startsWith(prefix));
		}
	}
}
