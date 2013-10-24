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

import javax.activation.FileDataSource;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/*
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
 */

import net.nbirn.srbclient.utils.MessageUtil;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.fr.datavo.fr.query.RecvfileRequestType;
import edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType;


public class FrServiceDriver {

	public static final String THIS_CLASS_NAME = FrServiceDriver.class.getName();
	private static Log log = LogFactory.getLog(THIS_CLASS_NAME);

	private static String serviceURL = UserInfoBean.getInstance().getCellDataUrl("FRC");

	public static OMElement getVersion() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd", "tns");

		OMElement method = fac.createOMElement("getVersion", omNs);

		return method;
	}

	/**
	 * Function to send getSendfileRequest requestVdo to CRC web service
	 * 
	 * @param GetSendfileRequest  parentNode we wish to get data for
	 * @return A String containing the CRC web service response 
	 */

	public static String getSendfileRequest(String[] attachmentfiles, SendfileRequestType parentNode, String type) throws Exception{
		String response = null;

		try {


			SendfileRequestMessage reqMsg = new SendfileRequestMessage();

			String getChildrenRequestString = reqMsg.doBuildXML(parentNode);
			log.debug(getChildrenRequestString);

			response = sendSOAP(attachmentfiles,  getChildrenRequestString, type);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return response;
	}

	/**
	 * Function to send getSendfileRequest requestVdo to CRC web service
	 * 
	 * @param GetSendfileRequest  parentNode we wish to get data for
	 * @return A String containing the CRC web service response 
	 */

	public static String getRecvfileRequest(RecvfileRequestType parentNode,  String type) throws Exception{
		String response = null;

		try {
			RecvfileRequestMessage reqMsg = new RecvfileRequestMessage();

			String getChildrenRequestString = reqMsg.doBuildXML(parentNode);
			log.debug(getChildrenRequestString);

			response = sendSOAP(null,  getChildrenRequestString, type);
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
	public static OMElement getFrPayLoad(String requestVdo) throws Exception {
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


	public static OperationClient createOperationClient() throws Exception
	{
		/*
		Options options = new Options();
//		options.setTo( restEPR);
		String serviceUrl = "http://phsi2b2appdev:8080/i2b2/services/FRService";
		options.setTo( new EndpointReference(serviceUrl));


		options.setAction("urn:sendfileRequest");
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);


		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(10000);

		options.setProperty(Constants.Configuration.ENABLE_SWA,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,"temp");
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");
		 */

		ServiceClient sender = FrServiceClient.getServiceClient();
		//sender.setOptions(options);

		return sender.createClient(ServiceClient.ANON_OUT_IN_OP);
	}

	public static String sendSOAP(String[] attachmentfiles,  String requestString, String type) throws Exception{	

		/*
		if(type != null){
			if(type.equals("FR"))
				System.setProperty("CRC_REQUEST", "URL: " + restEPR + "\n" + getCrc.toString());
			else 
				System.setProperty("FIND_REQUEST", "URL: " + restEPR + "\n" + getCrc.toString());
		}
		 */
		MessageContext response = null;


		OMElement getCrc = getFrPayLoad(requestString);




		Options options = new Options();
		//			options.setTo( restEPR);
		//String serviceUrl = "http://phsi2b2appdev:8080/i2b2/services/FRService";
		if (serviceURL.endsWith("/"))
			serviceURL = serviceURL.substring(0, serviceURL.length()-1);
		
		options.setTo( new EndpointReference(serviceURL));


		options.setAction("urn:sendfileRequest");
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);


		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(10000);

		options.setProperty(Constants.Configuration.ENABLE_SWA,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,"temp");
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");


		ServiceClient sender = FrServiceClient.getServiceClient();
		sender.setOptions(options);

		OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);


		MessageContext mc = new MessageContext();

		for (int i=0; i < attachmentfiles.length; i++) {
			javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(attachmentfiles[i]));

			mc.addAttachment("cid",dataHandler);
		}
		mc.setDoingSwA(true);

		//	OMElement requestElement = convertStringToOMElement(requestString);
		SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope env = sfac.getDefaultEnvelope();

		/*OMNamespace omNs = sfac.createOMNamespace(
				"http://www.i2b2.org/xsd", "swa");
		OMElement idEle = sfac.createOMElement("attchmentID", omNs);
		idEle.setText("cid");

		env.getBody().addChild(idEle);
		 */
		env.getBody().addChild(getCrc);

		// SOAPEnvelope env = createEnvelope("fileattachment");
		mc.setEnvelope(env);
		mepClient.addMessageContext(mc);
		mepClient.execute(true);

		MessageContext inMsgtCtx = mepClient.getMessageContext("In");
		SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();
		
		OMElement soapResponse = responseEnv.getBody().getFirstElement();
		
		OMElement soapResult = soapResponse.getFirstElement();

		String i2b2Response = soapResponse.toString();

		MessageUtil.getInstance().setRequest("URL: " + serviceURL + "\n" + requestString);
        MessageUtil.getInstance().setResponse("URL: " + serviceURL + "\n" + response);


		return soapResponse.toString();
		//mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope().toStringWithConsume();

	}
	/*
	public static OMElement convertStringToOMElement(String requestXmlString) throws Exception { 
		StringReader strReader = new StringReader(requestXmlString);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = xif.createXMLStreamReader(strReader);

        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement lineItem = builder.getDocumentElement();
        return lineItem;
	}
	 */
}
