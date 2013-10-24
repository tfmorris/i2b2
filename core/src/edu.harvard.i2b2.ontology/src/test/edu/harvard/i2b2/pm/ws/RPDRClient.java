package edu.harvard.i2b2.pm.ws;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

import edu.harvard.i2b2.ontology.datavo.pm.ConfigureType;

public class RPDRClient {
	private static EndpointReference targetEPR = new EndpointReference(
			"https://localhost/RPDRServices/RpdrProjectManagementService.asmx"
		);
	
	
	public static OMElement getPmPayLoad() throws Exception {
		StringReader strReader = new StringReader(getQueryString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		System.out.println("Line item string " + lineItem.toString());
		return lineItem;
	}


	
	public static ServiceClient getServiceClient() throws Exception {


		Options options = new Options();
		options.setTo(targetEPR);
		
		options.setTimeOutInMilliSeconds(1800000);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		
		// Blocking invocation
		//ServiceClient sender = new ServiceClient(configContext, null);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		return sender;
	}
	
	public static OMElement getPmPayLoad2(String requestPm) throws Exception {
		OMElement method  = null;
	//	OMElement lineItem = null;
		try {

			// this should not be necessary.....
			// will come out later?
			
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace("http://www.i2b2.org/xsd/hive/msg",
			"i2b2");

			method = fac.createOMElement("request", omNs);

			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
	//		method = builder.getDocumentElement();
			OMElement lineItem = builder.getDocumentElement();
			method.addChild(lineItem);
		} catch (FactoryConfigurationError e) {
			System.out.println(e.getMessage());
			throw new Exception(e);
		}
		return method;
//		return lineItem;
	}



public static String getQueryString() throws Exception  { 
	StringBuffer queryStr = new StringBuffer();
	DataInputStream dataStream = new DataInputStream(new FileInputStream("rpdr/rpdr_client.xml"));  //reqTest.xml
	while(dataStream.available()>0) {
		queryStr.append(dataStream.readLine());// + "\n");
	}
	//System.out.println("queryStr: " + queryStr);
	return 
      queryStr.toString();

}

/*public static String generateQueryString() throws Exception {

	 getCategories request
* 		GetReturnType data = new GetReturnType();
	data.setType("core");
	data.setBlob(false);
	
	// getChildren request
	 //		GetChildrenType data = new GetChildrenType();
	//		data.setType("core");
	//		data.setBlob(false);
	//		data.setParent("\\\\i2b2\\RPDR\\Diagnosis\\Circulatory system (390-459)");
	
	// getTermInfo request
	  		GetTermInfoType data = new GetTermInfoType();
			data.setType("core");
			data.setBlob(false);
			data.setSelf("\\\\i2b2\\RPDR\\drg\\(20) Alcohol and Drug Abuse");
	
			// getNameInfo request
			  		VocabRequestType data = new VocabRequestType();
					data.setCategory("diagnosis");
					data.setBlob(false);
				    data.setMax(100);
				    MatchStrType match = new MatchStrType();
				    match.setStrategy("contains");
				    match.setValue("asthma");
				    data.setMatchStr(match);
		

	
	String requestString = null;
	
//	GetCategoriesRequestMessage reqMsg = new GetCategoriesRequestMessage();
//	GetChildrenRequestMessage reqMsg = new GetChildrenRequestMessage();
//	GetTermInfoRequestMessage reqMsg = new GetTermInfoRequestMessage();
	GetNameInfoRequestMessage reqMsg = new GetNameInfoRequestMessage();
					try {
						MessageHeaderType messageHeader = reqMsg.getMessageHeader(); 
						messageHeader.getSecurity().setDomain("demo");			 
						messageHeader.getSecurity().setUsername("demo");
						messageHeader.getSecurity().setPassword("demouser");
						RequestHeaderType reqHeader  = reqMsg.getRequestHeader();
						BodyType bodyType = reqMsg.getBodyType(data) ;
						RequestMessageType reqMessageType = reqMsg.getRequestMessageType(messageHeader,
								reqHeader, bodyType);
						requestString = reqMsg.getXMLString(reqMessageType);
					} catch (JAXBUtilException e) {
						System.out.println(e.getMessage());
					} 
				 

	//	System.out.println(getCategoriesRequestString); 
		return requestString;
	}*/
	


public static SOAPEnvelope createSOAPEnvelope(String xmlStr) {
	SOAPEnvelope envelope = null;

	try {
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		envelope = fac.getDefaultEnvelope();
		OMNamespace omNs = fac.createOMNamespace(
				"http://rpdr.partners.org/",                                   
				"rpdr");
		// creating the payload
		OMElement method = fac.createOMElement("GetUserConfiguration", omNs);
		OMElement value = fac.createOMElement("RequestXmlString", omNs);
		value.setText(xmlStr);
		method.addChild(value);
		envelope.getBody().addChild(method);
	}
	catch (FactoryConfigurationError e) {
	
		e.printStackTrace();
		return envelope;
		//log.error(e.getMessage());
		//throw new Exception(e);
	}
	
	return envelope;
}

public static void doPrint(String response) throws Exception {
    JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
                "edu.harvard.i2b2.ontology.datavo.pm",
                "edu.harvard.i2b2.ontology.datavo.i2b2message"
            });
    JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
    ResponseMessageType responseMessageType = (ResponseMessageType) jaxbElement.getValue();
    System.out.println("Response Message Number  :" + responseMessageType.getMessageHeader().getMessageControlId().getMessageNum());
    PMResponseMessage msg = new PMResponseMessage();
    StatusType procStatus = msg.processResult(response);
	System.out.println(procStatus.getType());

	ConfigureType pmConfigure = msg.readUserInfo();
	ProjectType project = pmConfigure.getUser().getProject().get(0);
	System.out.println(project.getName());



}
	
	public static void main(String[] args) throws Exception {
		ServiceClient client = new ServiceClient();
		OperationClient operationClient = client
				.createClient(ServiceClient.ANON_OUT_IN_OP);
		
		// creating message context
		MessageContext outMsgCtx = new MessageContext();
		// assigning message context's option object into instance variable
		Options opts = outMsgCtx.getOptions();
		// setting properties into option
		opts.setTo(targetEPR);
		opts.setAction("http://rpdr.partners.org/GetUserConfiguration");
		opts.setTimeOutInMilliSeconds(180000);
		
		SOAPEnvelope request = createSOAPEnvelope(getQueryString());
//		SOAPEnvelope request = createSOAPEnvelope(generateQueryString());
		System.out.println("request: "+ request);
		outMsgCtx.setEnvelope(request);
		
		operationClient.addMessageContext(outMsgCtx);
		operationClient.execute(true);
		
		MessageContext inMsgtCtx = operationClient.getMessageContext("In");
		SOAPEnvelope response = inMsgtCtx.getEnvelope();
//		System.out.println("response: "+response.getBody().getFirstElement().toStringWithConsume());
		
		OMElement soapResponse = response.getBody().getFirstElement();
		System.out.println("Sresponse: "+ soapResponse.toString());
		OMElement soapResult = soapResponse.getFirstElement();
		System.out.println("Sresult: "+ soapResult.toString());

		String i2b2Response = soapResult.getText();
		doPrint(i2b2Response);
	}
	
	
}
