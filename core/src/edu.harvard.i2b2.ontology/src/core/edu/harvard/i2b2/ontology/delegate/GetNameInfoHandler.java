/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.delegate;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.GetCodeInfoDao;
import edu.harvard.i2b2.ontology.dao.GetNameInfoDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.util.StringUtil;
import edu.harvard.i2b2.ontology.ws.GetNameInfoDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetNameInfoHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetNameInfoHandler.class);
	private GetNameInfoDataMessage  nameInfoMsg = null;
	private VocabRequestType vocabType = null;
	private List categories = null;

	public GetNameInfoHandler(GetNameInfoDataMessage requestMsg) {
		try {
			nameInfoMsg = requestMsg;
			vocabType = requestMsg.getVocabRequestType();
			// test case for bad user
			//		nameInfoMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
			ProjectType project = getRoleInfo(nameInfoMsg.getMessageHeaderType());
			// if project is null, then user was not validated
			if(project != null)
				categories = getCategories(project);
		} catch (JAXBUtilException e) {
			log.error("error setting up getNameInfoHandler");
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("error setting up getNameInfoHandler");
			log.error(e.getMessage());
		}
	}
	public String execute() {
		// call ejb and pass input object
		GetNameInfoDao nameInfoDao = new GetNameInfoDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		
		// if categories == null, user was not validated
		if(categories == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "User was not validated");
			 try {
				response = MessageFactory.convertToXMLString(responseMessageType);
				log.debug("USER_INVALID or PM_SERVICE_NOT_RESPONDING");
			} catch (I2B2Exception e) {
				log.error(e.getMessage());
			}
			return response;	 
		} 
		
		//extract table code
		String tableCd = vocabType.getCategory();
		
		// test case for table access denied
		//	tableCd = "aaaaa";	
		//make sure user has access to this tableCd -- compare to categories list
		//verify that tableCd is in list of categories' key
		Iterator it = categories.iterator();
		Boolean found = false;
		while (it.hasNext())
		{
			ConceptType node = (ConceptType)it.next();
			String keyCd = StringUtil.getTableCd(node.getKey());
			if(tableCd.equals(keyCd)){
				found = true;
				break;
			}
		}
		if(found == false){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database table access was denied");
			try {
				response = MessageFactory.convertToXMLString(responseMessageType);
				log.debug(response);
			} catch (I2B2Exception e) {
				log.error(e.getMessage());
			}
			return response;	
		}
		
		List response = null;
		try {
			response = nameInfoDao.findNameInfo(vocabType, categories);
		} catch (DataAccessException e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Database error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "Query results are empty");
			}
			// No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(vocabType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > vocabType.getMax()){
					log.debug("Max request size of " + vocabType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(nameInfoMsg.getMessageHeaderType(), "MAX_EXCEEDED");
				}
				// otherwise send results
				else {
					Iterator itr = response.iterator();
					while (itr.hasNext())
					{
						ConceptType node = (ConceptType)itr.next();
						concepts.getConcept().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				}  
			}
			//max not specified so send results
			else {
				Iterator itr = response.iterator();
				while (itr.hasNext())
				{
					ConceptType node = (ConceptType)itr.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(nameInfoMsg.getMessageHeaderType());          
				responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
			}        
		}
        String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}
}
