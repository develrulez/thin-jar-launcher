# Thin Jar Launcher

This Maven plugin aims to provide a lightweight solution, to build thin jars, which get its dependencies at launch time via a [proper locally installed Maven](https://maven.apache.org/install.html) distribution.

**[The Central Repository](http://central.sonatype.org/) integration is in process.**

When this is done, you can simply add the following plugin to your distributable jar artifact...

```xml
<plugin>
  <groupId>org.devrulez.thinjar</groupId>
  <artifactId>thin-jar-maven-plugin</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <executions>
    <execution>
      <id>thin-jar</id>
      <goals>
        <goal>thin-jar</goal>
      </goals>
      <configuration>
        <mainClass>org.example.ExampleApplication</mainClass>
      </configuration>
    </execution>
  </executions>
</plugin>
```

... and you'll get an additional artifact with the prefix **-thin.jar**.

To be continued...

## License

This project is Open Source software released under 
[the MIT license](https://opensource.org/licenses/MIT).