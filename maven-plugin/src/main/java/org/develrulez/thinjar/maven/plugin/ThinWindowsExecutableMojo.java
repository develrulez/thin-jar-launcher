package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.develrulez.thinjar.Launcher;

import java.io.File;
import java.nio.file.Paths;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

@Mojo(name = "thin-windows-executable", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinWindowsExecutableMojo extends MojoExecutableMojo {

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String finalNameThinJar = finalName + "-thin.jar";
        String finalNameThinExe = finalName + "-thin.exe";

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
                getExecutionEnvironment()
        );
        attachArtifact("exe", "thin", Paths.get(outputDirectory.toURI()).resolve(finalNameThinExe).toFile());
    }
}
