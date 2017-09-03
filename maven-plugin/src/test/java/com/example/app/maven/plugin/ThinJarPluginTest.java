package com.example.app.maven.plugin;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;

public class ThinJarPluginTest {

    @Test
    public void test() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/unit" );
        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );
    }
}
