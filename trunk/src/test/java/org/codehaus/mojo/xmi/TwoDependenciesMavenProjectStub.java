package org.codehaus.mojo.xmi;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * @author Jan Van Besien
 */
public class TwoDependenciesMavenProjectStub extends MavenProjectStub
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

    public List getDependencies()
    {
        Dependency d1 = new Dependency();
        d1.setGroupId("TEST");
        d1.setArtifactId("A");
        d1.setVersion("1");

        Dependency d2 = new Dependency();
        d2.setGroupId("TEST");
        d2.setArtifactId("B");
        d2.setVersion("1");

        List<Dependency> dependencies = new ArrayList<Dependency>();
        dependencies.add(d1);
        dependencies.add(d2);

        return dependencies;
    }

    public Set getDependencyArtifacts()
    {
        DefaultArtifact a1 = new DefaultArtifact("TEST", "A", VersionRange.createFromVersion("1"), "compile", "jar",
                null, new DefaultArtifactHandler("jar"));
        DefaultArtifact a2 = new DefaultArtifact("TEST", "B", VersionRange.createFromVersion("1"), "compile", "jar",
                null, new DefaultArtifactHandler("jar"));

        Set<Artifact> artifacts = new HashSet<Artifact>();
        artifacts.add(a1);
        artifacts.add(a2);

        return artifacts;
    }
}
