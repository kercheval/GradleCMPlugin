package org.jbk.gradle.about;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class JGitUtilTest {

	@Test
	public void test() {
			JGitUtil jgu = new JGitUtil(new File("."));
			Properties props = jgu.getStatus();
			PropertyUtil.storeSorted(props, System.out, "\nGit Info\n");
			assertNotNull(props);
			assertTrue(props.size() > 0);
	}
}
