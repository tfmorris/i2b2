/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Wensong Pan
 *     
 */
package edu.harvard.i2b2.previousquery.data;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.previousquery.datavo.PreviousQueryJAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.ObjectFactory;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.OutputOptionSelectType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.OutputOptionType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.PdoRequestTypeType;


public class PDORequestMessageFactory {

	private PdoQryHeaderType buildHeaderType() { 
		PdoQryHeaderType pdoHeader = new PdoQryHeaderType();
		pdoHeader.setEstimatedTime(180000);
		pdoHeader.setRequestType(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST);
		return pdoHeader;
	}
	
	protected MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		
		messageHeader.setI2B2VersionCompatible(new BigDecimal(Messages.getString("QueryData.i2b2VersionCompatible"))); //$NON-NLS-1$

		ApplicationType appType = new ApplicationType();
		appType.setApplicationName(Messages.getString("QueryData.SendingApplicationName")); //$NON-NLS-1$
		appType.setApplicationVersion(Messages.getString("QueryData.SendingApplicationVersion"));  //$NON-NLS-1$
		messageHeader.setSendingApplication(appType);
		
		messageHeader.setAcceptAcknowledgementType(new String("messageId"));
		
		MessageTypeType messageTypeType = new MessageTypeType();
		messageTypeType.setEventType(Messages.getString("QueryData.EventType"));
		messageTypeType.setMessageCode(Messages.getString("QueryData.MessageCode"));
		messageHeader.setMessageType(messageTypeType);
		
		FacilityType facility = new FacilityType();
		facility.setFacilityName(Messages.getString("QueryData.SendingFacilityName")); //$NON-NLS-1$
		messageHeader.setSendingFacility(facility);
		
		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion(Messages.getString("QueryData.ReceivingApplicationVersion")); //$NON-NLS-1$
		appType2.setApplicationName(Messages.getString("QueryData.ReceivingApplicationName"));		 //$NON-NLS-1$
		messageHeader.setReceivingApplication(appType2);
	
