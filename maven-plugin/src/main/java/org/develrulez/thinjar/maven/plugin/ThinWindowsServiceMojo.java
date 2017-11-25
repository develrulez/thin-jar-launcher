package org.develrulez.thinjar.maven.plugin;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.develrulez.thinjar.Launcher;
import org.develrulez.thinjar.util.JarHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

@Mojo(name = "thin-winsw", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ThinWindowsServiceMojo extends MojoExecutableMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path assemblyFolder = Paths.get(getProjectBuildDirectory().getPath()).resolve("thin-win-svc-assembly");
        if(Files.notExists(assemblyFolder)){
            try {
                Files.createDirectory(assemblyFolder);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to create assembly folder", e);
            }
        }

        try {
            JarHelper.forClass(getClass()).unzipFile("winsw/winsw-2.1.2-bin.exe",
                    assemblyFolder.resolve(getFinalName() + "-thin.exe"));
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }

        copy(assemblyFolder);

        executeMojo(
                getAssemblyPlugin(),
                goal("single"),
                configuration(
                        element("descriptorRefs",
                                element("descriptorRef" , "thin-winsw"))
                ),
                getExecutionEnvironment());
    }

    private void copy(Path assemblyFolder){
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_26);
        configuration.setClassForTemplateLoading(this.getClass(), "/");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        Map<String, Object> model = new HashMap<>();
        model.put("project", getMavenProject());
        model.put("serviceId", getMavenProject().getArtifactId());
        model.put("serviceName", getMavenProject().getName());
        model.put("serviceDescription", getMavenProject().getDescription());

        Path serviceConfigFile = assemblyFolder.resolve(getFinalName() + "-thin.xml");

        String templateName = "winsw/service.xml.ftl";
        try (Writer out = new OutputStreamWriter(new FileOutputStream(serviceConfigFile.toFile()))) {
            Template template = configuration.getTemplate(templateName);
            template.process(model, out);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Unable to process template '" + templateName + "'", e);
        }
    }
}
