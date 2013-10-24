package edu.harvard.i2b2.ontology.ws;


import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * PFT client test. 
 */
public class OntologyServiceRESTTest {
    private static EndpointReference targetEPR = new EndpointReference(
            "http://localhost:8080/axis2/rest/OntologyService/getChildren");

    public static OMElement getVersion() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd",
                "tns");

        OMElement method = fac.createOMElement("getVersion", omNs);

        return method;
    }

    public static void doPrint(String response) throws Exception {
        JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
                    "edu.harvard.i2b2.ontology.datavo.vdo",
                    "edu.harvard.i2b2.ontology.datavo.i2b2message"
                });
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
        ResponseMessageType responseMessageType = (ResponseMessageType) jaxbElement.getValue();
        System.out.println("Response Message Number  :" + responseMessageType.getMessageHeader().getMessageControlId().getMessageNum());
    }

    /**
     * Test code to generate a Ont requestVdo for a test sample and convert to
     * OMElement called by main below
     *
     * @param requestVdo
     *            String requestPdo to send to Ont web service
     * @return An OMElement containing the Ont web service requestVdo
     */
    public static OMElement getPFTPayLoad() throws Exception {
        OMElement method = null;

        try {
      //      OMFactory fac = OMAbstractFactory.getOMFactory();
      //      OMNamespace omNs = fac.createOMNamespace("http://i2b2.mgh.harvard.edu/message",
       //             "i2b2");
      //      method = fac.createOMElement("getChildren", omNs);

            StringReader strReader = new StringReader(getOntologyString());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(strReader);

            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement lineItem = builder.getDocumentElement();
            method = lineItem;
       //     method.addChild(lineItem);
        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            // No log because its a thread?
            e.printStackTrace();
            throw new Exception(e);
        }

        return method;
    }

    /**
     * Test code to generate a Ontology requestVdo String 
     * called by main below
     *
     * @return A String containing the Ontology request
     */
    public static String getOntologyString() throws Exception {
        StringBuffer queryStr = new StringBuffer();
        DataInputStream dataStream = new DataInputStream(OntologyServiceRESTTest.class.getResourceAsStream(
                    "OntSample.xml"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    dataStream));
        String singleLine = null;

        while ((singleLine = reader.readLine()) != null) {
            queryStr.append(singleLine + "\n");
        }

        // Log query string
        System.out.println("queryStr " + queryStr);

        return queryStr.toString();
    }

    /**
     * Test code to generate a Ont requestVdo and make
     * a Ont web service call  Response is printed out to console.
     *
     */
    public static void main(String[] args) {
        try {
            OMElement getPft = getPFTPayLoad();
            Options options = new Options();
            options.setTo(targetEPR);

            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_REST,
                Constants.VALUE_TRUE);
            options.setTimeOutInMilliSeconds(5);

            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);

            OMElement result = sender.sendReceive(getPft);

            if (result == null) {
                System.out.println("result is null");
            } else {
                String response = result.getFirstElement().toString();
                System.out.println("response = " + response);
                doPrint(response);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
