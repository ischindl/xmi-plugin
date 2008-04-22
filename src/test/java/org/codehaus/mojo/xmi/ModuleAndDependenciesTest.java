package org.codehaus.mojo.xmi;


import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Tests if the correct xmi is generated in the situation: <ul> <li>ROOT has module E <li>ROOT has module B <li>module E
 * depends on module B </ul>
 *
 * @author Jan Van Besien
 */
public class ModuleAndDependenciesTest extends XmiMojoTestCase
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

        Element e = root.addElement("ownedMember");
        e.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        e.addAttribute(createQNameInXmiNamespace("id"), "TEST:E");
        e.addAttribute("name", "TEST:E");
        e.addAttribute("visibility", "public");

        Element b = root.addElement("ownedMember");
        b.addAttribute(createQNameInXmiNamespace("type"), "uml:Package");
        b.addAttribute(createQNameInXmiNamespace("id"), "TEST:B");
        b.addAttribute("name", "TEST:B");
        b.addAttribute("visibility", "public");

        {
            Element d1 = e.addElement("ownedMember");
            d1.addAttribute(createQNameInXmiNamespace("type"), "uml:Dependency");
            d1.addAttribute(createQNameInXmiNamespace("id"), "TEST:E_TEST:B");
            d1.addAttribute("visibility", "public");
            Element s = d1.addElement("supplier");
            s.addAttribute(createQNameInXmiNamespace("idref"), "TEST:B");
            Element c = d1.addElement("client");
            c.addAttribute(createQNameInXmiNamespace("idref"), "TEST:E");
        }

        return xmiDocument;
    }

    public void test() throws Exception
    {
        final File pomXmlFile = FileUtils.toFile(this.getClass().getResource("module-and-dependencies-pom.xml"));
        XmiMojo mojo = (XmiMojo) this.lookupMojo("xmi", pomXmlFile);
        mojo.execute();

        assertNotNull(mojo.getXmiDocument());
        assertEquals(asFormattedXML(expected()), asFormattedXML(mojo.getXmiDocument()));
    }

}