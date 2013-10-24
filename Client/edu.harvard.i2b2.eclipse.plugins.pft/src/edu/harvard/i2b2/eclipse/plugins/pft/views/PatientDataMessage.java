/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.pft.views;

import edu.harvard.i2b2.eclipse.plugins.pft.ws.OntServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.pft.ws.GetCodeInfoResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.pft.ws.PFTJAXBUtil;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.pftclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.pftclient.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.pftclient.datavo.vdo.MatchStrType;
import edu.harvard.i2b2.pftclient.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.pftclient.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.pftclient.datavo.pdo.EncounterIdeType;
import edu.harvard.i2b2.pftclient.datavo.pdo.ObservationFactType;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.pftclient.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.pftclient.datavo.pdo.PatientIdeType;

/**
 * The PatientDataMessage class is a helper class to build
 * PFT messages in the i2b2 format
 * @authors Raj Kuttan, Lori Phillips   
 */


public class PatientDataMessage {
	public static final String THIS_CLASS_NAME = PatientDataMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);
	/**
	 * The constructor
	 */
	public PatientDataMessage() {
	}

	/**
	 * Function to build observation fact type for a given report/observation_blob
	 * 
	 * @param report   String containing report contents to be placed in observation_blob
	 * @return ObservationFactType object
	 */
	public ObservationFactType getObservationFactType(String report) { 
		ObservationFactType obType = new ObservationFactType();
				
		obType.setEncounterNum(new Integer(10000001));
		obType.setPatientNum(new Integer(1234567));
		obType.setConceptCd("LCS-I2B2:pul");
		obType.setProviderId("12345");
		
		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		obType.setStartDate(factory.getXMLGregorianCalendar(currentDate.getTime()));

     	obType.setObservationBlob(report);
		obType.setUpdateDate(factory.getXMLGregorianCalendar(currentDate.getTime()));
		obType.setDownloadDate(factory.getXMLGregorianCalendar(currentDate.getTime()));
		obType.setImportDate(factory.getXMLGregorianCalendar(currentDate.getTime()));

		obType.setSourcesystemCd("RPDRPulmonary");
		
		return obType;
	}

	/**
	 * Function to build patientData body type
	 * 
	 * @param report   String containing report contents to be placed in observation_blob
	 * @return BodyType object
	 */
	
	public BodyType getBodyType(String report) {
		PatientDataType patientData = new PatientDataType();
		PatientDataType.ObservationFactSet obsSet = new PatientDataType.ObservationFactSet();
		obsSet.getObservationFact().add((ObservationFactType) getObservationFactType(report));

		patientData.getObservationFactSet().add(obsSet);
		edu.harvard.i2b2.pftclient.datavo.pdo.ObjectFactory of = new edu.harvard.i2b2.pftclient.datavo.pdo.ObjectFactory();
		
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPatientData(patientData));
		return bodyType;
	}
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
	public MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		
		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.0"));

		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("i2b2 PFT");
		appType.setApplicationVersion("1.0 Demo"); 
		messageHeader.setSendingApplication(appType);
		
		FacilityType facility = new FacilityType();
		facility.setFacilityName("Demonstration Hive");
		messageHeader.setSendingFacility(facility);
		
		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion("1.0 Demo");
		appType2.setApplicationName("PFT Cell");		
		messageHeader.setReceivingApplication(appType2);
	
		FacilityType facility2 = new FacilityType();
		facility2.setFacilityName("Demonstration Hive");
		messageHeader.setReceivingFacility(facility2);

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(0);
		mcIdType.setMessageNum(generateMessageId());
		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId("P");
		proc.setProcessingMode("I");
		messageHeader.setProcessingId(proc);
		
		messageHeader.setAcceptAcknowledgementType("AL");
		messageHeader.setApplicationAcknowledgementType("AL");
		messageHeader.setCountryCode("US");

		return messageHeader;
	}
	
	/**
	 * Function to generate i2b2 message header message number
	 * 
	 * @return String
	 */
	protected String generateMessageId() {
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
	
	/**
	 * Function to convert PFT Request message type to an XML string
	 * 
	 * @param reqMessageType   String containing PFT request message to be converted to string
	 * @return A String data type containing the PFT RequestMessage in XML format
	 */
	private String getXMLString(RequestMessageType reqMessageType)throws Exception{ 
		StringWriter strWriter = null;
		try {
			JAXBUtil jaxbUtil = PFTJAXBUtil.getJAXBUtil();
			strWriter = new StringWriter();
			edu.harvard.i2b2.pftclient.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.pftclient.datavo.i2b2message.ObjectFactory();
			jaxbUtil.marshaller(of.createRequest(reqMessageType), strWriter);
		} catch (JAXBUtilException e) {
			log.error("Error marshalling PFT request message");
			throw new JAXBUtilException(e.getMessage(), e);
		} 
		return strWriter.toString();
	}
	/**
	 * Function to build PFT Request message type and return it as an XML string
	 * 
	 * @param report   String containing report contents to be placed in observation_blob
	 * @return A String data type containing the PFT RequestMessage in XML format
	 */
	public String doBuildXML(String report) throws Exception{ 
		String requestString = null;
			try {
				MessageHeaderType messageHeader = getMessageHeader(); 
				RequestHeaderType reqHeader  = getRequestHeader();
				BodyType bodyType = getBodyType(report) ;
				RequestMessageType reqMessageType = getRequestMessageType(messageHeader,
						reqHeader, bodyType);
				requestString = getXMLString(reqMessageType);
			} catch (JAXBUtilException e) {
				throw new JAXBUtilException(e.getMessage(), e);	
			} 
		return requestString;
	}

	/**
	 * Function to read value, units and concept code from PFT Service response
	 * Then query Ontology service (stub) for name of concept code
	 * 
	 * Place name, value/units and concept code in the PFT result table UI
	 * 
	 * @param response   String containing response returned from PFT web service
	 */
	public void doReadConceptCode(String response) throws Exception {
		try {
			JAXBUtil jaxbUtil = PFTJAXBUtil.getJAXBUtil();
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
			ResponseMessageType respMessageType  = (ResponseMessageType) jaxbElement.getValue();
			
			// Get response message status 
			ResponseHeaderType responseHeader = respMessageType.getResponseHeader();
			String procStatus = responseHeader.getResultStatus().getStatus().getType();
			String procMessage = responseHeader.getResultStatus().getStatus().getValue();
			
			if(procStatus.equals("ERROR")){
				String name="Error reported by PFT web Service";
				String value_units = "";
				String code=procMessage;
				ResultsTab.getInstance().setItem(name, value_units, code);
			}
			else if(procStatus.equals("WARNING")){
				String name="Warning reported by PFT web Service";
				String value_units = "";
				String code=procMessage;
				ResultsTab.getInstance().setItem(name, value_units, code);
			}
			else{
				BodyType bodyType = respMessageType.getMessageBody();
				JAXBUnWrapHelper helper = new JAXBUnWrapHelper(); 
				PatientDataType patientDataType = (PatientDataType)helper.getObjectByClass(bodyType.getAny(), PatientDataType.class);
			
				List<PatientDataType.ObservationFactSet> obsFactSet =  patientDataType.getObservationFactSet();
				if (obsFactSet != null) { 
					if(obsFactSet.get(0).getObservationFact().size() ==0){
						// No PFT results were found by PFT Service
						log.info("No PFT results were found");
			  			String name="No PFT results were found";
						String value_units = "";
						String code="Please verify that the correct report was entered";
						ResultsTab.getInstance().setItem(name, value_units, code);
					}
					else {		
						// Loop through results and place in Results tab
						for(int i = 0; i < (obsFactSet.get(0).getObservationFact().size()); i++) {
							ObservationFactType obsFactType = obsFactSet.get(0).getObservationFact().get(i);
							// Do ontology lookup to map concept code to name
							String conceptName = getCodeInfo(obsFactType);
							
							
//							OntologyServiceLookup lookup = new OntologyServiceLookup();
//							String conceptName = lookup.getConceptName(obsFactType.getConceptCd());
							String value_units = obsFactType.getNvalNum() + " " + obsFactType.getUnitsCd();
							ResultsTab.getInstance().setItem(conceptName, value_units, obsFactType.getConceptCd());
						}
		    			}
				}
				else {
					log.info("No PFT results were found");
					String name="No PFT results were found";
					String value_units = "";
					String code="Please check that the correct report was entered";
					ResultsTab.getInstance().setItem(name, value_units, code);
				}
			}
		} catch (Exception e) {
			log.error("Error unmarshalling PFT response");
			String name="No PFT results were found";
			String value_units = "";
			String code="Please check that PFT web service is running";
			ResultsTab.getInstance().setItem(name, value_units, code);
			throw new Exception(e.getMessage());
		}
	}

//	
//	public Thread getCodeInfo(ObservationFactType observation) {
//		final ObservationFactType theObservation = observation;
//		final ResultsTab theResultsTab = ResultsTab.getInstance();
//		final String theName= null;
//		final Display theDisplay = Display.getCurrent();
//		return new Thread() {
//			public void run(){
//				try {
//					theName = updateCodeInfo(theObservation);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					System.setProperty("statusMessage", e.getMessage());					
//				}
//				theDisplay.syncExec(new Runnable() {
//					public void run() {
//		 				theObservation.get
//						String value_units = theObservation.getNvalNum() + " " + theObservation.getUnitsCd();
//						theResultsTab.setItem(theName, value_units, theObservation.getConceptCd());
//						
////						ResultsTab.refresh();
//
//						//	theBrowser.refresh();
//					}
//				});
//			}
//		};
//	}	

	

    
    public String getCodeInfo(ObservationFactType observation) 
    {
    	String conceptName = null;
    	try {
			VocabRequestType request = new VocabRequestType();
			request.setCategory("i2b2");
			MatchStrType match = new MatchStrType();
			match.setStrategy("exact");
			match.setValue(observation.getConceptCd());
			request.setMatchStr(match);
			request.setType("default");

    	    GetCodeInfoResponseMessage msg = new GetCodeInfoResponseMessage();
			StatusType procStatus = null;	
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = OntServiceDriver.getCodeInfo(request);			
				procStatus = msg.processResult(response);
				log.info(procStatus.getType());
				log.info(procStatus.getValue());
				//Error processing goes here
					procStatus.setType("DONE");
			}
			ConceptsType allConcepts = msg.doReadConcepts();   	    
			List concepts = allConcepts.getConcept();
			if(concepts.isEmpty())
				return null;
			conceptName = getNameFromConcept((ConceptType)concepts.get(0));


			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conceptName;
    }
	
	private String getNameFromConcept(ConceptType concept) {
	
		return concept.getName();
		
	}
	
}


		