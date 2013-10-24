/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   	
 * 		Wensong Pan
 *      
 */
package edu.harvard.i2b2.timeline.lifelines;

import java.util.List;

import javax.xml.bind.JAXBElement;

//import edu.harvard.i2b2.querytool.datavo.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.explorer.datavo.ExplorerJAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ResponseMessageType;
//import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.PatientDataJAXBUnWrapHelper;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.PatientDataType;

public class PDOResponseMessageFactory {
	
	public List<PatientDataType.ObservationFactSet> getFactSetsFromResponseXML(String responseXML) throws Exception {
		JAXBUtil jaxbUtil = ExplorerJAXBUtil.getJAXBUtil();
		
        JAXBElement jaxbElement  = jaxbUtil.unMashallFromString(responseXML);
        ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();       
        BodyType bodyType = messageType.getMessageBody();
        PatientDataType patientDataType = 
			(PatientDataType) new JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.class);
		
		return patientDataType.getObservationFactSet();
	}
	
	public PatientDataType.PatientDimensionSet getPatientSetFromResponseXML(String responseXML) throws Exception {
		JAXBUtil jaxbUtil = ExplorerJAXBUtil.getJAXBUtil();
		
        JAXBElement jaxbElement  = jaxbUtil.unMashallFromString(responseXML);
        ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();       
        BodyType bodyType = messageType.getMessageBody();
        PatientDataType patientDataType = 
			(PatientDataType) new JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.class);
		PatientDataType.PatientDimensionSet patientFactSet =  patientDataType.getPatientDimensionSet();
		
		return patientFactSet;
	}
	
	public PatientDataType.VisitDimensionSet getVisitSetFromResponseXML(String responseXML) throws Exception {
		JAXBUtil jaxbUtil = ExplorerJAXBUtil.getJAXBUtil();
		
        JAXBElement jaxbElement  = jaxbUtil.unMashallFromString(responseXML);
        ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();       
        BodyType bodyType = messageType.getMessageBody();
        PatientDataType patientDataType = 
			(PatientDataType) new JAXBUnWrapHelper().getObjectByClass(bodyType.getAny(),PatientDataType.class);
		PatientDataType.VisitDimensionSet visitSet =  patientDataType.getVisitDimensionSet();
		
		return visitSet;
	}
	
	public static void main(String args[]) throws Exception { 
		
	}
	
	
}
