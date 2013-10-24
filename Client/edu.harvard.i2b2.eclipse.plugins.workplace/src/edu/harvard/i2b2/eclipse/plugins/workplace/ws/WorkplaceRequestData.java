/*
 * Copyright (c) 2006-2010 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */

package edu.harvard.i2b2.eclipse.plugins.workplace.ws;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.plugins.workplace.util.Messages;
import edu.harvard.i2b2.eclipse.plugins.workplace.util.WorkplaceJAXBUtil;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.wkplclient.datavo.i2b2message.SecurityType;

abstract public class WorkplaceRequestData {

	public static final String THIS_CLASS_NAME = WorkplaceRequestData.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);
	public WorkplaceRequestData() {}

	/**
	 * Function to build i2b2 Request message header
	 * 
	 * @return RequestHeader object
	 */
	public RequestHeaderType getRequestHeader() { 
		RequestHeaderType reqHeader = new RequestHeaderType();
		reqHeader.setResultWaittimeMs(120000);
		return reqHeader;
	}
	
	/**
	 * Function to build i2b2 message header
	 * 
	 * @return MessageHeader object
	 */

	protected MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		
		messageHeader.setI2B2VersionCompatible(new BigDecimal(Messages.getString("WorkplaceRequestData.i2b2VersionCompatible"))); //$NON-NLS-1$

		ApplicationType appType = new ApplicationType();
		appType.setApplicationName(Messages.getString("WorkplaceRequestData.SendingApplicationName")); //$NON-NLS-1$
		appType.setApplicationVersion(Messages.getString("WorkplaceRequestData.SendingApplicationVersion"));  //$NON-NLS-1$
		messageHeader.setSendingApplication(appType);
		
		messageHeader.setAcceptAcknowledgementType(new String("messageId"));
		
		FacilityType facility = new FacilityType();
		facility.setFacilityName(Messages.getString("WorkplaceRequestData.SendingFacilityName")); //$NON-NLS-1$
		messageHeader.setSendingFacility(facility);
		
		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion(Messages.getString("WorkplaceRequestData.ReceivingApplicationVersion")); //$NON-NLS-1$
		appType2.setApplicationName(Messages.getString("WorkplaceRequestData.ReceivingApplicationVersion"));		 //$NON-NLS-1$
		messageHeader.setReceivingApplication(appType2);
	
		FacilityType facility2 = new FacilityType();
		facility2.setFacilityName(Messages.getString("WorkplaceRequestData.ReceivingFacilityName")); //$NON-NLS-1$
		messageHeader.setReceivingFacility(facility2);

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));
		
		SecurityType secType = new SecurityType();
		secType.setDomain(UserInfoBean.getInstance().getUserDomain());
		secType.setUsername(UserInfoBean.getInstance().getUserName());

		PasswordType ptype = new PasswordType();
		ptype.setIsToken(UserInfoBean.getInstance().getUserPasswordIsToken());
		ptype.setTokenMsTimeout(UserInfoBean.getInstance().getUserPasswordTimeout());
		ptype.setValue(UserInfoBean.getInstance().getUserPassword());
		

		secType.setPassword(ptype);
		messageHeader.setSecurity(secType);
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(0);
		mcIdType.setMessageNum(generateMessageId());
		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId(Messages.getString("WorkplaceRequestData.ProcessingId")); //$NON-NLS-1$
		proc.setProcessingMode(Messages.getString("WorkplaceRequestData.ProcessingMode")); //$NON-NLS-1$
		messageHeader.setProcessingId(proc);
		
		messageHeader.setAcceptAcknowledgementType(Messages.getString("WorkplaceRequestData.AcceptAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setApplicationAcknowledgementType(Messages.getString("WorkplaceRequestData.ApplicationAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setCountryCode(Messages.getString("WorkplaceRequestData.CountryCode")); //$NON-NLS-1$
		messageHeader.setProjectId(UserInfoBean.getInstance().getProjectId());
		return messageHeader;
	}
	
	/**
	 * Function to generate i2b2 message header message number
	 * 
	 * @return String
	 */
	public String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}
	
	/**
	 * Function to generate random number used in message number
	 * 
	 * @return int 
	 */
	private int getValidAcsiiValue() {
		int number = 48;
		while(true) {
			number = 48+(int) Math.round(Math.random() * 74);
			if((number > 47 && number < 58) || (number > 64 && number < 91) 
				|| (number > 96 && number < 123)) {
					break;
				}
		}
		return number;
	}
	/**
	 * Function to convert Workplace Request message type to an XML string
	 * 
	 * @param reqMessageType   String containing Workplace request message to be converted to string
	 * @return A String data type containing the Workplace RequestMessage in XML format
	 */
	public String getXMLString(RequestMessageType reqMessageType) throws JAXBUtilException{ 
		StringWriter strWriter = null;
		try {
			strWriter = new StringWriter();
			edu.harvard.i2b2.wkplclient.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.wkplclient.datavo.i2b2message.ObjectFactory();
			WorkplaceJAXBUtil.getJAXBUtil().marshaller(of.createRequest(reqMessageType), strWriter);
		} catch (JAXBUtilException e) {
			log.error("Error marshalling Workplace request message");
			throw e;
		} 
		return strWriter.toString();
	}

	
	
	/**
	 * Function to build Request message type
	 * 
	 * @param messageHeader MessageHeader object  
	 * @param reqHeader     RequestHeader object
	 * @param bodyType      BodyType object 
	 * @return RequestMessageType object
	 */
	public RequestMessageType getRequestMessageType(MessageHeaderType messageHeader,
			RequestHeaderType reqHeader, BodyType bodyType) { 
		RequestMessageType reqMsgType = new RequestMessageType();
		reqMsgType.setMessageHeader(messageHeader);
		reqMsgType.setMessageBody(bodyType);
		reqMsgType.setRequestHeader(reqHeader);
		return reqMsgType;
	}
	
}