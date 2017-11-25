<service>
    <id>${serviceId}</id>
    <name>${serviceName}</name>
    <description>${(serviceDescription)!}</description>
    <executable>java</executable>
    <arguments>-jar ${project.build.finalName}-thin.jar</arguments>
</service>