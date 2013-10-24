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

package edu.harvard.i2b2.exportData.data;

import java.util.List;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.PatientDataResponseType;

import edu.harvard.i2b2.common.datavo.pdo.EventSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.common.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.common.datavo.pdo.PatientSet;
import edu.harvard.i2b2.common.datavo.pdo.PidSet;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.exportData.datavo.ImageExplorerJAXBUtil;

public class PDOResponseMessageFactory {
    public StatusType getStatusFromResponseXML(String responseXML)
	    throws Exception {
	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	StatusType statusType = messageType.getResponseHeader()
		.getResultStatus().getStatus();

	return statusType;
    }

    public List<ObservationSet> getFactSetsFromResponseXML(String responseXML)
	    throws Exception {
	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	BodyType bodyType = messageType.getMessageBody();
	PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
		.getObjectByClass(bodyType.getAny(),
			PatientDataResponseType.class);
	PatientDataType patientDataType = responseType.getPatientData();
	// (PatientDataType) new
	//JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.
	// class);

	return patientDataType.getObservationSet();
    }

    public PatientDataType getPatientDataTypeFromResponseXML(String responseXML)
	    throws Exception {
	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	BodyType bodyType = messageType.getMessageBody();

	PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
		.getObjectByClass(bodyType.getAny(),
			PatientDataResponseType.class);
	PatientDataType patientDataType = responseType.getPatientData();
	// (PatientDataType) new
	//JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.
	// class);

	return patientDataType;
    }

    public PatientSet getPatientSetFromResponseXML(String responseXML)
	    throws Exception {
	// System.out.println(responseXML);

	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	BodyType bodyType = messageType.getMessageBody();
	PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
		.getObjectByClass(bodyType.getAny(),
			PatientDataResponseType.class);
	PatientDataType patientDataType = responseType.getPatientData();
	// (PatientDataType) new
	//JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.
	// class);

	PatientSet patientFactSet = patientDataType.getPatientSet();

	return patientFactSet;
    }

    public PidSet getPidSetFromResponseXML(String responseXML) throws Exception {
	// System.out.println(responseXML);

	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	BodyType bodyType = messageType.getMessageBody();
	PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
		.getObjectByClass(bodyType.getAny(),
			PatientDataResponseType.class);
	PatientDataType patientDataType = responseType.getPatientData();
	// (PatientDataType) new
	//JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.
	// class);

	PidSet pidSet = patientDataType.getPidSet();

	return pidSet;
    }

    public EventSet getEventSetFromResponseXML(String responseXML)
	    throws Exception {
	JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
	ResponseMessageType messageType = (ResponseMessageType) jaxbElement
		.getValue();
	BodyType bodyType = messageType.getMessageBody();
	PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
		.getObjectByClass(bodyType.getAny(),
			PatientDataResponseType.class);
	PatientDataType patientDataType = responseType.getPatientData();

	EventSet eventSet = patientDataType.getEventSet();

	return eventSet;
    }

    public static void main(String args[]) throws Exception {
	// PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
	// String xmlrequest =
	// pdoFactory.requestXmlMessage("zzp___050206101533684227.xml", new
	// Integer(0), new Integer(25), false);
	// System.out.println("Request: "+xmlrequest);
	// String response = PDOQueryClient.sendPDOQueryRequestSOAP(xmlrequest);
	// System.out.println("Response: "+response);
	// PatientSet patientSet = new
	// PDOResponseMessageFactory().getPatientSetFromResponseXML(response);
	// System.out.println("Patient set size:
	// "+patientSet.getPatient().size());
    }
}