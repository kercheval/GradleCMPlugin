package org.kercheval.gradle.buildversion;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

import java.text.ParseException;

import java.util.Date;

public class BuildVersionTest
{
	@Test
	public void testPatternSet()
		throws ParseException
	{
		BuildVersion verify = new BuildVersion("%d%", "20121110");

		Assert.assertEquals("%d%", verify.getPattern());
		verify = new BuildVersion(null, "3.4-20121110.123456");
		Assert.assertEquals(BuildVersion.DEFAULT_PATTERN, verify.getPattern());
		verify = new BuildVersion(null, null);
		Assert.assertEquals(BuildVersion.DEFAULT_PATTERN, verify.getPattern());
	}

	@Test
	public void testCandidatePattern()
		throws ParseException
	{
		BuildVersion verify = new BuildVersion(null, "\\d+.\\d+-\\d+.\\d+", "4.5-20121101.123456");

		Assert.assertNotNull(verify.toString());
		verify = new BuildVersion(BuildVersion.DEFAULT_PATTERN + "Postfix",
			"\\d+.\\d+-\\d+.\\d+Postfix", "4.5-20121101.123456Postfix");
		Assert.assertNotNull(verify.toString());

		try
		{
			verify = new BuildVersion(null, "\\d+.\\d+-\\d+.\\d+Postfix",
				"4.5-20121101.123456Postfix");
		}
		catch (final IllegalStateException e)
		{
			Assert.assertTrue(e.getMessage().contains("does not match candidate pattern"));
		}
	}

	@Test
	public void testValidatePattern()
	{

		//
		// Test valid patterns
		//
		testValidPattern(null);
		testValidPattern("");
		testValidPattern("%d%.%t%%%");
		testValidPattern("%d%%%%t%");
		testValidPattern("Prefix%d%Infix%%Infix%t%Postfix");
		testValidPattern("Pre....fix%d%Infix%%Infix%t%Postfix");
		testValidPattern("Prefix%d%Infix%%Inf\\wix%t%Postfix");
		testValidPattern("Prefix%d%Infix%%Infix%t%P\\d+ostfix");
		testValidPattern("Prefix%d%Infix%%Infix%t%P\\d*ostfix");
		testValidPattern("Prefix%d%Infix%%Infix%t%P\\d?ostfix");
		testValidPattern("Pre....fix%d%Inf\\wix%%Infix%t%P\\d+ostfix");

		//
		// Test duplicate usage
		//
		testDuplicateVariablePattern('M');
		testDuplicateVariablePattern('m');
		testDuplicateVariablePattern('b');
		testDuplicateVariablePattern('d');
		testDuplicateVariablePattern('t');

		//
		// Test invalid patterns
		//
		testInvalidPattern(" ", "whitespace not allowed");
		testInvalidPattern("%", "unbalanced %");
		testInvalidPattern("%d", "unbalanced %");
		testInvalidPattern("%d%%", "unbalanced %");
		testInvalidPattern("%%d%", "unbalanced %");
		testInvalidPattern("%t%%%d%", "unbalanced %");
		testInvalidPattern("%w%", "invalid variable reference");
		testInvalidPattern("%tw%", "invalid variable reference");
		testInvalidPattern("%t%%w%", "invalid variable reference");
		testInvalidPattern("%w%%t%", "invalid variable reference");
		testInvalidPattern("%d%%w%%t%", "invalid variable reference");
	}

	@Test
	public void testBooleanPatternUsage()
	{

		//
		// Test boolean results
		//
		BuildVersion verify = testValidPattern(BuildVersion.DEFAULT_PATTERN);

		Assert.assertTrue(verify.useMajor());
		Assert.assertTrue(verify.useMinor());
		Assert.assertFalse(verify.useBuild());
		verify = testValidPattern("v%M%.%m%.%b%-%d%.%t%");
		Assert.assertTrue(verify.useMajor());
		Assert.assertTrue(verify.useMinor());
		Assert.assertTrue(verify.useBuild());
		verify = testValidPattern("%d%.%t%");
		Assert.assertFalse(verify.useMajor());
		Assert.assertFalse(verify.useMinor());
		Assert.assertFalse(verify.useBuild());
	}

	@Test
	public void testDefaultDate()
	{
		BuildVersion verify = new BuildVersion("", 0, 0, 0, null);

		Assert.assertNotNull(verify.getBuildDate());

		final Date now = new Date();

		verify = new BuildVersion("", 0, 0, 0, now);
		Assert.assertSame(now, verify.getBuildDate());
	}

