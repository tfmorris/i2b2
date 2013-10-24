package edu.harvard.i2b2.pm.ws;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;


public class PMClient {

	private static EndpointReference targetEPR = new EndpointReference(
//	"http://phsi2b2appdev.mgh.harvard.edu:9090/axis2/rest/PMService/getServices");	
"http://phsi2b2apptest.mgh.harvard.edu:7070/axis2/rest/PMService/getServices");	

	public static String getQueryString() throws Exception  { 
		StringBuffer queryStr = new StringBuffer();

		DataInputStream dataStream = new DataInputStream(new FileInputStream("pm_client.xml"));
		
		while(dataStream.available()>0) {
			queryStr.append(dataStream.readLine() + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();	
	}
	
	public static ServiceClient getServiceClient() throws Exception {

		Options options = new Options();
		options.setTo(targetEPR);
		
		options.setTimeOutInMilliSeconds(1800000);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(50000));
		options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(50000));

		// Blocking invocation
		//ServiceClient sender = new ServiceClient(configContext, null);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		return sender;
	}
	
	public static OMElement getPmPayLoad2(String requestPm) throws Exception {
		OMElement lineItem = null;
		try {

			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			lineItem = builder.getDocumentElement();
			
		} catch (FactoryConfigurationError e) {
			System.out.println(e.getMessage());
			throw new Exception(e);
		}
		return lineItem;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		try {
			OMElement payload = getPmPayLoad2(getQueryString());
			ServiceClient sender = getServiceClient();
			OMElement result = sender.sendReceive(payload);
			System.out.println(result.toString());
		
		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		}
	}
}
