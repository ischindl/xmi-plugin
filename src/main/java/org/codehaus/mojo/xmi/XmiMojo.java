package org.codehaus.mojo.xmi;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.tree.DefaultDependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Maven plugin to generate an xmi model of the dependencies between maven projects. Every maven project is rendered as
 * an uml package, modules are rendered as inner packages.
 *
 * @author Jan Van Besien
 * @goal xmi
 */
public class XmiMojo extends AbstractMojo
{
    //    TODO: modules in non default profile are not taken into account.
    //    TODO: modules in a pom.xml file that is not part of the build, but only of the dependencies, are not rendered as inner packages.
    //    TODO: what about dependency scope?
    //    TODO: add support for XMI version 1.2 (a lot of tools out there seem to support that version only)

    /**
     * The project whose project files to create.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Regular expression to match to the groupId.
     *
     * @parameter expression="${groupIdPattern}" default-value=".*"
     */
    private String groupIdPattern;

    /**
     * The target directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;

    /**
     * Used to construct project instances from POMs.
     *
     * @component
     * @required
     * @readonly
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * Local Repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * @component
     * @required
     * @readonly
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver artifactResolver;

    /**
     * ArtifactMetadataSource.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * A comma separated list of remote repositories to take into account when transitively resolving dependencies.
     *
     * @parameter expression="${remoteRepoList}" default-value="http://repo1.maven.org/maven2/"
     */
    private String remoteRepoList;

    /**
     * The plexus container holding all components.
     */
    private PlexusContainer container;

    /**
     * The XML Document we are generating.
     */
    private Document xmiDocument;

