/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.fr.ws;

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import net.nbirn.srbclient.utils.MessageUtil;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.xml.*;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetUploadInfoRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.eclipse.UserInfoBean;


public class CrcServiceDriver {

	public static final String THIS_CLASS_NAME = CrcServiceDriver.class.getName();
    private static Log log = LogFactory.getLog(THIS_CLASS_NAME);
    private static String serviceURL = 
    	//"http://infra3.mgh.harvard.edu:9090/i2b2/rest/CRCLoaderService/";
    	UserInfoBean.getInstance().getCellDataUrl("CRC");
	
	private static EndpointReference soapEPR = new EndpointReference(serviceURL);
	
	private static EndpointReference publishDataRequestEPR = new EndpointReference(
			serviceURL + "publishDataRequest");

	private static EndpointReference loadDataStatusRequestEPR = new EndpointReference(
		serviceURL + "getLoadDataStatusRequest");
	
	
	
    public static OMElement getVersion() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd", "tns");

        OMElement method = fac.createOMElement("getVersion", omNs);

        return method;
    }
	
	/**
	 * Function to send getPublishDataRequest requestVdo to CRC web service
	 * 
	 * @param GetPublishDataRequest  parentNode we wish to get data for
	 * @return A String containing the CRC web service response 
	 */
	
	public static String getPublishDataRequest(PublishDataRequestType parentNode, String type) throws Exception{
		String response = null;
		
			 try {
				 GetPublishDataRequestMessage reqMsg = new GetPublishDataRequestMessage();

				 String getChildrenRequestString = reqMsg.doBuildXML(parentNode);
				 log.debug(getChildrenRequestString);
				 				 
				 if(System.getProperty("webServiceMethod").equals("SOAP")) {
					 response = sendSOAP(getChildrenRequestString, "http://rpdr.partners.org/GetChildren", "GetChildren", type );
				 }
				 else {
					 response = sendREST(publishDataRequestEPR, getChildrenRequestString, type);
				 }
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
	/**
	 * Function to send getPublishDataRequest requestVdo to CRC web service
	 * 
	 * @param GetPublishDataRequest  parentNode we wish to get data for
	 * @return A String containing the CRC web service response 
	 */
	
	public static String getLoadDataStatusRequest(GetUploadInfoRequestType parentNode, String type) throws Exception{
		String response = null;
		
			 try {
				 GetLoadDataStatusRequestMessage reqMsg = new GetLoadDataStatusRequestMessage();

				 String getChildrenRequestString = reqMsg.doBuildXML(parentNode);
				 log.debug(getChildrenRequestString);
				 				 
				 if(System.getProperty("webServiceMethod").equals("SOAP")) {
					 response = sendSOAP(getChildrenRequestString, "http://rpdr.partners.org/GetChildren", "GetChildren", type );
				 }
				 else {
					 response = sendREST(loadDataStatusRequestEPR, getChildrenRequestString, type);
				 }
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
    

	/**
	 * Function to convert Ont requestVdo to OMElement
	 * 
	 * @param requestVdo   String requestVdo to send to Ont web service
	 * @return An OMElement containing the Ont web service requestVdo
	 */
	public static OMElement getCrcPayLoad(String requestVdo) throws Exception {
		OMElement lineItem  = null;
		try {
			StringReader strReader = new StringReader(requestVdo);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			lineItem = builder.getDocumentElement();
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return lineItem;
	}
	

	
	public static String sendREST(EndpointReference restEPR, String requestString, String type) throws Exception{	
		OMElement getCrc = getCrcPayLoad(requestString);

		if(type != null){
			if(type.equals("CRC"))
				System.setProperty("CRC_REQUEST", "URL: " + restEPR + "\n" + getCrc.toString());
			else 
				System.setProperty("FIND_REQUEST", "URL: " + restEPR + "\n" + getCrc.toString());
		}
		
		Options options = new Options();
		log.debug(restEPR.toString());
		options.setTo(restEPR);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
		options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));

		ServiceClient sender = FrServiceClient.getServiceClient();
		sender.setOptions(options);

		OMElement result = sender.sendReceive(getCrc);
		String response = result.toString();
		
		if(type != null){
			if(type.equals("CRC"))
				System.setProperty("CRC_RESPONSE", "URL: " + restEPR + "\n" + response);
			else 
				System.setProperty("FIND_RESPONSE", "URL: " + restEPR + "\n" + response);
		}
		
		MessageUtil.getInstance().setRequest("URL: " + restEPR + "\n" + requestString);
        MessageUtil.getInstance().setResponse("URL: " + restEPR + "\n" + response);
		return response;

	}
	
	public static String sendSOAP(String requestString, String action, String operation, String type) throws Exception{	

		ServiceClient sender = FrServiceClient.getServiceClient();
		OperationClient operationClient = sender
				.createClient(ServiceClient.ANON_OUT_IN_OP);

		// creating message context
		MessageContext outMsgCtx = new MessageContext();
		// assigning message context's option object into instance variable
		Options opts = outMsgCtx.getOptions();
		// setting properties into option
		log.debug(soapEPR);
		opts.setTo(soapEPR);
		opts.setAction(action);
		opts.setTimeOutInMilliSeconds(180000);
		
		log.debug(requestString);

		SOAPEnvelope envelope = null;
		
		try {
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			envelope = fac.getDefaultEnvelope();
			OMNamespace omNs = fac.createOMNamespace(
					"http://rpdr.partners.org/",                                   
					"rpdr");

			
			// creating the SOAP payload
			OMElement method = fac.createOMElement(operation, omNs);
			OMElement value = fac.createOMElement("RequestXmlString", omNs);
			value.setText(requestString);
			method.addChild(value);
			envelope.getBody().addChild(method);
		}
		catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
 
		outMsgCtx.setEnvelope(envelope);
		
		// used to be envelope.getBody().getFirstElement().toString()
		if(type != null){
			String request = envelope.toString();
			String formattedRequest = XMLUtil.StrFindAndReplace("&lt;", "<", request);
			if (type.equals("CRC")){
				System.setProperty("CRC_REQUEST", "URL: " + soapEPR + "\n" + formattedRequest);
			}

			else {
				System.setProperty("FIND_REQUEST", "URL: " + soapEPR + "\n" + formattedRequest);
			}
		}
		
		operationClient.addMessageContext(outMsgCtx);
		operationClient.execute(true);
		
		
		MessageContext inMsgtCtx = operationClient.getMessageContext("In");
		SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();
		
		OMElement soapResponse = responseEnv.getBody().getFirstElement();
		
		if(type != null){
			if(type.equals("CRC")){
				String formattedResponse = XMLUtil.StrFindAndReplace("&lt;", "<", responseEnv.toString());
				String indentedResponse = XMLUtil.convertDOMToString(XMLUtil.convertStringToDOM(formattedResponse) );
				System.setProperty("CRC_RESPONSE", "URL: " + soapEPR + "\n" + indentedResponse );
			}else{
				String formattedResponse = XMLUtil.StrFindAndReplace("&lt;", "<", responseEnv.toString());
				String indentedResponse = XMLUtil.convertDOMToString(XMLUtil.convertStringToDOM(formattedResponse) );
				System.setProperty("FIND_RESPONSE", "URL: " + soapEPR + "\n" + indentedResponse);
			}
		}
		
		OMElement soapResult = soapResponse.getFirstElement();

		String i2b2Response = soapResult.getText();
		log.debug(i2b2Response);
		MessageUtil.getInstance().setRequest("URL: " + soapEPR + "\n" + requestString);
        MessageUtil.getInstance().setResponse("URL: " + soapEPR + "\n" + i2b2Response);

		return i2b2Response;		
	}
	
}
