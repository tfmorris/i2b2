/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

/**
 *  Class: QueryData
 *  
 *  An abstract and parent class for the querytool query related data.
 */
package edu.harvard.i2b2.query;

import java.io.StringWriter;

//import edu.harvard.i2b2.previousquery.datavo.i2b2message.CommonMessageHeader;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.eclipse.UserInfoBean;

/**
 * @author wp066
 *
 */
abstract public class QueryData {
	private String name;
	public void name(String str) {name = str;}
	public String name() {return name;}
	
	private String id;
	public void id(String str) {id = str;}
	public String id() {return id;}
	
	private String userId;
	public void userId(String str) {userId = str;}
	public String userId() {return userId;}
	
	private String visualAttribute;
	public void visualAttribute(String str) {visualAttribute = new String(str);}
	public String visualAttribute() {return visualAttribute;}
	
	private String tooltip;
	public void tooltip(String str) {tooltip = new String(str);}
	public String tooltip() {return tooltip;}
	
	private String xmlContent;
	public void xmlContent(String str) {xmlContent = str;}
	public String xmlContent() {return xmlContent;}
	
	public QueryData() {}
	
	@Override
	public String toString() {
		return name;
	}
	
	abstract public String writeContentQueryXML();
	
	protected MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		messageHeader.setAcceptAcknowledgementType(new String("messageId"));
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		//mcIdType.setInstanceNum(1);
		mcIdType.setMessageNum(generateMessageId());
		//mcIdType.setSessionId("1");
		messageHeader.setMessageControlId(mcIdType);
		
		MessageTypeType messageTypeType = new MessageTypeType();
		messageTypeType.setEventType("EQQ");
		messageTypeType.setMessageCode("Q04");
		messageHeader.setMessageType(messageTypeType);
		
		ApplicationType sendAppType = new ApplicationType();
		sendAppType.setApplicationName("i2b2_QueryTool");
		sendAppType.setApplicationVersion("0.2"); 
		messageHeader.setSendingApplication(sendAppType);
		
		ApplicationType receiveAppType = new ApplicationType();
		receiveAppType.setApplicationName("i2b2_DataRepositoryCell");
		receiveAppType.setApplicationVersion("0.2"); 
		messageHeader.setReceivingApplication(receiveAppType);
		
		FacilityType facType = new FacilityType();
		facType.setFacilityName("PHS");
		messageHeader.setSendingFacility(facType);
		messageHeader.setReceivingFacility(facType);
		
		SecurityType secType = new SecurityType();
		secType.setDomain(System.getProperty("projectName"));
		secType.setUsername(UserInfoBean.getInstance().getUserName());
		secType.setPassword(UserInfoBean.getInstance().getUserPassword());
		messageHeader.setSecurity(secType);
		
		ProcessingIdType procIdType = new ProcessingIdType();
		procIdType.setProcessingId("P");
		procIdType.setProcessingMode("I");
		messageHeader.setProcessingId(procIdType);
		
		return messageHeader;
	}
	
	protected String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			//System.out.println("Generated number: " + num + " char: "+(char)num);
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}
	
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
}
