package org.codehaus.mojo.xmi;


import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tests if the correct xmi is generated in the situation: <ul> <li>ROOT has module A <li>ROOT has module B <li>A
 * depends on C <li>B depends on C </ul>
 *
 * @author Jan Van Besien
 */
public class DiamondDependenciesTest extends XmiMojoTestCase
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

        Element a = root.addElement("ownedMember");
        a.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        a.addAttribute(createQNameInXmiNamespace("id"), "TEST:A");
        a.addAttribute("name", "TEST:A");
        a.addAttribute("visibility", "public");

        Element b = root.addElement("ownedMember");
        b.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        b.addAttribute(createQNameInXmiNamespace("id"), "TEST:B");
        b.addAttribute("name", "TEST:B");
        b.addAttribute("visibility", "public");

        Element c = model.addElement("ownedMember");
        c.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        c.addAttribute(createQNameInXmiNamespace("id"), "TEST:C");
        c.addAttribute("name", "TEST:C");
        c.addAttribute("visibility", "public");

        {
            Element d1 = a.addElement("ownedMember");
            d1.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
            d1.addAttribute(createQNameInXmiNamespace("id"), "TEST:A_TEST:C");
            d1.addAttribute("visibility", "public");
            Element supplier = d1.addElement("supplier");
            supplier.addAttribute(createQNameInXmiNamespace("idref"), "TEST:C");
            Element client = d1.addElement("client");
            client.addAttribute(createQNameInXmiNamespace("idref"), "TEST:A");
        }
        {
            Element d2 = b.addElement("ownedMember");
            d2.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
            d2.addAttribute(createQNameInXmiNamespace("id"), "TEST:B_TEST:C");
            d2.addAttribute("visibility", "public");
            Element supplier = d2.addElement("supplier");
            supplier.addAttribute(createQNameInXmiNamespace("idref"), "TEST:C");
            Element client = d2.addElement("client");
            client.addAttribute(createQNameInXmiNamespace("idref"), "TEST:B");
        }

        return xmiDocument;
    }

    public void test() throws Exception
    {
        final File pomXmlFile = FileUtils.toFile(this.getClass().getResource("diamond-dependencies-pom.xml"));
        XmiMojo mojo = (XmiMojo) this.lookupMojo("xmi", pomXmlFile);
        mojo.execute();

        assertNotNull(mojo.getXmiDocument());
        assertEquals(asFormattedXML(expected()), asFormattedXML(mojo.getXmiDocument()));
    }

}