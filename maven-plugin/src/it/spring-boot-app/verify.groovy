File jarArtifact = new File( basedir, "target/spring-boot-app-1.0-SNAPSHOT.jar" )
assert jarArtifact.isFile()

File thinJarArtifact = new File( basedir, "target/spring-boot-app-1.0-SNAPSHOT-thin.jar" )
assert thinJarArtifact.isFile()

File thinRunArtifact = new File( basedir, "target/spring-boot-app-1.0-SNAPSHOT-thin.run" )
assert thinRunArtifact.isFile()

File thinExeArtifact = new File( basedir, "target/spring-boot-app-1.0-SNAPSHOT-thin.exe" )
assert thinExeArtifact.isFile()

assert thinJarArtifact.length() > jarArtifact.length()
assert thinRunArtifact.length() > thinJarArtifact.length()
assert thinExeArtifact.length() > thinRunArtifact.length()
