package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.develrulez.thinjar.Launcher;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "thin-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinJarMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Parameter( defaultValue = "${plugin}", readonly = true )
    private PluginDescriptor plugin;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter( property = "outputDirectory", defaultValue = "${project.build.directory}", required = true )
    private File outputDirectory;

    @Parameter( property = "mainClass", required = true)
    private String mainClass;

    public void execute() throws MojoExecutionException {

        ExecutionEnvironment executionEnvironment = executionEnvironment(mavenProject, mavenSession, pluginManager);

        executeMojo(
                plugin(
                        artifactId("org.apache.maven.plugins"),
                        groupId("maven-dependency-plugin"),
                        version("3.0.1")),
                goal("unpack"),
                configuration(
                        element("artifactItems",
                                element("artifactItem",
                                        element("groupId", plugin.getGroupId()),
                                        element("artifactId", "thin-jar-launcher"),
                                        element("version", plugin.getVersion()),
                                        element("type", "jar"),
                                        element("overWrite", "true"),
                                        element("outputDirectory", "${project.build.directory}/thin-jar-launcher-extracted"),
                                        element("includes", "**/*.class"))
                        )
                ),
                executionEnvironment);

        executeMojo(
                plugin(
                        artifactId("org.apache.maven.plugins"),
                        groupId("maven-assembly-plugin"),
                        version("3.1.0"),
                        getAssemblyPluginDependencies()
                ),
                goal("single"),
                configuration(
                        element("descriptorRefs",
                                element("descriptorRef" , "thin")),
                        element("archive",
                                element("manifest",
                                        element("addClasspath", "true"),
                                        element("mainClass", Launcher.class.getName()),
                                        element("classpathLayoutType", "repository")),
                                element("manifestEntries",
                                        element("Maven-Artifact", "${project.groupId}:${project.artifactId}:${project.version}"),
                                        element("Start-Class", mainClass))
                        )
                ),
                executionEnvironment);
    }

    private List<Dependency> getAssemblyPluginDependencies(){
        Dependency dependency = new Dependency();
        dependency.setGroupId(plugin.getGroupId());
        dependency.setArtifactId("thin-jar-maven-assembly-descriptors");
        dependency.setVersion(plugin.getVersion());
        return Arrays.asList(dependency);
    }
}