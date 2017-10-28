package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.develrulez.thinjar.maven.Dependency;
import org.develrulez.thinjar.util.JarHelper;
import org.eclipse.aether.RepositorySystemSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.attribute;

@Mojo(name = "thin-linux-executable", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinLinuxExecutableMojo extends MojoExecutableMojo {

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File outputDirectory;

    @Parameter( property = "artifact")
    private String artifact;

    @Parameter( property = "script")
    private String script;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String finalNameThinJar = finalName + "-thin.jar";
        String finalNameThinRun = finalName + "-thin.run";

        Path launchScriptPath = null;
        try {
            launchScriptPath = getLaunchScriptPath();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to get launch script path", e);
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
                getExecutionEnvironment()
        );
        attachArtifact("run", "thin", Paths.get(outputDirectory.toURI()).resolve(finalNameThinRun).toFile());
    }

    private Path getLaunchScriptPath() throws MojoExecutionException, IOException {

        Path launchScriptPath = Paths.get(outputDirectory.toURI()).resolve("launch.sh");

        if(artifact != null){
            if(script == null){
                throw new MojoExecutionException("When an artifact is defined a script path must be specified to search for.");
            }
            executeMojo(
                    plugin(
                            artifactId("org.apache.maven.plugins"),
                            groupId("maven-dependency-plugin"),
                            version("3.0.2")),
                    goal("get"),
                    configuration(
                            element("artifact", artifact),
                            element("transitive", "false")
                    ),
                    getExecutionEnvironment());
            Path artifactPath = Paths.get(repositorySystemSession.getLocalRepository().getBasedir().toURI()).resolve(Dependency.from(artifact).getRepositoryLayoutPath());
            JarHelper.unzipFile(artifactPath, script, launchScriptPath);

        }else if(script != null){
            Files.copy(Paths.get(script), launchScriptPath);

        }else{
            JarHelper.forClass(getClass()).unzipFile("exec-template.sh", launchScriptPath);
        }

        return launchScriptPath;
    }
}
