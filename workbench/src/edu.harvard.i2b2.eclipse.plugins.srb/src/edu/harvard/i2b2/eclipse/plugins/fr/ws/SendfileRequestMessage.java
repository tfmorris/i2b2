/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 	    Raj Kuttan
 * 		Lori Phillips
 */

package edu.harvard.i2b2.eclipse.plugins.fr.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType;


/**
 * @author Lori Phillips
 *
 */
public class SendfileRequestMessage extends CrcRequestData {
	
	public static final String THIS_CLASS_NAME = SendfileRequestMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	

   // private SendfileRequestType publishDataRequestType;

	public SendfileRequestMessage() {}
	
	/**
	 * Function to build getChildren type for a given request
	 * 
	 * @return SendfileRequestType object
	 */
	public SendfileRequestType getSendfileRequestType() { 
		SendfileRequestType publishDataRequestType = new SendfileRequestType();		
		return publishDataRequestType;
	}
	
	
	/**
	 * Function to build vocabData body type
	 * 
	 * @param 
	 * @return BodyType object
	 */
	
	public BodyType getBodyType() {
		SendfileRequestType publishDataRequestType = getSendfileRequestType();
		edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory of = new edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createSendfileRequest(publishDataRequestType));
		return bodyType;
	}
	
	/**
	 * Function to build vocabData body type
	 * 
	 * @param 
	 * @return BodyType object
	 */
	
	public BodyType getBodyType(SendfileRequestType publishDataRequestType) {
		edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory of = new edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createSendfileRequest(publishDataRequestType));
		return bodyType;
	}

	/**
	 * Function to build Ont Request message type and return it as an XML string
	 * 
	 * @param SendfileRequestType parentData (get children of this parent node)
	 * @return A String data type containing the Ont RequestMessage in XML format
	 */
	public String doBuildXML(SendfileRequestType parentData){ 
		String requestString = null;
			try {
				MessageHeaderType messageHeader = getMessageHeader(); 
				RequestHeaderType reqHeader  = getRequestHeader();
				BodyType bodyType = getBodyType(parentData) ;
				RequestMessageType reqMessageType = getRequestMessageType(messageHeader,
						reqHeader, bodyType);
				requestString = getXMLString(reqMessageType);
			} catch (JAXBUtilException e) {
				log.error(e.getMessage());
			} 
		return requestString;
	}
}


	
	