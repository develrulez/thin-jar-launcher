package org.develrulez.thinjar.util;

import org.develrulez.thinjar.maven.Dependency;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarHelper {

    private final Class<?> targetClass;

    private final LazyInitializer<Manifest> manifest = new LazyInitializer<Manifest>() {
        @Override
        protected Manifest initialize() {
            String jarName = getJarName();
            if (jarName.equals("classes")) {
                throw new IllegalStateException("Launcher cannot be executed from within an IDE.");
            }
            URL manifestUrl = getClassPathResourceUrl(JarFile.MANIFEST_NAME);
            try (InputStream is = manifestUrl.openStream()) {
                return new Manifest(is);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to open " +
                        JarFile.MANIFEST_NAME +
                        " from " +
                        manifestUrl.toString() +
                        "", e);
            }
        }
    };

    private JarHelper(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public static JarHelper forClass(Class<?> targetClass) {
        return new JarHelper(targetClass);
    }

    public List<String> getClassPath() {
        String classPathValue = manifest.get().getMainAttributes().getValue("Class-Path");
        if(classPathValue == null || classPathValue.isEmpty()){
            throw new IllegalStateException("Manifests Class-Path attribute must not be null or empty.");
        }

        List<String> classPath = new ArrayList<>();
        for(String dependency : classPathValue.split("\\s")){
            if(dependency.isEmpty()){
                continue;
            }
            classPath.add(dependency);
        }
        return Collections.unmodifiableList(classPath);
    }

    public Path getJarHome() {
        return getJarPath().getParent();
    }

    public Path getJarPath() {
        String path = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();

        // If running on windows, the path contains an illegal leading slash (e.g. '/C:/xzy/abc.jar'), that will be removed.
        path = path.replaceFirst("^/(.:/)", "$1");

        return Paths.get(path);
    }

    public String getJarName() {
        return getJarPath().getFileName().toString();
    }

    public URL getMavenPomUrl() {
        String mavenArtifactString = manifest.get().getMainAttributes().getValue("Maven-Artifact");
        if(mavenArtifactString == null || mavenArtifactString.isEmpty()){
            throw new IllegalStateException("Maven artifact value must not be null or empty");
        }
        Dependency mavenArtifact = Dependency.from(mavenArtifactString);
        return getClassPathResourceUrl(String.format("META-INF/maven/%s/%s/pom.xml", mavenArtifact.getGroupId(), mavenArtifact.getArtifactId()));
    }

    public Manifest getManifest() {
        return manifest.get();
    }

    private URL getClassPathResourceUrl(String name) {
        String jarName = getJarName();
        Enumeration resEnum;
        try {
            resEnum = Thread.currentThread().getContextClassLoader().getResources(name);
        }catch(IOException e){
            throw new IllegalStateException("Unable to get resources with name '" +
                    name +
                    "'.", e);
        }
        while (resEnum.hasMoreElements()) {
            URL url = (URL) resEnum.nextElement();
            if (url.toString().contains(jarName)) {
                return url;
            }
        }
        throw new IllegalStateException("No resource found with name '" +
                name +
                "'.");
    }
}
