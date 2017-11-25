package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.develrulez.thinjar.util.LazyInitializer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public abstract class MojoExecutableMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String finalName;

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File projectBuildDirectory;

    @Parameter( defaultValue = "${plugin}", readonly = true )
    private PluginDescriptor plugin;

    private LazyInitializer<ExecutionEnvironment> executionEnvironment = new LazyInitializer<ExecutionEnvironment>() {
        @Override
        protected ExecutionEnvironment initialize() {
            return executionEnvironment(mavenProject, mavenSession, pluginManager);
        }
    };

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment.get();
    }

    public PluginDescriptor getPlugin() {
        return plugin;
    }

    public String getFinalName() {
        return finalName;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public File getProjectBuildDirectory() {
        return projectBuildDirectory;
    }

    public void attachArtifact(String artifactType, String artifactClassifier, File artifactFile){
        projectHelper.attachArtifact(mavenProject, artifactType, artifactClassifier, artifactFile);
    }

    public Plugin getAssemblyPlugin(){
        return plugin(
                artifactId("org.apache.maven.plugins"),
                groupId("maven-assembly-plugin"),
                version("3.1.0"),
                getAssemblyPluginDependencies()
        );
    }

    public Plugin getDependencyPlugin(){
        return plugin(
                artifactId("org.apache.maven.plugins"),
                groupId("maven-dependency-plugin"),
                version("3.0.2"));
    }

    public Plugin getAntrunPlugin(){
        return plugin(
                artifactId("org.apache.maven.plugins"),
                groupId("maven-antrun-plugin"),
                version("1.8"));
    }

    public Plugin getLaunch4jPlugin(){
        return plugin(
                artifactId("com.akathist.maven.plugins.launch4j"),
                groupId("launch4j-maven-plugin"),
                version("1.7.21"));
    }

    private List<Dependency> getAssemblyPluginDependencies(){
        Dependency dependency = new Dependency();
        dependency.setGroupId(plugin.getGroupId());
        dependency.setArtifactId("thin-jar-maven-assembly-descriptors");
        dependency.setVersion(plugin.getVersion());
        return Arrays.asList(dependency);
    }
}
