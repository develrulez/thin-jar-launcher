package org.develrulez.thinjar.maven;

import org.develrulez.thinjar.util.JarHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DependencyRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private JarHelper jarHelper;

    @Before
    public void before() throws ClassNotFoundException {
        Class<?> testDummyClass = Class.forName("org.develrulez.thinjar.TestDummy");
        jarHelper = JarHelper.forClass(testDummyClass);
    }

    @Test
    public void test(){
        DependencyRepository repository = DependencyRepository.builder()
                .home(Paths.get(folder.getRoot().toURI()))
                .mavenPom(jarHelper.getMavenPomUrl())
                .build();
        assertTrue(Files.exists(repository.getRepositoryHomePath().resolve("org/apache/commons/commons-lang3/3.6/commons-lang3-3.6.jar")));
        assertTrue(Files.exists(repository.getRepositoryHomePath().resolve("org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar")));
    }
}