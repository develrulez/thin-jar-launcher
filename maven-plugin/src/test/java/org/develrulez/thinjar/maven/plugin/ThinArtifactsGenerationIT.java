package org.develrulez.thinjar.maven.plugin;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.1.0"})
public class ThinArtifactsGenerationIT {

    private static String buildDirectory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public ThinArtifactsGenerationIT(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
        this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
    }

    @BeforeClass
    public static void beforeClass(){
        buildDirectory = System.getProperty("buildDirectory");
    }

    @Test
    public void testGenerationOfAllArtifacts() throws Exception {
        File basedir = new File(buildDirectory + "/test-projects/all-artifacts-generation");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
        File jarFile = new File(buildDirectory + "/test-projects/all-artifacts-generation/target/all-artifacts-generation-1.0-SNAPSHOT.jar");
        assertTrue(jarFile.exists());
        File thinJarFile = new File(buildDirectory + "/test-projects/all-artifacts-generation/target/all-artifacts-generation-1.0-SNAPSHOT-thin.jar");
        assertTrue(thinJarFile.exists());
        File thinRunFile = new File(buildDirectory + "/test-projects/all-artifacts-generation/target/all-artifacts-generation-1.0-SNAPSHOT-thin.run");
        assertTrue(thinRunFile.exists());
        File thinExeFile = new File(buildDirectory + "/test-projects/all-artifacts-generation/target/all-artifacts-generation-1.0-SNAPSHOT-thin.exe");
        assertTrue(thinExeFile.exists());
        assertTrue(thinJarFile.length() > 0);
        assertTrue(thinJarFile.length() > jarFile.length());
        assertTrue(thinRunFile.length() > thinJarFile.length());
        assertTrue(thinExeFile.length() > thinRunFile.length());
    }

    @Test
    public void testMissingScript() throws Exception {
        File basedir = new File(buildDirectory + "/test-projects/linux-exec-artifact");
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("When an artifact is defined a script path must be specified to search for");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
    }

    @Test
    public void testUnsupportedPackagingType() throws Exception {
        File basedir = new File(buildDirectory + "/test-projects/unsupported-packaging-type");
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Unsupported packaging type 'pom'. Must be 'jar'.");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
    }

    @Test
    public void testLinuxExecGenerationWithScript() throws Exception {
        File basedir = new File(buildDirectory + "/test-projects/linux-exec-script");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
        File jarFile = new File(buildDirectory + "/test-projects/linux-exec-script/target/linux-exec-script-1.0-SNAPSHOT.jar");
        assertTrue(jarFile.exists());
        File thinJarFile = new File(buildDirectory + "/test-projects/linux-exec-script/target/linux-exec-script-1.0-SNAPSHOT-thin.jar");
        assertTrue(thinJarFile.exists());
        File thinRunFile = new File(buildDirectory + "/test-projects/linux-exec-script/target/linux-exec-script-1.0-SNAPSHOT-thin.run");
        assertTrue(thinRunFile.exists());
    }

    @Test
    public void testLinuxExecGeneration() throws Exception {
        File basedir = new File(buildDirectory + "/test-projects/linux-exec");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
        File jarFile = new File(buildDirectory + "/test-projects/linux-exec/target/linux-exec-1.0-SNAPSHOT.jar");
        assertTrue(jarFile.exists());
        File thinJarFile = new File(buildDirectory + "/test-projects/linux-exec/target/linux-exec-1.0-SNAPSHOT-thin.jar");
        assertTrue(thinJarFile.exists());
        File thinRunFile = new File(buildDirectory + "/test-projects/linux-exec/target/linux-exec-1.0-SNAPSHOT-thin.run");
        assertTrue(thinRunFile.exists());
    }
}