package org.develrulez.thinjar.util;

import junit.framework.TestCase;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.regex.Pattern;

public class JarHelperTest {

    @Test
    public void test() {
        JarHelper helper = JarHelper.forClass(TestCase.class);
        helper.getManifest();
        helper.getJarPath();
        helper.getJarName();

        Assertions.assertThat(helper.getJarName()).containsPattern(Pattern.compile("^junit-.*\\.jar$"));
    }
}