		FacilityType facility2 = new FacilityType();
		facility2.setFacilityName(Messages.getString("QueryData.ReceivingFacilityName")); //$NON-NLS-1$
		messageHeader.setReceivingFacility(facility2);

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));
		
		SecurityType secType = new SecurityType();
		secType.setDomain(UserInfoBean.getInstance().getUserDomain());
		secType.setUsername(UserInfoBean.getInstance().getUserName());
		secType.setPassword(UserInfoBean.getInstance().getUserPassword());
		messageHeader.setSecurity(secType);
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(0);
		mcIdType.setMessageNum(generateMessageId());
		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId(Messages.getString("QueryData.ProcessingId")); //$NON-NLS-1$
		proc.setProcessingMode(Messages.getString("QueryData.ProcessingMode")); //$NON-NLS-1$
		messageHeader.setProcessingId(proc);
		
		messageHeader.setAcceptAcknowledgementType(Messages.getString("QueryData.AcceptAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setApplicationAcknowledgementType(Messages.getString("QueryData.ApplicationAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setCountryCode(Messages.getString("QueryData.CountryCode")); //$NON-NLS-1$
		messageHeader.setProjectId(UserInfoBean.getInstance().getProjectId());
		return messageHeader;
	}
	
	public GetPDOFromInputListRequestType buildPatientSetRequestType(
			String patientSetRefId, Integer min, Integer max, boolean fromFact) {
		
		PatientListType patientListType = new PatientListType();
		if(patientSetRefId.equalsIgnoreCase("-1")) {
			patientListType.setPatientSetCollId(null);
			patientListType.setEntirePatientSet(true);
		}
		else {
			patientListType.setPatientSetCollId(patientSetRefId);
		}
		patientListType.setMin(min);
		patientListType.setMax(max);
		//patientListType.setEntirePatientSet();
		
		//PatientListType.PatientNum patientNum = new PatientListType.PatientNum();
		//patientNum.setIndex(1);
		//patientNum.setValue(344);
		//patientListType.getPatientNum().add(patientNum);
		
		//VisitListType visitListType = new VisitListType();
		//visitListType.setPatientEncCollId(100);
		
		FilterListType filterListType = new FilterListType();
		//filterListType.setConceptList(conceptListType);
		//if(providerPaths.size() > 0) {
		//	filterListType.setObserverList(providerListType);
		//}
				
		OutputOptionType patientOutputOptionType = new OutputOptionType();
		if(fromFact) {
			patientOutputOptionType.setSelect(OutputOptionSelectType.USING_FILTER_LIST);
		}
		else {
			patientOutputOptionType.setSelect(OutputOptionSelectType.USING_INPUT_LIST);
		}
		patientOutputOptionType.setOnlykeys(false);
			
		//FactOutputOptionType factOutputOptionType = new FactOutputOptionType(); 
		//factOutputOptionType.setOnlykeys(false);
		//factOutputOptionType.setBlob(false);
		
		//OutputOptionType visitOutputOptionType = new OutputOptionType();
		//if(fromFact) {
		//	visitOutputOptionType.setSelect(OutputOptionSelectType.USING_FILTER_LIST);
		//}
		//else {
		//	visitOutputOptionType.setSelect("from_input");
		//}
		//visitOutputOptionType.setOnlykeys(false);
		
		OutputOptionListType outputOptionListType = new OutputOptionListType();
		outputOptionListType.setPatientSet(patientOutputOptionType);
		//outputOptionListType.setVisitDimension(visitOutputOptionType);
		//outputOptionListType.setObservationSet(factOutputOptionType);
		
		/*
		GetPDOFromPatientSetRequestType requestType = new GetPDOFromPatientSetRequestType();
		requestType.setPatientList(patientListType);
		requestType.setFilterList(filterListType);
		requestType.setOuputOptionList(outputOptionListType);
		*/
		
		InputOptionListType inputOptionListType = new InputOptionListType();
		inputOptionListType.setPatientList(patientListType);
		//inputOptionListType.setVisitList(visitListType);
		
		GetPDOFromInputListRequestType inputListRequestType = new GetPDOFromInputListRequestType();
		inputListRequestType.setFilterList(filterListType);
		inputListRequestType.setOutputOption(outputOptionListType);
		inputListRequestType.setInputList(inputOptionListType);
		
		return inputListRequestType;
	}
	
	public String  requestXmlMessage(
			String patientSetRefId, Integer min, Integer max, boolean fromFact) throws Exception { 
		PdoQryHeaderType headerType = buildHeaderType();
		//GetPDOFromPatientSetRequestType patientSetRequestType = buildPatientSetRequestType();
		
		GetPDOFromInputListRequestType patientSetRequestType = buildPatientSetRequestType(
				patientSetRefId, min, max, fromFact);
		ObjectFactory obsFactory = new ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(obsFactory.createPdoheader(headerType));
		bodyType.getAny().add(obsFactory.createRequest(patientSetRequestType));
		
		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(900000);
		
		MessageHeaderType messageHeader = getMessageHeader();
		RequestMessageType reqMsgType = new RequestMessageType();
		reqMsgType.setMessageBody(bodyType);
		reqMsgType.setMessageHeader(messageHeader);
		reqMsgType.setRequestHeader(requestHeader);
				
		JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory();
			jaxbUtil.marshaller(of.createRequest(reqMsgType), strWriter);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.print("Request Xml String: " + strWriter.toString());
		return strWriter.toString();
	}
		
	public static void main(String[] args) throws Exception { 
		PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
		/*String conceptPath = new String("\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\CA");
		
		ArrayList<String> paths = new ArrayList<String>();
		paths.add(conceptPath);
		
		conceptPath = new String("\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\GGT");
		paths.add(conceptPath);
		
		ArrayList<String> ppaths = new ArrayList<String>();
		conceptPath = new String("\\Providers\\BWH");
		ppaths.add(conceptPath);*/
		
		pdoFactory.requestXmlMessage("zzp___050206101533684227.xml", new Integer(0), new Integer(10), false);
		//pdoFactory.responseXmlMessage();
		//pdoFactory.requestUnmarshaller(); 
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
