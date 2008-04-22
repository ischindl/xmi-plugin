package org.codehaus.mojo.xmi;


import java.io.IOException;
import java.io.StringWriter;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 * @author Jan Van Besien
 */
abstract class XmiMojoTestCase extends AbstractMojoTestCase
{

    static QName createQNameInUmlNamespace(final String name)
    {
        return DocumentFactory.getInstance().createQName(name, "uml", "http://schema.omg.org/spec/UML/2.0");
    }

    static QName createQNameInXmiNamespace(final String name)
    {
        return DocumentFactory.getInstance().createQName(name, "xmi", "http://schema.omg.org/spec/XMI/2.1");
    }

    static String asFormattedXML(final Document document) throws IOException
    {
        StringWriter stringWriter = new StringWriter();
        XMLWriter writer = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        writer.write(document);
        return stringWriter.toString();
    }
}
