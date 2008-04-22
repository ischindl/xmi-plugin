package org.codehaus.mojo.xmi;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;

/**
 * @author Jan Van Besien
 */
public class TransitiveModulesMavenProjectStub extends MavenProjectStub
{

    public boolean isExecutionRoot()
    {
        return true;
    }

    public String getGroupId()
    {
        return "TEST";
    }

    public String getArtifactId()
    {
        return "ROOT";
    }

    public String getVersion()
    {
        return "1";
    }

    public Artifact getArtifact()
    {
        return new DefaultArtifact(getGroupId(), getArtifactId(), VersionRange.createFromVersion("1"), "compile", "jar",
                null, new DefaultArtifactHandler("jar"));
    }

    public File getBasedir()
    {
        File basedir = Utilities.getFile("/org/codehaus/mojo/xmi/");

        if (!basedir.exists())
        {
            basedir.mkdirs();
        }

        return basedir;
    }

    public List getModules()
    {
        List modules = new ArrayList();

        modules.add("module-C");

        return modules;
    }

    public List getCollectedProjects()
    {
        List projects = new ArrayList();

        projects.add(createModule("module-C"));

        return projects;
    }

    private MavenProject createModule(String artifactId)
    {
        return new ModuleMavenProjectStub(artifactId, new File(getBasedir(), artifactId));
    }

}