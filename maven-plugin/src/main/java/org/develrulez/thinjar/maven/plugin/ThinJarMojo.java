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
import org.springframework.boot.loader.tools.JarWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "thin-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinJarMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter( defaultValue = "${plugin}", readonly = true )
    private PluginDescriptor plugin;

    @Parameter( property = "outputDirectory", defaultValue = "${project.build.directory}", required = true )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Parameter( defaultValue = "${project.packaging}", readonly = true )
    private String packaging;

    @Parameter( property = "mainClass", required = true)
    private String mainClass;

    @Parameter( property = "generateExecutables", defaultValue = "false")
    private boolean generateExecutables;

    public void execute() throws MojoExecutionException {

        if(!"jar".equals(packaging)){
            throw new MojoExecutionException("Unsupported packaging type '" +
                    packaging +
                    "'. Must be 'jar'.");
        }

        ExecutionEnvironment executionEnvironment = executionEnvironment(mavenProject, mavenSession, pluginManager);

        executeMojo(
                plugin(
                        artifactId("org.apache.maven.plugins"),
                        groupId("maven-dependency-plugin"),
                        version("3.0.2")),
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
                                        element("useUniqueVersions", "false"),
                                        element("mainClass", Launcher.class.getName()),
                                        element("classpathLayoutType", "repository")),
                                element("manifestEntries",
                                        element("Start-Class", mainClass))
                        )
                ),
                executionEnvironment);

        if(generateExecutables){
            generateExecutables(executionEnvironment);
        }
    }

    private void generateExecutables(ExecutionEnvironment executionEnvironment) throws MojoExecutionException {

        String finalNameThinJar = finalName + "-thin.jar";
        String finalNameThinRun = finalName + "-thin.run";
        String finalNameThinExe = finalName + "-thin.exe";

        Path launchScriptPath = Paths.get(outputDirectory.toURI()).resolve("launch.sh");
        //URL launchScriptSourceUrl = getClass().getClassLoader().getResource("launch/launch.sh");
        URL launchScriptSourceUrl = JarWriter.class.getProtectionDomain().getClassLoader().getResource("org/springframework/boot/loader/tools/launch.script");
        try(InputStream inputStream = launchScriptSourceUrl.openStream()){
            Files.copy(inputStream, launchScriptPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy launch script", e);
        }

        executeMojo(
                plugin(
                        artifactId("org.apache.maven.plugins"),
                        groupId("maven-antrun-plugin"),
                        version("1.8")
                ),
                goal("run"),
                configuration(
                        element("target",
                                element("concat",
                                        attributes(
                                                attribute("destfile", "${project.build.directory}/" + finalNameThinRun),
                                                attribute("binary", "true")
                                        ),
                                        element("fileset", attribute("file", launchScriptPath.toString())),
                                        element("fileset", attribute("file", "${project.build.directory}/" + finalNameThinJar))
                                ),
                                element("chmod", attributes(
                                        attribute("file", "${project.build.directory}/" + finalNameThinRun),
                                        attribute("perm", "755")
                                )))
                ),
                executionEnvironment
        );
        attachThinArtifact(finalNameThinRun, "run", executionEnvironment);

        executeMojo(
                plugin(
                        artifactId("com.akathist.maven.plugins.launch4j"),
                        groupId("launch4j-maven-plugin"),
                        version("1.7.21")
                ),
                goal("launch4j"),
                configuration(
                        element("headerType", "console"),
                        element("jar", "${project.build.directory}/" + finalNameThinJar),
                        element("outfile", "${project.build.directory}/" + finalNameThinExe),
                        element("classPath",
                                element("mainClass", Launcher.class.getName())),
                        element("jre",
                                // TODO Get version from maven compiler settings
                                element("minVersion", "1.8.0")),
                        element("versionInfo",
                                element("fileVersion", "1.0.0.0"),
                                element("txtFileVersion", "${project.version}"),
                                element("fileDescription", "${project.name}"),
                                element("copyright", "C"),
                                element("productVersion", "1.0.0.0"),
                                element("txtProductVersion", "1.0.0.0"),
                                element("productName", "${project.name}"),
                                element("internalName", "AppName"),
                                element("originalFilename", finalNameThinExe))
                ),
                executionEnvironment
        );
        attachThinArtifact(finalNameThinExe, "exe", executionEnvironment);
    }

    private void attachThinArtifact(String finalName, String type, ExecutionEnvironment executionEnvironment) throws MojoExecutionException {
        executeMojo(
                plugin(
                        artifactId("org.codehaus.mojo"),
                        groupId("build-helper-maven-plugin"),
                        version("3.0.0")
                ),
                goal("attach-artifact"),
                configuration(
                        element("artifacts",
                                element("artifact",
                                        element("file", "${project.build.directory}/" + finalName),
                                        element("type", type),
                                        element("classifier", "thin")))
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