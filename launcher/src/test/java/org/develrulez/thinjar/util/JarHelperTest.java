package org.develrulez.thinjar.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class JarHelperTest {

    private static String buildDirectory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JarHelper sut;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass(){
        buildDirectory = System.getProperty("buildDirectory");
    }

    @Before
    public void before() throws ClassNotFoundException {
        Class<?> testDummyClass = Class.forName("org.develrulez.thinjar.TestDummy");
        sut = JarHelper.forClass(testDummyClass);
    }

    @Test
    public void test() {
        assertThat(sut.getJarName()).containsPattern(Pattern.compile("^dummy-.*\\.jar$"));
    }

    @Test
    public void testGetManifestForNonPackagedClass(){
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Manifest is not available for non-packaged class 'org.develrulez.thinjar.util.JarHelperTest'");
        JarHelper.forClass(JarHelperTest.class).getManifest();
    }

    @Test
    public void testGetClassPathFromManifest(){
        List<String> classPath = sut.getClassPath();
        assertThat(classPath).hasSize(2);
    }

    @Test
    public void testGetResource(){
        assertThat(sut.getResource(Pattern.compile(".*/pom.xml"))).isNotEmpty();
    }

    @Test
    public void testGetResourceWithNoMatch(){
        Pattern pattern = Pattern.compile(".*/asdasd.xml");
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("No resource found with pattern '" +
                pattern.pattern() +
                "'");
        sut.getResource(pattern);
    }


    @Test
    public void testGetResourceWithMultipleMatch(){
        Pattern pattern = Pattern.compile("org/.*");
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("More than one resource found with pattern '" +
                pattern.pattern() +
                "'");
        sut.getResource(pattern);
    }

    @Test
    public void testGetResources(){
        List<String> resources = sut.getResources(Pattern.compile(".*"));
        assertThat(resources).hasSize(11);
        resources = sut.getResources(Pattern.compile("META-INF/.*"));
        assertThat(resources).hasSize(7);
        resources = sut.getResources(Pattern.compile("abcxyz"));
        assertThat(resources).hasSize(0);
    }

    @Test
    public void testGetMavenPomUrl(){
        String mavenPomUrl = sut.getMavenPomUrl().toString();
        assertThat(mavenPomUrl).endsWith("launcher/target/test-projects/dummy/target/dummy-x.y.z-SNAPSHOT.jar!/META-INF/maven/org.develrulez.thinjar.ut/dummy/pom.xml");
        assertThat(mavenPomUrl).startsWith("jar:file:");
    }

    @Test
    public void testGetJarPath(){
        String jarPath = sut.getJarPath().toString();
        assertThat(jarPath).endsWith("launcher/target/test-projects/dummy/target/dummy-x.y.z-SNAPSHOT.jar");
    }

    @Test
    public void testGetJarHome(){
        String jarHome = sut.getJarHome().toString();
        assertThat(jarHome).endsWith("launcher/target/test-projects/dummy/target");
    }

    @Test
    public void testGetNonExistentResourceUrl(){
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("No resource found with name 'log4j.properties'");
        sut.getResourceUrl("log4j.properties");
    }

    @Test
    public void testStaticUnzipFile() throws URISyntaxException, IOException {
        Path jarPath = Paths.get(buildDirectory).resolve("test-projects/dummy/target/dummy-x.y.z-SNAPSHOT.jar");
        assertThat(Files.exists(jarPath));
        Path target = Paths.get(temporaryFolder.getRoot().toString()).resolve("pom.xml");
        JarHelper.unzipFile(jarPath, "META-INF/maven/org.develrulez.thinjar.ut/dummy/pom.xml", target);
        assertThat(Files.size(target)).isGreaterThan(0);
    }

    @Test
    public void testUnzipFile() throws IOException {
        Path target = Paths.get(temporaryFolder.getRoot().toString()).resolve("MANIFEST.MF");
        JarHelper.unzipFile(sut.getJarPath(), "META-INF/MANIFEST.MF", target);
        assertThat(Files.size(target)).isGreaterThan(0);
    }

    @Test
    public void testUnzipWithNonExistentFile() throws IOException {
        Path target = Paths.get(temporaryFolder.getRoot().toString()).resolve("MANIFEST.MF");
        expectedException.expect(FileNotFoundException.class);
        expectedException.expectMessage("File 'META-INF/MANIFESTSSS.MF' not found in archive '");
        JarHelper.unzipFile(sut.getJarPath(), "META-INF/MANIFESTSSS.MF", target);
    }
}