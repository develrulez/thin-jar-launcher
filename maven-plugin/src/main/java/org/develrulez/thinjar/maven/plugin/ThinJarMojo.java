package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.develrulez.thinjar.Launcher;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "thin-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinJarMojo extends MojoExecutableMojo {

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Parameter( defaultValue = "${project.packaging}", readonly = true )
    private String packaging;

    @Parameter( property = "mainClass", required = true)
    private String mainClass;

    public void execute() throws MojoExecutionException {

        if(!"jar".equals(packaging)){
            throw new MojoExecutionException("Unsupported packaging type '" +
                    packaging +
                    "'. Must be 'jar'.");
        }

        executeMojo(
                getDependencyPlugin(),
                goal("unpack"),
                configuration(
                        element("artifactItems",
                                element("artifactItem",
                                        element("groupId", getPlugin().getGroupId()),
                                        element("artifactId", "thin-jar-launcher"),
                                        element("version", getPlugin().getVersion()),
                                        element("type", "jar"),
                                        element("overWrite", "true"),
                                        element("outputDirectory", "${project.build.directory}/thin-jar-launcher-extracted"),
                                        element("includes", "**/*.class"))
                        )
                ),
                getExecutionEnvironment());

        executeMojo(
                getAssemblyPlugin(),
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
                getExecutionEnvironment());
    }
}