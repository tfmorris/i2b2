/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.pft.ws;

import java.io.DataInputStream;
import java.io.FileInputStream;
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
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.eclipse.UserInfoBean;

public class PFTServiceDriver2 {

	public static final String THIS_CLASS_NAME = PFTServiceDriver2.class.getName();
    private static Log log = LogFactory.getLog(THIS_CLASS_NAME);
	
	//private static EndpointReference testEPR = new EndpointReference(
//		"http://localhost:8080/axis2/rest/PFTService");
	
	/**
	 * Function to send requestPdo to PFT web service
	 * 
	 * @param requestPdo   String requestPdo to send to PFT web service
	 * @return A String containing the PFT web service response 
	 */
	
	public static String callPft(String requestPdo) throws Exception{
		String response = null;
			 try {
				OMElement getPft = getPFTPayLoad(requestPdo);
				Options options = new Options();
				log.info("REST = " + UserInfoBean.getInstance().getCellDataUrl("pft") + "getPulmonaryData");
				options.setTo(new EndpointReference(UserInfoBean.getInstance().getCellDataUrl("pft") + "getPulmonaryData"));
				options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
				options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
				
				ServiceClient sender = new ServiceClient();
				sender.setOptions(options);
				
				OMElement result = sender.sendReceive(getPft);
				response = result.getFirstElement().toString();
				log.debug("PFT response = " + response);
			} catch (AxisFault e) {
				log.error(e.getMessage());
				e.printStackTrace();
				throw new Exception(e);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new Exception(e);
			}
		return response;
	}
	
	
    public static OMElement getVersion() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://axisversion.sample/xsd", "tns");

        OMElement method = fac.createOMElement("getVersion", omNs);

        return method;
    }
	
	
	/**
	 * Function to convert PFT requestPdo to OMElement
	 * 
	 * @param requestPdo   String requestPdo to send to PFT web service
	 * @return An OMElement containing the PFT web service requestPdo
	 */
	public static OMElement getPFTPayLoad(String requestPdo) throws Exception {
		OMElement method  = null;
		try {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace("http://i2b2.mgh.harvard.edu/message", "i2b2");
			method = fac.createOMElement("getPulmonaryData", omNs);
			
			StringReader strReader = new StringReader(requestPdo);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			OMElement lineItem = builder.getDocumentElement();
			method.addChild(lineItem);
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return method;
	}
	

}
