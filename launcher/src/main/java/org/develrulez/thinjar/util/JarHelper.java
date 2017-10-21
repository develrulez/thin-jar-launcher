package org.develrulez.thinjar.util;

import org.develrulez.thinjar.maven.Dependency;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarHelper {

    private final Pattern POM_PATH_PATTERN = Pattern.compile("META-INF/maven/.*/pom.xml");

    private final Class<?> targetClass;

    private final LazyInitializer<Manifest> manifest = new LazyInitializer<Manifest>() {
        @Override
        protected Manifest initialize() {
            String jarName = getJarName();
            if (jarName.equals("classes") || jarName.equals("test-classes")) {
                throw new IllegalStateException("Manifest is not available for non-packaged class '" +
                        targetClass.getName() +
                        "'");
            }
            URL manifestUrl = getResourceUrl(JarFile.MANIFEST_NAME);
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
            throw new IllegalStateException("Manifests Class-Path attribute not set");
        }
        List<String> classPath = new ArrayList<>();
        for(String dependency : classPathValue.split("\\s")){
            if(!dependency.isEmpty()){
                classPath.add(dependency);
            }
        }
        return Collections.unmodifiableList(classPath);
    }

    public Path getJarHome() {
        return getJarPath().getParent();
    }

    public Path getJarPath() {
        String path = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();

        // If running on windows, the path contains an 'illegal' leading slash (e.g. '/C:/xzy/abc.jar'), that will be removed.
        path = path.replaceFirst("^/(.:/)", "$1");

        return Paths.get(path);
    }

    public String getJarName() {
        return getJarPath().getFileName().toString();
    }

    public URL getMavenPomUrl() {
        return getResourceUrl(getResource(POM_PATH_PATTERN));
    }

    public Manifest getManifest() {
        return manifest.get();
    }

    public URL getResourceUrl(String name) {
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

    public String getResource(Pattern pattern){
        List<String> resources = getResources(pattern);
        if(resources.size() == 0){
            throw new IllegalStateException("No resource found with pattern '" +
                    pattern.pattern() +
                    "'");
        }
        if(resources.size() > 1){
            throw new IllegalStateException("More than one resource found with pattern '" +
                    pattern.pattern() +
                    "'");
        }
        return resources.get(0);
    }

    public List<String> getResources(Pattern pattern){
        List<String> retval = new ArrayList<>();
        try(ZipFile zf = new ZipFile(getJarPath().toFile())){
            Enumeration e = zf.entries();
            while(e.hasMoreElements()){
                ZipEntry ze = (ZipEntry) e.nextElement();
                String fileName = ze.getName();
                if(pattern.matcher(fileName).matches()){
                    retval.add(fileName);
                }
            }
        } catch(IOException e){
            throw new IllegalStateException(e);
        }
        return retval;
    }
}