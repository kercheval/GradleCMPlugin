package org.jbk.gradle.about;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JGitUtilTest {

	@Test
	public void test() {
			JGitUtil jgu = new JGitUtil();
			String status = jgu.getStatus();
			System.out.println(status);
			assertNotNull(status);
			assertTrue(status.length() > 0);
	}

}
