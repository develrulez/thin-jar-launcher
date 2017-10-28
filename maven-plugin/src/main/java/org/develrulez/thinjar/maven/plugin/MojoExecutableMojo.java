package org.develrulez.thinjar.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.develrulez.thinjar.util.LazyInitializer;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;

public abstract class MojoExecutableMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Component
    private MavenProjectHelper projectHelper;

    private LazyInitializer<MojoExecutor.ExecutionEnvironment> executionEnvironment = new LazyInitializer<MojoExecutor.ExecutionEnvironment>() {
        @Override
        protected MojoExecutor.ExecutionEnvironment initialize() {
            return MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager);
        }
    };

    public MojoExecutor.ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment.get();
    }

    public void attachArtifact(String artifactType, String artifactClassifier, File artifactFile){
        projectHelper.attachArtifact(mavenProject, artifactType, artifactClassifier, artifactFile);
    }
}