    /**
     * A map of modules that exist in the build; mapped by their versionLessKey.
     */
    // TODO: I would like to write new HashMap<String, MavenProject>, but then the site generation fails (docck)
    private Map<String, MavenProject> modulesByArtifact = new LinkedHashMap();

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // Run the plugin only on the root pom... we decend into the modules (if they exist) by ourselves
        if (project.isExecutionRoot())
        {
            try
            {
                preProcess(project);

                this.xmiDocument = DocumentHelper.createDocument();
                Element xmiElement = this.xmiDocument.addElement(createQNameInXmiNamespace("XMI"));
                xmiElement.addAttribute(createQNameInXmiNamespace("version"), "2.1");
                Element model = xmiElement.addElement(createQNameInUmlNamespace("Model"));
                model.addAttribute(createQNameInXmiNamespace("id"), "model");
                model.addAttribute("name", "dependencies");
                model.addAttribute("visibility", "public");

                generateXmi(project, model);
                writeXmi(this.xmiDocument);
            } catch (IOException e)
            {
                throw new MojoExecutionException("IOException while writing XMI output", e);
            } catch (ProjectBuildingException e)
            {
                throw new MojoExecutionException(
                        "Could not construct maven project for one of the modules or dependencies", e);
            } catch (DependencyTreeBuilderException e)
            {
                throw new MojoExecutionException("Could not construct dependency tree", e);
            }
        }
    }

    static QName createQNameInUmlNamespace(final String name)
    {
        return DocumentFactory.getInstance().createQName(name, "uml", "http://schema.omg.org/spec/UML/2.0");
    }

    static QName createQNameInXmiNamespace(final String name)
    {
        return DocumentFactory.getInstance().createQName(name, "xmi", "http://schema.omg.org/spec/XMI/2.1");
    }

    private void writeXmi(Document xmiDocument) throws IOException
    {
        if (!buildDirectory.exists())
            buildDirectory.mkdir();
        XMLWriter writer = new XMLWriter(new FileOutputStream(
                new File(buildDirectory, this.project.getArtifactId() + "-" + this.project.getVersion() + ".xmi"),
                false), OutputFormat.createPrettyPrint());
        writer.write(xmiDocument);
        writer.close();
    }

    /**
     * Do some preprocessing on the maven project, to make the xmi generation phase easier.
     *
     * @param project maven project
     * @throws ProjectBuildingException if something went wrong while construction a maven project from a pom.xml file
     */
    private void preProcess(final MavenProject project) throws ProjectBuildingException
    {
        // recursively list all the modules that exist in the build.
        for (Object o : project.getModules())
        {
            String module = (String) o;
            // construct maven project for module
            final File pomXmlFile = new File(project.getBasedir(), module + "/pom.xml");
            MavenProject moduleProject = this.projectBuilder.build(pomXmlFile, localRepository, null);
            // save
            this.modulesByArtifact.put(
                    ArtifactUtils.versionlessKey(moduleProject.getGroupId(), moduleProject.getArtifactId()),
                    moduleProject);
            // recursive call
            preProcess(moduleProject);
        }
    }

    /**
     * Generate the xmi Document.
     *
     * @param project maven project to generate xmi for
     * @param root    node in the xmi document where to start
     * @throws MojoExecutionException
     * @throws ProjectBuildingException
     * @throws DependencyTreeBuilderException
     */
    private void generateXmi(final MavenProject project,
                             final Element root) throws MojoExecutionException, ProjectBuildingException, DependencyTreeBuilderException
    {
        analyzeDependencies(project, root, root, 0);
    }

    /**
     * Analyze dependencies and modules recursively, and generate xmi document.
     *
     * @param project        maven project
     * @param currentElement current position in the xmi document
     * @param rootElement    root element of the relevant portion of the xmi document
     * @param logIndent      how much to indent the logging (for recursive calls)
     * @throws MojoExecutionException
     * @throws ProjectBuildingException
     * @throws DependencyTreeBuilderException
     */
    private void analyzeDependencies(final MavenProject project, final Element currentElement,
                                     final Element rootElement,
                                     final int logIndent) throws MojoExecutionException, ProjectBuildingException, DependencyTreeBuilderException
    {
        String client = ArtifactUtils.versionlessKey(project.getGroupId(), project.getArtifactId());
        this.getLog().debug(spaces(logIndent) + "xmi-plugin: analyzing [" + client + "]");
        // render client as a package, contained in the current element
        Element clientElement = currentElement.addElement("ownedMember");
        clientElement.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        clientElement.addAttribute(createQNameInXmiNamespace("id"), client);
        clientElement.addAttribute("name", client);
        clientElement.addAttribute("visibility", "public");

        // recursively deal with modules
        for (Object o : project.getModules())
        {
            String module = (String) o;
            // construct maven project for module
            final File pomXmlFile = new File(project.getBasedir(), module + "/pom.xml");
            MavenProject moduleProject = this.projectBuilder.build(pomXmlFile, localRepository, null);
            // recursive call with current element as root (all modules will be contained within it)
            analyzeDependencies(moduleProject, clientElement, rootElement, logIndent + 4);
        }

        // this projects dependencies, and recursively their dependencies
        DefaultDependencyTreeBuilder builder = new DefaultDependencyTreeBuilder();
        // TODO: is this the expected way of configuring logging?
        builder.enableLogging(new ConsoleLogger(Logger.LEVEL_DISABLED, "dependencyTreeBuilder"));
        DependencyNode dependencyNode = builder.buildDependencyTree(project, this.localRepository, this.artifactFactory,
                this.artifactMetadataSource, new AllwaysIncludeArtifactFilter(), this.artifactCollector);

        for (Object o : dependencyNode.getChildren())
        {
            DependencyNode dependency = (DependencyNode) o;
            renderDependencies(client, dependency, clientElement, rootElement, logIndent + 4);
        }
    }

    /**
     * Render dependencies recursively to xmi.
     *
     * @param client        client of the dependency
     * @param dependency    node representing the dependency from the client to the supplier
     * @param clientElement element in the xmi document representing the client
     * @param rootElement   root element of the relevant portion of the xmi document
     * @param logIndent     how much to indent the logging (for recursive calls)
     */
    private void renderDependencies(String client, DependencyNode dependency, Element clientElement,
                                    Element rootElement, int logIndent)
    {
        Artifact artifact = dependency.getArtifact();
        if (!artifact.getGroupId().matches(this.groupIdPattern))
            return;

        String supplier = ArtifactUtils.versionlessKey(artifact.getGroupId(), artifact.getArtifactId());
        this.getLog().debug(spaces(logIndent) + "  xmi-plugin: dependency [" + supplier + "]");
        // render supplier as a package if it doesn't exists yet (due to another dependency on it) and is not a module (will be rendered anyways)
        if (!projectAlreadyRendered(supplier, rootElement) && !this.modulesByArtifact.containsKey(supplier))
        {
            this.getLog().debug(spaces(logIndent) + "  xmi-plugin: adding dependency [" + supplier + "]");
            Element supplierElement = rootElement.addElement("ownedMember");
            supplierElement.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
            supplierElement.addAttribute(createQNameInXmiNamespace("id"), supplier);
            supplierElement.addAttribute("name", supplier);
            supplierElement.addAttribute("visibility", "public");

            // recursively deal with dependencies of this dependency
            for (Object o : dependency.getChildren())
            {
                DependencyNode childNode = (DependencyNode) o;
                renderDependencies(supplier, childNode, supplierElement, rootElement, logIndent + 4);
            }
        } else
        {
            this.getLog().debug(spaces(logIndent) + "  xmi-plugin: dependency [" + supplier +
                    "] already exists or will be added as module of this build");
        }

        // render dependency contained in the client of the dependency
        Element dependencyElement = clientElement.addElement("ownedMember");
        dependencyElement.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
        dependencyElement.addAttribute(createQNameInXmiNamespace("id"), client + "_" + supplier);
        dependencyElement.addAttribute("visibility", "public");
        Element s = dependencyElement.addElement("supplier");
        s.addAttribute(createQNameInXmiNamespace("idref"), supplier);
        Element c = dependencyElement.addElement("client");
        c.addAttribute(createQNameInXmiNamespace("idref"), client);
    }

    /**
     * Helper method to generate spaces, used for indentation.
     *
     * @param nSpaces number of spaces to generate
     * @return A string of spaces
     */
    private static String spaces(final int nSpaces)
    {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < nSpaces; i++)
            spaces.append(" ");
        return spaces.toString();
    }

    /**
     * Visitor to look for existing projects.
     */
    private static final class SearchExistingProjectVisitor extends VisitorSupport
    {

        boolean exists = false;

        private String project;

        public SearchExistingProjectVisitor(String project)
        {
            if (project == null)
                throw new IllegalArgumentException("invalid project [null]");
            this.project = project;
        }

        @Override
        public void visit(Element element)
        {
            if (!exists)
            {// not already found
                if (element.getName().equals("ownedMember") && project.equals(element.attribute("id").getData()))
                {
                    exists = true;
                }
            }
        }
    }

    /**
     * Checks of a certain project (xmi:ownedMember id=project) already exists in the generated xmi document.
     *
     * @param project id of the project to look for
     * @param node    node to look under
     * @return true if it exists, false otherwize
     */
    private static boolean projectAlreadyRendered(String project, Node node)
    {
        SearchExistingProjectVisitor visitor = new SearchExistingProjectVisitor(project);
        node.accept(visitor);
        return visitor.exists;
    }

    Document getXmiDocument()
    {
        return xmiDocument;
    }

    /**
     * An ArtifactFilter that includes everthing.
     *
     * TODO: write an artifact filter that matches the groupIdPattern.
     */
    private static final class AllwaysIncludeArtifactFilter implements ArtifactFilter
    {
        public boolean include(final Artifact artifact)
        {
            return true;
        }
    }

}
