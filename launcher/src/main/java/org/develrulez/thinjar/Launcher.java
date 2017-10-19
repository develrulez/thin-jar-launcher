package org.develrulez.thinjar;

import org.develrulez.thinjar.maven.DependencyRepository;
import org.develrulez.thinjar.util.JarHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Launcher {

    private final JarHelper jarHelper;

    private Launcher(JarHelper jarHelper) {
        this.jarHelper = jarHelper;
    }

    public static void main(String[] args) {
        Launcher.withJarHelper(JarHelper.forClass(Launcher.class)).launch(args);
    }

    public static Launcher withJarHelper(JarHelper jarHelper) {
        return new Launcher(jarHelper);
    }

    public void launch(String... args) {
        loadDependencies(DependencyRepository.builder().home(jarHelper.getJarHome().resolve("lib")).mavenPom(jarHelper.getMavenPomUrl()).build());
        String startClassName = jarHelper.getManifest().getMainAttributes().getValue("Start-Class");
        try {
            Class<?> startClass = Class.forName(startClassName);
            Method mainMethod = startClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        }catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            throw new IllegalStateException("Unable to execute starter class '" +
                    startClassName +
                    "'.", e);
        }
    }

    private void loadDependencies(DependencyRepository repository) {
        for (String dependency : jarHelper.getClassPath()) {
            Path dependencyPath = repository.getRepositoryHomePath().resolve(dependency);
            if(Files.notExists(dependencyPath)){
                throw new IllegalStateException("Depedency not found with path " + dependencyPath.toString());
            }
            loadDependency(dependencyPath);
        }
    }

    private synchronized void loadDependency(Path jarPath) {
        try {
            URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL url = jarPath.toUri().toURL();
            for (URL it : Arrays.asList(loader.getURLs())){
                if (it.equals(url)){
                    return;
                }
            }
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(loader, new Object[]{url});
        } catch (final NoSuchMethodException | IllegalAccessException | MalformedURLException | InvocationTargetException e){
            throw new IllegalStateException(e);
        }
    }
}
