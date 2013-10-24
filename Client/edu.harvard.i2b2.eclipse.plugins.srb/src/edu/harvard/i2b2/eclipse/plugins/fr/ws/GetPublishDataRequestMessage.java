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
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;


/**
 * @author Lori Phillips
 *
 */
public class GetPublishDataRequestMessage extends CrcRequestData {
	
	public static final String THIS_CLASS_NAME = GetPublishDataRequestMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	

   // private PublishDataRequestType publishDataRequestType;

	public GetPublishDataRequestMessage() {}
	
	/**
	 * Function to build getChildren type for a given request
	 * 
	 * @return PublishDataRequestType object
	 */
	public PublishDataRequestType getPublishDataRequestType() { 
		PublishDataRequestType publishDataRequestType = new PublishDataRequestType();		
		return publishDataRequestType;
	}
	
	
	/**
	 * Function to build vocabData body type
	 * 
	 * @param 
	 * @return BodyType object
	 */
	
	public BodyType getBodyType() {
		PublishDataRequestType publishDataRequestType = getPublishDataRequestType();
		edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory of = new edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPublishDataRequest(publishDataRequestType));
		return bodyType;
	}
	
	/**
	 * Function to build vocabData body type
	 * 
	 * @param 
	 * @return BodyType object
	 */
	
	public BodyType getBodyType(PublishDataRequestType publishDataRequestType) {
		edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory of = new edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPublishDataRequest(publishDataRequestType));
		return bodyType;
	}

	/**
	 * Function to build Ont Request message type and return it as an XML string
	 * 
	 * @param PublishDataRequestType parentData (get children of this parent node)
	 * @return A String data type containing the Ont RequestMessage in XML format
	 */
	public String doBuildXML(PublishDataRequestType parentData){ 
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


	
	