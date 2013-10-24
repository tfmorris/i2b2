package edu.harvard.i2b2.common.util.jaxb;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import java.io.File;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class JAXBUtil {
    private static Log log = LogFactory.getLog(JAXBUtil.class);
    private String allPackageName = null;
    private JAXBContext jaxbContext = null;

    /**
     * Default Constructor
     *
     */
    protected JAXBUtil() {
    }

    /**
     * Constructor to accept package name in String array
     *
     * @param packageName
     */
    public JAXBUtil(String[] packageName) {
        StringBuffer givenPackageName = new StringBuffer();

        for (int i = 0; i < packageName.length; i++) {
            givenPackageName.append(packageName[i]);

            if ((i + 1) < packageName.length) {
                givenPackageName.append(":");
            }
        }

        allPackageName = givenPackageName.toString();
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(allPackageName);
        }

        return jaxbContext;
    }

    /**
     *
     * @param requestMessageType
     * @param doc
     * @throws JAXBUtilException
     */
    public void marshaller(JAXBElement<?> jaxbElement, Document doc)
        throws JAXBUtilException {
        try {
            JAXBContext jaxbContext = getJAXBContext();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new NamespacePrefixMapperImpl());

            // get an Apache XMLSerializer configured to generate CDATA
            marshaller.marshal(jaxbElement, doc);
        } catch (JAXBException jaxbEx) {
            jaxbEx.printStackTrace();
            throw new JAXBUtilException("Error during marshalling ", jaxbEx);
        }
    }

    /**
     *
     * @param requestMessageType
     * @param strWriter
     * @throws JAXBUtilException
     */
    public void marshaller(Object element, Writer strWriter)
        throws JAXBUtilException {
        try {
            JAXBContext jaxbContext = getJAXBContext();
            Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new NamespacePrefixMapperImpl());
            // overriding default name space
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new NamespacePrefixMapperImpl());

            // get an Apache XMLSerializer configured to generate CDATA
            // XMLSerializer serializer = getXMLSerializer(strWriter);

            // marshal using the Apache XMLSerializer
            // marshaller.marshal(requestMessageElement,
            // serializer.asContentHandler());
            marshaller.marshal(element, strWriter);
        } catch (JAXBException jaxbEx) {
            jaxbEx.printStackTrace();
            throw new JAXBUtilException("Error during marshalling ", jaxbEx);
        }
    }

    public JAXBElement unMashallFromString(String xmlString)
        throws JAXBUtilException {
        if (xmlString == null) {
            throw new JAXBUtilException("String value is Null");
        }

        JAXBElement jaxbElement = unmashalFromString(xmlString);

        return jaxbElement;
    }

    public JAXBElement unMashallFromDocument(Document doc)
        throws JAXBUtilException {
        if (doc == null) {
            throw new JAXBUtilException("Document value is Null");
        }

        JAXBElement jaxbElement = unmashalFromDocument(doc);

        return jaxbElement;
    }

    public JAXBElement unMashallerRequest(String fileName)
        throws JAXBUtilException {
        if (fileName == null) {
            throw new JAXBUtilException("File name is Null");
        }

        JAXBElement jaxbElement = null;

        try {
            JAXBContext jaxbContext = getJAXBContext();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            jaxbElement = (JAXBElement) unmarshaller.unmarshal(new File(
                        fileName));
        } catch (JAXBException jaxbEx) {
            throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
        }

        return jaxbElement;
    }

    private JAXBElement unmashalFromDocument(Document doc)
        throws JAXBUtilException {
        JAXBElement unMarshallObject = null;

        try {
            JAXBContext jaxbContext = getJAXBContext();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unMarshallObject = (JAXBElement) unmarshaller.unmarshal(doc);
        } catch (JAXBException jaxbEx) {
            throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
        }

        return unMarshallObject;
    }

    private JAXBElement unmashalFromString(String xmlString)
        throws JAXBUtilException {
        JAXBElement unMarshallObject = null;

        try {
            JAXBContext jaxbContext = getJAXBContext();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unMarshallObject = (JAXBElement) unmarshaller.unmarshal(new StringReader(
                        xmlString));
            log.debug("object.toString()" +
                unMarshallObject.getDeclaredType().getCanonicalName());
        } catch (JAXBException jaxbEx) {
            throw new JAXBUtilException("Error during unmarshall ", jaxbEx);
        }

        return unMarshallObject;
    }

    private static XMLSerializer getXMLSerializer(Writer strWriter) {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
        // When processing xml that doesn't use namespaces, simply omit the
        // namespace prefix as shown in the third CDataElement below.
        of.setCDataElements(new String[] {
                "ns3^request_xml", // <ns1:foo>
            "ns2^request_xml", // <ns2:bar>
            "^request_xml"
            }); // <baz>

        // set any other options you'd like
        of.setPreserveSpace(true);
        of.setIndenting(true);
        of.setIndent(4);

        // create the serializer
        XMLSerializer serializer = new XMLSerializer(of);

        // serializer.setOutputByteStream(strWriter);
        serializer.setOutputCharStream(strWriter);

        return serializer;
    }
}
