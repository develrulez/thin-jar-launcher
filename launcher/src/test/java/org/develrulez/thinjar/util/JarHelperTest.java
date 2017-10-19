package org.develrulez.thinjar.util;

import junit.framework.TestCase;
import static org.assertj.core.api.Assertions.*;
import org.develrulez.thinjar.Launcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.regex.Pattern;

public class JarHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JarHelper sut;

    @Before
    public void before() throws ClassNotFoundException {
        Class<?> testDummyClass = Class.forName("org.develrulez.thinjar.TestDummy");
        sut = JarHelper.forClass(testDummyClass);
    }

    @Test
    public void test() {
        assertThat(sut.getJarName()).containsPattern(Pattern.compile("^thin-jar-test-dummy-.*\\.jar$"));
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
    public void testGetMavenPomUrl(){
        String mavenPomUrl = sut.getMavenPomUrl().toString();
        assertThat(mavenPomUrl).endsWith("launcher/target/test-classes/thin-jar-test-dummy-0.0.1-SNAPSHOT.jar!/META-INF/maven/org.develrulez.thinjar/thin-jar-test-dummy/pom.xml").isNotNull();
        assertThat(mavenPomUrl).startsWith("jar:file:");
    }

    @Test
    public void testGetJarHome(){
        String jarHome = sut.getJarHome().toString();
        assertThat(jarHome).endsWith("launcher/target/test-classes");
    }

    @Test
    public void testGetNonExistentResourceUrl(){
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("No resource found with name 'log4j.properties'");
        sut.getClassPathResourceUrl("log4j.properties");
    }
}