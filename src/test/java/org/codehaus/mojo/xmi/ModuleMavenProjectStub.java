package org.codehaus.mojo.xmi;


import java.io.File;

import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * @author Jan Van Besien
 */
public class ModuleMavenProjectStub extends MavenProjectStub
{
    private File basedir;

    private String artifactId;

    public ModuleMavenProjectStub(String artifactId, File basedir)
    {
        this.basedir = basedir;
        this.artifactId = artifactId;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public String getArtifactId()
    {
        return artifactId;
    }
}
