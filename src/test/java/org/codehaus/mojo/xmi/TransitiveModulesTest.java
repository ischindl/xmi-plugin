package org.codehaus.mojo.xmi;


import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tests if the correct xmi is generated in the situation: <ul> <li>ROOT has module C <li>module C has module B </ul>
 *
 * @author Jan Van Besien
 */
public class TransitiveModulesTest extends XmiMojoTestCase
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

        Element root = model.addElement("ownedMember");
        root.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        root.addAttribute(createQNameInXmiNamespace("id"), "TEST:ROOT");
        root.addAttribute("name", "TEST:ROOT");
        root.addAttribute("visibility", "public");

        Element moduleC = root.addElement("ownedMember");
        moduleC.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        moduleC.addAttribute(createQNameInXmiNamespace("id"), "TEST:C");
        moduleC.addAttribute("name", "TEST:C");
        moduleC.addAttribute("visibility", "public");

        Element moduleB = moduleC.addElement("ownedMember");
        moduleB.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        moduleB.addAttribute(createQNameInXmiNamespace("id"), "TEST:D");
        moduleB.addAttribute("name", "TEST:D");
        moduleB.addAttribute("visibility", "public");

        return xmiDocument;
    }

    public void test() throws Exception
    {
        final File pomXmlFile = FileUtils.toFile(this.getClass().getResource("transitive-modules-pom.xml"));
        XmiMojo mojo = (XmiMojo) this.lookupMojo("xmi", pomXmlFile);
        mojo.execute();

        assertNotNull(mojo.getXmiDocument());
        assertEquals(asFormattedXML(expected()), asFormattedXML(mojo.getXmiDocument()));
    }

}