package org.develrulez.thinjar.maven;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dependency {

    private static final Pattern ARTIFACT_PATTERN = Pattern.compile("^(?<groupId>[^:\\s]*):(?<artifactId>[^:\\s]*):(?<version>[^:\\s]*)(?::(?<packaging>jar)(?::(?<classifier>[^:\\s]*))?)?$");

    private static final Pattern DEPENDENCY_PLUGIN_GENERATED = Pattern.compile("^(?<groupId>[^:\\s]*):(?<artifactId>[^:\\s]*):(?<packaging>jar)(?::(?<classifier>[^:\\s]*))?:(?<version>[^:\\s]*):(?<scope>[^:\\s]*)$");

    private static final Set<Pattern> PATTERNS = new HashSet<>(Arrays.asList(
            ARTIFACT_PATTERN, DEPENDENCY_PLUGIN_GENERATED
    ));

    private final String groupId;
    private final String artifactId;
    private final String packaging;
    private final String version;
    private final String classifier;
    private final String scope;

    public Dependency(String groupId, String artifactId, String packaging, String version, String classifier, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.packaging = packaging;
        this.version = version;
        this.classifier = classifier;
        this.scope = scope;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getVersion() {
        return version;
    }

    public String getScope() {
        return scope;
    }

    public String getClassifier() {
        return classifier;
    }

    public static boolean isApplicable(String input){
        return DEPENDENCY_PLUGIN_GENERATED.matcher(input).matches() || ARTIFACT_PATTERN.matcher(input).matches();
    }

    private static Matcher getMatcher(String input){
        for(Pattern pattern : PATTERNS){
            Matcher matcher = pattern.matcher(input);
            if(matcher.find()){
                return matcher;
            }
        }
        throw new IllegalArgumentException("Input '" +
                input +
                "' doesn't match on any pattern.");
    }

    public static Dependency from(String input){
        Matcher matcher = getMatcher(input);
        Map<String, Integer> namedGroups = getNamedGroups(matcher.pattern());
        return new Dependency(
                matcher.group("groupId"),
                matcher.group("artifactId"),
                matcher.group("packaging"),
                matcher.group("version"),
                matcher.group("classifier"),
                namedGroups.containsKey("scope") ? matcher.group("scope") : null
        );
    }

    private static Map<String, Integer> getNamedGroups(Pattern regex) {

        Method namedGroupsMethod = null;
        try {
            namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        namedGroupsMethod.setAccessible(true);

        Map<String, Integer> namedGroups = null;
        try {
            namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if (namedGroups == null) {
            throw new IllegalStateException("Named groups extraction failed on class " + Pattern.class.getName());
        }

        return Collections.unmodifiableMap(namedGroups);
    }

    public String getRepositoryLayoutPath(){
        StringBuilder sb = new StringBuilder(groupId.replace('.', File.separatorChar))
                .append(File.separatorChar).append(artifactId)
                .append(File.separatorChar).append(version)
                .append(File.separatorChar).append(artifactId).append("-").append(version);
        if(classifier != null){
            sb.append("-").append(classifier);
        }
        sb.append(".").append(packaging);
        return sb.toString();
    }

    public String getAsDependencyCopyArtifact(){
        return new StringBuilder(groupId).append(":")
                .append(artifactId).append(":")
                .append(version).append(":")
                .append(packaging).toString();
    }
}
