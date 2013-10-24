/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.pm;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


public class PMServiceDriver {
	private static Log log = LogFactory.getLog(PMServiceDriver.class);

	/**
	 * Function to convert pm requestVdo to OMElement
	 * 
	 * @param requestPm   String request to send to pm web service
	 * @return An OMElement containing the pm web service requestVdo
	 */
	public  OMElement getPmPayLoad(String requestPm) throws I2B2Exception {
		OMElement method  = null;
		try {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			//OMNamespace omNs = fac.createOMNamespace("http://www.i2b2.org/xsd/hive/msg",
			//"i2b2");
			//method = fac.createOMElement("request", omNs);
			StringReader strReader = new StringReader(requestPm);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			method = builder.getDocumentElement();
			
		} catch (FactoryConfigurationError e) {
			log.error(e.getMessage());
			throw new I2B2Exception("",e.getException());
		} catch (XMLStreamException e) {
			log.error(e.getMessage());
			throw new I2B2Exception("",e);
		}
		return method;
	}
	/**
	 * Function to send getRoles request to PM web service
	 * 
	 * @param GetUserConfigurationType  userConfig we wish to get data for
	 * @return A String containing the PM web service response 
	 */
	public  String getRoles1( SecurityType userSec) throws AxisFault,I2B2Exception {
		String response = null;	
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
			String getRolesRequestString = reqMsg.doBuildXML(new GetUserConfigurationType(), userSec);
			OMElement getPm = getPmPayLoad(getRolesRequestString);
			String pmEPR = "";
			try {
				pmEPR = QueryProcessorUtil.getInstance().getProjectManagementCellUrl();
				log.debug("project management cell URL " + pmEPR);
			} catch (I2B2Exception e1) {
				e1.printStackTrace();
				log.error("Error getting project management cell URL " + e1.getMessage());
			}
			Options options = new Options();
			options.setTo( new EndpointReference(pmEPR));
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setTimeOutInMilliSeconds(50000);
			ServiceClient sender = PMServiceClient.getServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(getPm);
			if (result != null) {
				response = result.toString();
				log.info(response);
				
			}
			sender.cleanup();
		} catch (AxisFault e) {
			log.error(e);
			throw e;
	//	} catch (Exception e) {
	//		log.error(e);
	//		throw e;
		}
		return response;
	}
	
	
	 public static ProjectType checkValidUser(SecurityType security) throws I2B2Exception,AxisFault  
	    {
	    	ProjectType projectType = null;
	    	
			// Are we bypassing the PM cell?  Look in properties file.
			Boolean pmBypass = false;
			String pmBypassRole = null;
			String pmBypassProject = null;
			String response = null;
			try {
				pmBypass = QueryProcessorUtil.getInstance().getProjectManagementByPassFlag();
				pmBypassRole = QueryProcessorUtil.getInstance().getProjectManagementByPassRole();
				log.info(pmBypass + pmBypassRole + pmBypassProject);
			} catch (I2B2Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1.getMessage());
			}
	    	
			if(pmBypass == true){
				projectType = new ProjectType();
				projectType.getRole().add(pmBypassRole);
				
			}
			else {
				try {
					GetUserConfigurationType userConfigType = new GetUserConfigurationType();
					PMResponseMessage msg = new PMResponseMessage();
					log.info("calling PM service!!!!!!!!!!!!!!");
					response = new PMServiceDriver().getRoles1( security);
					log.info("PM response" + response);
					
					StatusType procStatus = msg.processResult(response);
	            	if (procStatus.getType().equalsIgnoreCase("ERROR")) { 
	            		projectType = null;
	            		
	            	}
	            	else { 
					ConfigureType pmResponseUserInfo = msg.readUserInfo();
					projectType = pmResponseUserInfo.getUser().getProject().get(0);
	            	}
					//StatusType procStatus = msg.processResult(response);
					
					//ResponseType pmResponse = msg.readUserInfo();
					//projectType = pmResponse.getConfigure().getUser().getProject().get(0);
				} catch (Exception e) {
					e.printStackTrace();
					projectType = null;
				}
			}
			return projectType;
	    }
	
	
}