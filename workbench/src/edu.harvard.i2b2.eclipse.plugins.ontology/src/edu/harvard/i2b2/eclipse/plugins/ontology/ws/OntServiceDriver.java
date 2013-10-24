/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 *      Raj Kuttan
 */

package edu.harvard.i2b2.eclipse.plugins.ontology.ws;

import java.io.StringReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontclient.datavo.vdo.VocabRequestType;


public class OntServiceDriver {

	public static final String THIS_CLASS_NAME = OntServiceDriver.class.getName();
    private static Log log = LogFactory.getLog(THIS_CLASS_NAME);
    private int result;
    private static String serviceURL = UserInfoBean.getInstance().getCellDataUrl("ont");
	
	private static EndpointReference childrenEPR = new EndpointReference(
//		"http://wxx53142:8080/axis2/rest/OntologyService/getChildren");
//			"http://wxp26488:8080/axis2/rest/OntologyService/getChildren");
			serviceURL + "getChildren");

	private static EndpointReference categoriesEPR = new EndpointReference(
//	"http://wxx53142:8080/axis2/rest/OntologyService/getCategories");
//	"http://wxp26488:8080/axis2/rest/OntologyService/getCategories");
		serviceURL + "getCategories");
	
	private static EndpointReference nameInfoEPR = new EndpointReference(
//	"http://wxx53142:8080/axis2/rest/OntologyService/getNameInfo");
//  "http://phsi2b2appdev:8080/axis2/rest/OntologyService/getNameInfo");
			serviceURL + "getNameInfo");
	
	private static EndpointReference codeInfoEPR = new EndpointReference(
//	"http://wxx53142:8080/axis2/rest/OntologyService/getCodeInfo");
		serviceURL + "getCodeInfo");
	
	private static EndpointReference termInfoEPR = new EndpointReference(
//	"http://wxx53142:8080/axis2/rest/OntologyService/getTermInfo");	
			serviceURL + "getTermInfo");
	
	private static EndpointReference schemesEPR = new EndpointReference(
//			"http://phsi2b2appdev:8080/axis2/rest/OntologyService/getSchemes");	
					serviceURL + "getSchemes");
	
	
    public static OMElement getVersion() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd", "tns");

        OMElement method = fac.createOMElement("getVersion", omNs);

        return method;
    }
	
	/**
	 * Function to send getChildren requestVdo to ONT web service
	 * 
	 * @param GetChildrenType  parentNode we wish to get data for
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getChildren(GetChildrenType parentNode, String type) throws Exception{
		String response = null;
		
			 try {
				 GetChildrenRequestMessage reqMsg = new GetChildrenRequestMessage();

				 String getChildrenRequestString = reqMsg.doBuildXML(parentNode);
				 log.debug(getChildrenRequestString);
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getChildrenRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getChildrenRequestString);
				 }
						 
				OMElement getOnt = getOntPayLoad(getChildrenRequestString);
			
				Options options = new Options();
				
				log.debug(childrenEPR.toString());
				options.setTo(childrenEPR);
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));

				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				log.debug("Ont response = " + response);
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
    
	/**
	 * Function to send getCategories requestVdo to ONT web service
	 * 
	 * @param GetReturnType  return parameters 
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getCategories(GetReturnType returnData, String type) throws Exception {
		String response = null;
			 try {
				 GetCategoriesRequestMessage reqMsg = new GetCategoriesRequestMessage();
				 String getCategoriesRequestString = reqMsg.doBuildXML(returnData);
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getCategoriesRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getCategoriesRequestString);
				 }
				log.debug(getCategoriesRequestString); 
				OMElement getOnt = getOntPayLoad(getCategoriesRequestString);
				
				Options options = new Options();
				log.debug(categoriesEPR.toString());
				options.setTo(categoriesEPR);
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));
				
				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
				log.debug("Ont response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
	/**
	 * Function to send getSchemes requestVdo to ONT web service
	 * 
	 * @param GetReturnType  return parameters 
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getSchemes(GetReturnType returnData, String type) throws Exception{
		String response = null;
			 try {
				 GetSchemesRequestMessage reqMsg = new GetSchemesRequestMessage();
				 String getSchemesRequestString = reqMsg.doBuildXML(returnData);
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getSchemesRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getSchemesRequestString);
				 }	
				log.debug(getSchemesRequestString);
				OMElement getOnt = getOntPayLoad(getSchemesRequestString);
				
				Options options = new Options();

				log.debug(schemesEPR.toString());
				options.setTo(schemesEPR);
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));
				
				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
				log.debug("Ont response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
	
	/**
	 * Function to send getTermInfo requestVdo to ONT web service
	 * 
	 * @param GetTermInfoType  node (self) we wish to get data for
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getTermInfo(GetTermInfoType self, String type) throws Exception{
		String response = null;
			 try {
				 GetTermInfoRequestMessage reqMsg = new GetTermInfoRequestMessage();

				 String getTermInfoRequestString = reqMsg.doBuildXML(self);		
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getTermInfoRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getTermInfoRequestString);
				 }
				log.debug(getTermInfoRequestString);
				OMElement getOnt = getOntPayLoad(getTermInfoRequestString);
				Options options = new Options();

				log.debug(termInfoEPR.toString());
				options.setTo(termInfoEPR);
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));
				
				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
				log.debug("Ont response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
	
	
	/**
	 * Function to send getNameInfo requestVdo to ONT web service
	 * 
	 * @param VocabRequestType  return parameters 
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getNameInfo(VocabRequestType vocabData, String type) throws Exception{
		String response = null;
			 try {
				 GetNameInfoRequestMessage reqMsg = new GetNameInfoRequestMessage();
				 String getNameInfoRequestString = reqMsg.doBuildXML(vocabData);
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getNameInfoRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getNameInfoRequestString);
				 }
				 log.debug(getNameInfoRequestString);
				OMElement getOnt = getOntPayLoad(getNameInfoRequestString);

				Options options = new Options();

				options.setTo(nameInfoEPR);
				log.debug(nameInfoEPR.toString());
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));
				
				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);

				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
				log.debug("Ont response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
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
	public static OMElement getOntPayLoad(String requestVdo) throws Exception {
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
	
	/**
	 * Function to send getCodeInfo requestVdo to ONT web service
	 * 
	 * @param VocabRequestType vocabType we wish to get data for
	 * @return A String containing the ONT web service response 
	 */
	
	public static String getCodeInfo(VocabRequestType vocabType, String type) throws Exception{
		String response = null;
			 try {
				 GetCodeInfoRequestMessage reqMsg = new GetCodeInfoRequestMessage();
				 String getCodeInfoRequestString = reqMsg.doBuildXML(vocabType);
				 if(type != null){
					 if (type.equals("ONT"))
						 System.setProperty("ONT_REQUEST", getCodeInfoRequestString);
					 else
						 System.setProperty("FIND_REQUEST", getCodeInfoRequestString);
				 }
				log.debug(getCodeInfoRequestString);
				OMElement getOnt = getOntPayLoad(getCodeInfoRequestString);
		
				Options options = new Options();

				log.debug(codeInfoEPR.toString());
				options.setTo(codeInfoEPR);
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(125000));
				options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(125000));
				
				ServiceClient sender = OntServiceClient.getServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getOnt);
				response = result.getFirstElement().toString();
				if(type != null){
					if (type.equals("ONT"))
						System.setProperty("ONT_RESPONSE", response);
					else
						System.setProperty("FIND_RESPONSE", response);
				}
				log.debug("Ont response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw new AxisFault(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
}
