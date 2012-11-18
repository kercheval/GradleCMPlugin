package org.kercheval.gradle.vcs.git;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.console.TextDevice;
import org.kercheval.gradle.console.TextDevices;

public class VCSGitImplCredentialsProviderTest
{
	public static class CustomCredentialItem
		extends CredentialItem
	{
		public CustomCredentialItem(final String promptText, final boolean maskValue)
		{
			super(promptText, maskValue);
		}

		@Override
		public void clear()
		{}
	}

	@Test
	public void testGet()
		throws URISyntaxException
	{
		final String inputString = "user\npass\ny\nY\nn\n";
		final ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
		input.mark(0);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final TextDevice userIO = TextDevices.streamDevice(input, output);
		final URIish uri = new URIish("foo");

		final VCSGitImplCredentialsProvider inputCredentialsProvider = new VCSGitImplCredentialsProvider(
			userIO);

		final Map<String, String> environmentMap = new HashMap<String, String>();
		environmentMap.put("GIT_ORIGIN_USERNAME", "gituser");
		environmentMap.put("GIT_ORIGIN_PASSWORD", "gitpass");
		VCSGitImplCredentialsProvider.setEnvironmentMap(environmentMap);

		final VCSGitImplCredentialsProvider environmentCredentialsProvider = new VCSGitImplCredentialsProvider(
			userIO);
		final VCSGitImplCredentialsProvider parameterCredentialsProvider = new VCSGitImplCredentialsProvider(
			userIO, "paramuser", "parampass");

		final CredentialItem.Username userCredentialItem = new CredentialItem.Username();
		final CredentialItem.Password passCredentialItem = new CredentialItem.Password();

		inputCredentialsProvider.get(uri, userCredentialItem, passCredentialItem);

		Assert.assertEquals("user", userCredentialItem.getValue());
		Assert.assertEquals("pass", String.valueOf(passCredentialItem.getValue()));
		Assert.assertEquals("foo - Username: foo - Password: ", output.toString());

		output.reset();
		environmentCredentialsProvider.get(uri, userCredentialItem, passCredentialItem);

		Assert.assertEquals("gituser", userCredentialItem.getValue());
		Assert.assertEquals("gitpass", String.valueOf(passCredentialItem.getValue()));
		Assert.assertEquals("", output.toString());

		output.reset();
		parameterCredentialsProvider.get(uri, userCredentialItem, passCredentialItem);

		Assert.assertEquals("paramuser", userCredentialItem.getValue());
		Assert.assertEquals("parampass", String.valueOf(passCredentialItem.getValue()));
		Assert.assertEquals("", output.toString());

		final CredentialItem.StringType stringCredentialItemMasked = new CredentialItem.StringType(
			"MyPromptMasked", true);

		output.reset();
		environmentCredentialsProvider.get(uri, stringCredentialItemMasked);
		Assert.assertEquals("gitpass", String.valueOf(stringCredentialItemMasked.getValue()));
		Assert.assertEquals("", output.toString());

		output.reset();
		inputCredentialsProvider.get(uri, stringCredentialItemMasked);
		Assert.assertEquals("y", String.valueOf(stringCredentialItemMasked.getValue()));
		Assert.assertEquals("foo - MyPromptMasked: ", output.toString());

		final CredentialItem.CharArrayType charArrayCredentialItemMasked = new CredentialItem.CharArrayType(
			"HisPromptMasked", true);

		input.reset();
		output.reset();
		environmentCredentialsProvider.get(uri, charArrayCredentialItemMasked);
		Assert.assertEquals("gitpass", String.valueOf(charArrayCredentialItemMasked.getValue()));
		Assert.assertEquals("", output.toString());

		output.reset();
		inputCredentialsProvider.get(uri, charArrayCredentialItemMasked);
		Assert.assertEquals("Y", String.valueOf(charArrayCredentialItemMasked.getValue()));
		Assert.assertEquals("foo - HisPromptMasked: ", output.toString());

		final CredentialItem.CharArrayType charArrayCredentialItem = new CredentialItem.CharArrayType(
			"HisPromptMasked", false);
		output.reset();
		inputCredentialsProvider.get(uri, charArrayCredentialItem);
		Assert.assertEquals("n", String.valueOf(charArrayCredentialItem.getValue()));
		Assert.assertEquals("foo - HisPromptMasked: ", output.toString());

		final CredentialItem.InformationalMessage infoCredentialItem = new CredentialItem.InformationalMessage(
			"InfoMessage");
		output.reset();
		inputCredentialsProvider.get(uri, infoCredentialItem);
		Assert.assertEquals("foo - InfoMessage: ", output.toString());

		final CredentialItem.YesNoType ynCredentialItem = new CredentialItem.YesNoType("YNPrompt");
		input.reset();
		output.reset();
		inputCredentialsProvider.get(uri, ynCredentialItem);
		Assert.assertFalse(ynCredentialItem.getValue());
		Assert.assertEquals("foo - YNPrompt [y/n]: ", output.toString());

		inputCredentialsProvider.get(uri, ynCredentialItem);
		Assert.assertFalse(ynCredentialItem.getValue());

		inputCredentialsProvider.get(uri, ynCredentialItem);
		Assert.assertTrue(ynCredentialItem.getValue());

		inputCredentialsProvider.get(uri, ynCredentialItem);
		Assert.assertTrue(ynCredentialItem.getValue());

		inputCredentialsProvider.get(uri, ynCredentialItem);
		Assert.assertFalse(ynCredentialItem.getValue());

		try
		{
			inputCredentialsProvider.get(uri, new CustomCredentialItem("Foo", true));
			Assert.fail("Expected exception");
		}
		catch (final UnsupportedCredentialItem e)
		{
			// Expected
		}
	}

	@Test
	public void testSupportsAndIsInteractive()
	{
		final CredentialItem itemUsername = new CredentialItem.Username();
		final CredentialItem itemPassword = new CredentialItem.Password();
		final CredentialItem itemInfo = new CredentialItem.InformationalMessage("Test Info");
		final CredentialItem itemYNPrompt = new CredentialItem.YesNoType("Test YN");
		final CredentialItem itemString = new CredentialItem.StringType("String", false);
		final CredentialItem itemCharArray = new CredentialItem.CharArrayType("CharArray", false);
		final CredentialItem itemStringMasked = new CredentialItem.StringType("String", true);
		final CredentialItem itemCharArrayMasked = new CredentialItem.CharArrayType("CharArray",
			true);

		final VCSGitImplCredentialsProvider credentialProvider = new VCSGitImplCredentialsProvider(
			TextDevices.defaultTextDevice());
		Assert.assertTrue(credentialProvider.supports(itemUsername, itemPassword, itemInfo,
			itemYNPrompt, itemString, itemCharArray, itemStringMasked, itemCharArrayMasked));
		Assert.assertFalse(credentialProvider.supports(new CustomCredentialItem("Custom", true)));
		Assert.assertTrue(credentialProvider.isInteractive());
	}
}
