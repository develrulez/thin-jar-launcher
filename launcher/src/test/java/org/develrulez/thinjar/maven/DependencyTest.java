package org.develrulez.thinjar.maven;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DependencyTest {

    @Test
    public void testArtifactString(){
        Dependency artifact = Dependency.from("com.example:spring-boot-thin-without-parent:0.0.1-SNAPSHOT");
        assertThat(artifact.getGroupId()).isEqualTo("com.example");
        assertThat(artifact.getArtifactId()).isEqualTo("spring-boot-thin-without-parent");
        assertThat(artifact.getPackaging()).isEqualTo("jar");
        assertThat(artifact.getClassifier()).isNull();
        assertThat(artifact.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(artifact.getScope()).isNull();
    }

    @Test
    public void testFromString(){
        Dependency artifact = Dependency.from("org.springframework.boot.experimental:spring-boot-thin-launcher:jar:exec:1.0.6.RELEASE:compile");
        assertThat(artifact.getGroupId()).isEqualTo("org.springframework.boot.experimental");
        assertThat(artifact.getArtifactId()).isEqualTo("spring-boot-thin-launcher");
        assertThat(artifact.getPackaging()).isEqualTo("jar");
        assertThat(artifact.getClassifier()).isEqualTo("exec");
        assertThat(artifact.getVersion()).isEqualTo("1.0.6.RELEASE");
        assertThat(artifact.getScope()).isEqualTo("compile");
    }

    @Test
    public void testRepositoryLayoutPath(){
        Dependency artifact = Dependency.from("org.springframework.boot.experimental:spring-boot-thin-launcher:jar:exec:1.0.6.RELEASE:compile");
        assertThat(artifact.getRepositoryLayoutPath()).isEqualTo("org/springframework/boot/experimental/spring-boot-thin-launcher/1.0.6.RELEASE/spring-boot-thin-launcher-1.0.6.RELEASE-exec.jar");
    }

    @Test
    public void testDependencyCopyArtifact(){
        Dependency artifact = Dependency.from("org.yaml:snakeyaml:jar:1.17:runtime");
        assertThat(artifact.getAsDependencyCopyArtifact()).isEqualTo("org.yaml:snakeyaml:1.17:jar");
    }
}