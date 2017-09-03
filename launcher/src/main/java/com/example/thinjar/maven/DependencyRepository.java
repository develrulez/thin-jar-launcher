package com.example.thinjar.maven;

import com.example.thinjar.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DependencyRepository {

    private final Path repositoryHomePath;

    private DependencyRepository(Builder builder) {
        this.repositoryHomePath = builder.repositoryHomePath;
    }

    public Path getRepositoryHomePath() {
        return repositoryHomePath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Path repositoryHomePath;

        public Builder home(Path repositoryHomePath) {
            this.repositoryHomePath = repositoryHomePath;
            return this;
        }

        public DependencyRepository build() {

            Path tempDir = null;
            try {
                tempDir = Files.createTempDirectory("thin-jar-launch-");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create temp directory.", e);
            }
            Path pomPath = tempDir.resolve("pom.xml");

            try(InputStream inputStream = Helper.getMavenPomUrl().openStream()){
                Files.copy(inputStream, pomPath);
            }catch (IOException e) {
                throw new IllegalStateException("Unable to copy pom.xml.", e);
            }

            StringBuilder cmd = new StringBuilder("mvn");
            cmd.append(" org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy-dependencies ");
            cmd.append(" -DoutputDirectory=").append(repositoryHomePath.toString());
            cmd.append(" -DoverWriteReleases=false -DoverWriteSnapshots=false -DoverWriteIfNewer=true -DincludeScope=runtime -Dmdep.useRepositoryLayout=true");

            Process process = null;
            try {
                process = new ProcessBuilder("/bin/bash", "-l", "-c", cmd.toString())
                        .inheritIO()
                        .directory(tempDir.toFile())
                        .start();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to execute Maven to download dependencies.", e);
            }

            int exitValue = 0;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unable to wait for process termination.", e);
            }
            if (exitValue > 0) {
                throw new IllegalStateException("Exit value " + exitValue);
            }

            return new DependencyRepository(this);
        }
    }
}
