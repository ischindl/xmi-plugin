package org.codehaus.mojo.xmi;


import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tests if the correct xmi is generated in the situation: <ul> <li>ROOT depends on A <li>ROOT depends on B </ul>
 *
 * @author Jan Van Besien
 */
public class TwoDependenciesTest extends XmiMojoTestCase
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

        Element b = model.addElement("ownedMember");
        b.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        b.addAttribute(createQNameInXmiNamespace("id"), "TEST:B");
        b.addAttribute("name", "TEST:B");
        b.addAttribute("visibility", "public");

        Element a = model.addElement("ownedMember");
        a.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        a.addAttribute(createQNameInXmiNamespace("id"), "TEST:A");
        a.addAttribute("name", "TEST:A");
        a.addAttribute("visibility", "public");

        {
            Element d2 = project.addElement("ownedMember");
            d2.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
            d2.addAttribute(createQNameInXmiNamespace("id"), "TEST:ROOT_TEST:B");
            d2.addAttribute("visibility", "public");
            Element s = d2.addElement("supplier");
            s.addAttribute(createQNameInXmiNamespace("idref"), "TEST:B");
            Element c = d2.addElement("client");
            c.addAttribute(createQNameInXmiNamespace("idref"), "TEST:ROOT");
        }
        {
            Element d1 = project.addElement("ownedMember");
            d1.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
            d1.addAttribute(createQNameInXmiNamespace("id"), "TEST:ROOT_TEST:A");
            d1.addAttribute("visibility", "public");
            Element s = d1.addElement("supplier");
            s.addAttribute(createQNameInXmiNamespace("idref"), "TEST:A");
            Element c = d1.addElement("client");
            c.addAttribute(createQNameInXmiNamespace("idref"), "TEST:ROOT");
        }

        return xmiDocument;
    }

    public void test() throws Exception
    {
        final File pomXmlFile = FileUtils.toFile(this.getClass().getResource("two-dependencies-pom.xml"));
        XmiMojo mojo = (XmiMojo) this.lookupMojo("xmi", pomXmlFile);
        mojo.execute();

        assertNotNull(mojo.getXmiDocument());
        assertEquals(asFormattedXML(expected()), asFormattedXML(mojo.getXmiDocument()));
    }

}