	@Test
	public void testVersionUpdate()
	{
		final BuildVersion verify = new BuildVersion("", 0, 0, 0, null);

		verify.setMinor(30);
		Assert.assertSame(30, verify.getMinor());
		verify.incrementMinor();
		Assert.assertSame(31, verify.getMinor());
		verify.setMajor(3);
		Assert.assertSame(3, verify.getMajor());
		verify.incrementMajor();
		Assert.assertSame(4, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		verify.setBuild(23);
		Assert.assertSame(23, verify.getBuild());
		verify.incrementBuild();
		Assert.assertSame(24, verify.getBuild());

		final Date now = new Date();

		Assert.assertNotSame(now, verify.getBuildDate());
		verify.setBuildDate(now);
		Assert.assertSame(now, verify.getBuildDate());
	}

	@Test
	public void testToString()
	{
		BuildVersion verify = new BuildVersion(null, 0, 0, 0, new Date(0));

		Assert.assertEquals("0.0-19691231.160000", verify.toString());
		verify = new BuildVersion("%d%%t%", 0, 0, 0, new Date(0));
		Assert.assertEquals("19691231160000", verify.toString());
		verify = new BuildVersion("Prefix%d%infix%t%postfix", 0, 0, 0, new Date(0));
		Assert.assertEquals("Prefix19691231infix160000postfix", verify.toString());
		verify = new BuildVersion("%d%%b%%t%", 0, 0, 0, new Date(0));
		Assert.assertEquals("196912310160000", verify.toString());
		verify = new BuildVersion("%%%d%%%%t%%%", 0, 0, 0, new Date(0));
		Assert.assertEquals("%19691231%160000%", verify.toString());
		verify = new BuildVersion("ThisIsATest", 0, 0, 0, new Date(0));
		Assert.assertEquals("ThisIsATest", verify.toString());
	}

	@Test
	public void testIncrementLogic()
	{
		BuildVersion verify = testValidPattern(BuildVersion.DEFAULT_PATTERN);

		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());

		Date buildDate = verify.getBuildDate();

		Assert.assertNotNull(buildDate);
		verify.incrementVersion();
		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(1, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
		Assert.assertNotNull(verify.getBuildDate());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		buildDate = verify.getBuildDate();
		verify.incrementMajor();
		Assert.assertSame(1, verify.getMajor());
		verify.incrementMinor();
		Assert.assertSame(1, verify.getMinor());
		verify.incrementBuild();
		Assert.assertSame(1, verify.getBuild());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		verify = testValidPattern("v%M%.%m%.%b%-%d%.%t%");
		buildDate = verify.getBuildDate();
		Assert.assertNotNull(buildDate);
		verify.incrementVersion();
		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(1, verify.getBuild());
		Assert.assertNotNull(verify.getBuildDate());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		buildDate = verify.getBuildDate();
		verify.incrementMajor();
		Assert.assertSame(1, verify.getMajor());
		verify.incrementMinor();
		Assert.assertSame(1, verify.getMinor());
		verify.incrementBuild();
		Assert.assertSame(2, verify.getBuild());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		verify = testValidPattern("v%M%-%d%.%t%");
		buildDate = verify.getBuildDate();
		Assert.assertNotNull(buildDate);
		verify.incrementVersion();
		Assert.assertSame(1, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
		Assert.assertNotNull(verify.getBuildDate());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		buildDate = verify.getBuildDate();
		verify.incrementMajor();
		Assert.assertSame(2, verify.getMajor());
		verify.incrementMinor();
		Assert.assertSame(1, verify.getMinor());
		verify.incrementBuild();
		Assert.assertSame(1, verify.getBuild());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		verify = testValidPattern("%d%.%t%");
		buildDate = verify.getBuildDate();
		Assert.assertNotNull(buildDate);
		verify.incrementVersion();
		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
		Assert.assertNotNull(verify.getBuildDate());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
		buildDate = verify.getBuildDate();
		verify.incrementMajor();
		Assert.assertSame(1, verify.getMajor());
		verify.incrementMinor();
		Assert.assertSame(1, verify.getMinor());
		verify.incrementBuild();
		Assert.assertSame(1, verify.getBuild());
		Assert.assertNotSame(buildDate, verify.getBuildDate());
	}

	@Test
	public void testUpdateMajor()
	{
		final BuildVersion verify = testValidPattern(BuildVersion.DEFAULT_PATTERN);

		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
		verify.setMinor(14);
		Assert.assertSame(0, verify.getMajor());
		Assert.assertSame(14, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
		verify.updateMajor(46);
		Assert.assertSame(46, verify.getMajor());
		Assert.assertSame(0, verify.getMinor());
		Assert.assertSame(0, verify.getBuild());
	}

	private void testDuplicateVariablePattern(final char c)
	{
		testInvalidPattern("%" + c + "%%" + c + "%", "used more than once");
		testInvalidPattern("foo%" + c + "%%" + c + "%", "used more than once");
		testInvalidPattern("%" + c + "%bar%" + c + "%", "used more than once");
		testInvalidPattern("%" + c + "%%" + c + "%baz", "used more than once");
		testInvalidPattern("foo%" + c + "%bar%" + c + "%baz", "used more than once");
		testInvalidPattern("%" + c + "%foo%" + c + "%bar%" + c + "%", "used more than once");
		testInvalidPattern("%" + c + "%%d%%" + c + "%", "used more than once");
		testInvalidPattern("%" + c + "%%t%%" + c + "%", "used more than once");
	}

	private void testInvalidPattern(final String pattern, final String exceptionContains)
	{
		try
		{
			final BuildVersion buildVersion = new BuildVersion(pattern, 0, 0, 0, null);

			fail("Invalid pattern was not caught: " + buildVersion.toString());
		}
		catch (final IllegalArgumentException e)
		{
			Assert.assertTrue(e.getMessage().contains(exceptionContains));
		}
	}

	private BuildVersion testValidPattern(final String pattern)
	{
		BuildVersion rVal = null;

		try
		{
			rVal = new BuildVersion(pattern, 0, 0, 0, null);
			System.out.println("Pattern '" + pattern + "' produced '" + rVal + "'");
		}
		catch (final IllegalArgumentException e)
		{
			fail("Valid pattern was rejected: " + e.getMessage());
		}

		return rVal;
	}

	@Test
	public void testParseCandidate()
		throws ParseException
	{
		final BuildVersion versionNow = new BuildVersion(BuildVersion.DEFAULT_PATTERN);
		BuildVersion verify = new BuildVersion(BuildVersion.DEFAULT_PATTERN, versionNow.toString());

		Assert.assertEquals(versionNow.toString(), verify.toString());
		verify = new BuildVersion("%M%.%m%.%b%-%d%.%t%", "9.3.456-20121101.123456");
		Assert.assertSame(9, verify.getMajor());
		Assert.assertSame(3, verify.getMinor());
		Assert.assertEquals(456, verify.getBuild());
		Assert.assertEquals("1351798496000", Long.valueOf(verify.getBuildDate().getTime())
			.toString());
		verify = new BuildVersion("%M%.%m%.%b%", "9.3.456");
		Assert.assertSame(9, verify.getMajor());
		Assert.assertSame(3, verify.getMinor());
		Assert.assertEquals(456, verify.getBuild());
		verify = new BuildVersion("%M%.%m%.%b%-%d%.%t%", "Prefix98.34.1456-2012111.123456Postfix");
		Assert.assertSame(98, verify.getMajor());
		Assert.assertSame(34, verify.getMinor());
		Assert.assertEquals(1456, verify.getBuild());
		Assert.assertEquals("1351798496000", Long.valueOf(verify.getBuildDate().getTime())
			.toString());
		verify = new BuildVersion("%%%M%.%m%.%b%-%d%.%t%%%",
			"Prefix98.34.1456-2012111.123456Postfix");
		Assert.assertSame(98, verify.getMajor());
		Assert.assertSame(34, verify.getMinor());
		Assert.assertEquals(1456, verify.getBuild());
		Assert.assertEquals("1351798496000", Long.valueOf(verify.getBuildDate().getTime())
			.toString());
		verify = new BuildVersion("%d%.%%%M%.%m%.%b%-%t%%%",
			"2012111.Prefix98.34.1456-123456Postfix");
		Assert.assertSame(98, verify.getMajor());
		Assert.assertSame(34, verify.getMinor());
		Assert.assertEquals(1456, verify.getBuild());
		Assert.assertEquals("1351798496000", Long.valueOf(verify.getBuildDate().getTime())
			.toString());
		verify = new BuildVersion("%d%.%%.%m%.%b%-%t%%%%M%",
			"2012111.Prefix.34.1456-123456Postfix98");
		Assert.assertSame(98, verify.getMajor());
		Assert.assertSame(34, verify.getMinor());
		Assert.assertEquals(1456, verify.getBuild());
		Assert.assertEquals("1351798496000", Long.valueOf(verify.getBuildDate().getTime())
			.toString());
		testParseFailure("%d%.%%.%m%.%b%-%t%%%%M%", "2012111.Prefix.34.1456-123456Postfix");
		testParseFailure("%d%.%%.%m%.%M%-%t%%%%b%", "2012111.Prefix.34.1456-123456Postfix");
		testParseFailure("%d%.%%.%b%.%M%-%t%%%%m%", "2012111.Prefix.34.1456-123456Postfix");
		testParseFailure("%d%.%%.%b%.%M%-%m%%%%t%", "2012111.Prefix.34.1456-123456Postfix");
		testParseFailure("%m%.%%.%b%.%M%-%t%%%%d%", "2012111.Prefix.34.1456-123456Postfix");
		testParseFailure("%M%.%m%.%b%-%d%.%t%", "9.3.456-121101.123456");
	}

	@SuppressWarnings("unused")
	void testParseFailure(final String pattern, final String candidate)
	{
		try
		{
			new BuildVersion(pattern, candidate);
			fail("ParseException expected");
		}
		catch (final ParseException e)
		{

			// Expected
		}
	}
}
