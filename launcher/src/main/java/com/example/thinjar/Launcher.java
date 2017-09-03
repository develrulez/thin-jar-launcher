package com.example.thinjar;

import com.example.thinjar.maven.DependencyRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

public class Launcher {

    public static void main(String[] args) {
        String startClassName = Helper.getManifest().getMainAttributes().getValue("Start-Class");
        DependencyRepository repository = DependencyRepository.builder().home(Helper.getJarHome().resolve("lib")).build();
        loadDependencies(repository);
        executeStartClass(startClassName, args);
    }

    private static void executeStartClass(String startClassName, String[] args) {
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

    private static void loadDependencies(DependencyRepository repository){
        String classPath = Helper.getManifest().getMainAttributes().getValue("Class-Path");
        if(classPath == null || classPath.isEmpty()){
            throw new IllegalStateException("Manifests Class-Path attribute must not be null or empty.");
        }

        for(String dependency : classPath.split("\\s")){
            if(dependency.isEmpty()){
                continue;
            }
            Path dependencyPath = repository.getRepositoryHomePath().resolve(dependency);
            System.out.println(dependencyPath.toString());
            loadDependency(dependencyPath);
        }
    }

    private static synchronized void loadDependency(Path jarPath) {
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
