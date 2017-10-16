package org.develrulez.thinjar.maven.plugin;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.1.0"})
public class ThinJarMojoIT {

    private static String buildDirectory;

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public ThinJarMojoIT(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
        this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
    }

    @BeforeClass
    public static void beforeClass(){
        buildDirectory = System.getProperty("buildDirectory");
    }

    @Test
    public void test() throws Exception {
        File basedir = new File(buildDirectory + "/it/spring-boot-app");
        maven.forProject(basedir)
                .withCliOption("-X")
                .execute("clean", "package")
                .assertErrorFreeLog();
    }
}
