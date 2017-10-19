package org.develrulez.thinjar.maven.plugin;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

@Ignore
public class PluginUnitTest {

    private static String buildDirectory;

    @Rule
    public final TestResources resources = new TestResources();

    @Rule
    public final TestMavenRuntime maven = new TestMavenRuntime();

    @BeforeClass
    public static void beforeClass(){
        buildDirectory = System.getProperty("buildDirectory");
    }

    @Test
    public void test() throws Exception {
        File basedir = new File(buildDirectory + "/it/spring-boot-app");
        maven.executeMojo(basedir, "thin-jar");
    }
}