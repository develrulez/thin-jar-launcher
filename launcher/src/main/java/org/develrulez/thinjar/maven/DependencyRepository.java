package org.develrulez.thinjar.maven;

import org.develrulez.thinjar.util.OperatingSystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        private URL mavenPomUrl;

        public Builder home(Path repositoryHomePath) {
            this.repositoryHomePath = repositoryHomePath;
            return this;
        }

        public Builder mavenPom(URL url) {
            this.mavenPomUrl = url;
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

            try (InputStream inputStream = mavenPomUrl.openStream()) {
                Files.copy(inputStream, pomPath);
            }catch (IOException e) {
                throw new IllegalStateException("Unable to copy pom.xml.", e);
            }

            StringBuilder cmd = new StringBuilder("mvn")
                    .append(" org.apache.maven.plugins:maven-dependency-plugin:3.0.2:copy-dependencies ")
                    .append(" -DoutputDirectory=").append(repositoryHomePath.toString())
                    .append(" -DoverWriteReleases=false")
                    .append(" -DoverWriteSnapshots=false")
                    .append(" -DoverWriteIfNewer=true")
                    .append(" -DincludeScope=runtime")
                    .append(" -Dmdep.useRepositoryLayout=true");

            ProcessBuilder processBuilder;
            OperatingSystem operatingSystem = OperatingSystem.get();
            if(operatingSystem.isUnix() || operatingSystem.isMac()){
                processBuilder = new ProcessBuilder("/bin/bash", "-l", "-c", cmd.toString());

            }else if(operatingSystem.isWindows()){
                processBuilder = new ProcessBuilder("cmd", "/c", cmd.toString());

            }else{
                throw new IllegalStateException("Unsupported operating system type '" +
                        operatingSystem.getType().name() +
                        "'");
            }

            Process process;
            try {
                process = processBuilder.inheritIO().directory(tempDir.toFile()).start();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to execute Maven to download dependencies.", e);
            }

            int exitValue;
            try {
                exitValue = process.waitFor();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unable to wait for Maven process termination.", e);
            }
            if (exitValue > 0) {
                throw new IllegalStateException("An error occured while invoking Maven (exit value " +
                        exitValue +
                        "). Check previous console output.");
            }

            return new DependencyRepository(this);
        }
    }
}
