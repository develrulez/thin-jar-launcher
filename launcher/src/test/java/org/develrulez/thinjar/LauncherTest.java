package org.develrulez.thinjar;

import org.assertj.core.api.Assertions;
import org.develrulez.thinjar.util.JarHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class LauncherTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private JarHelper jarHelper;

    @Before
    public void before() throws ClassNotFoundException {
        Class<?> testDummyClass = Class.forName("org.develrulez.thinjar.TestDummy");
        jarHelper = JarHelper.forClass(testDummyClass);
    }

    @Test
    public void testLaunch(){
        Launcher launcher = Launcher.withJarHelper(jarHelper);
        launcher.launch();
        Assertions.assertThat(systemOutRule.getLog()).isEqualTo("I'm up and running...\n");
    }
}