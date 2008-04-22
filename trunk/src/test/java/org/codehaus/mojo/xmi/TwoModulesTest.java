package org.codehaus.mojo.xmi;


import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tests if the correct xmi is generated in the situation: <ul> <li>ROOT has module A <li>ROOT has module B </ul>
 *
 * @author Jan Van Besien
 */
public class TwoModulesTest extends XmiMojoTestCase
{

    static Document expected()
    {
        Document xmiDocument = DocumentHelper.createDocument();
        Element xmiElement = xmiDocument.addElement(createQNameInXmiNamespace("XMI"));
        xmiElement.addAttribute(createQNameInXmiNamespace("version"), "2.1");
        Element model = xmiElement.addElement(createQNameInUmlNamespace("Model"));
        model.addAttribute(createQNameInXmiNamespace("id"), "model");
        model.addAttribute("name", "dependencies");
        model.addAttribute("visibility", "public");

        Element project = model.addElement("ownedMember");
        project.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        project.addAttribute(createQNameInXmiNamespace("id"), "TEST:ROOT");
        project.addAttribute("name", "TEST:ROOT");
        project.addAttribute("visibility", "public");

        Element moduleA = project.addElement("ownedMember");
        moduleA.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        moduleA.addAttribute(createQNameInXmiNamespace("id"), "TEST:A");
        moduleA.addAttribute("name", "TEST:A");
        moduleA.addAttribute("visibility", "public");

        Element moduleB = project.addElement("ownedMember");
        moduleB.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        moduleB.addAttribute(createQNameInXmiNamespace("id"), "TEST:B");
        moduleB.addAttribute("name", "TEST:B");
        moduleB.addAttribute("visibility", "public");

        return xmiDocument;
    }

    public void test() throws Exception
    {
        final File pomXmlFile = FileUtils.toFile(this.getClass().getResource("two-modules-pom.xml"));
        XmiMojo mojo = (XmiMojo) this.lookupMojo("xmi", pomXmlFile);
        mojo.execute();

        assertNotNull(mojo.getXmiDocument());
        assertEquals(asFormattedXML(expected()), asFormattedXML(mojo.getXmiDocument()));
    }

}
