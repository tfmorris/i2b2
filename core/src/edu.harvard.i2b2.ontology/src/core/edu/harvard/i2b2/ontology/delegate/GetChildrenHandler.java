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
import edu.harvard.i2b2.ontology.dao.GetChildrenDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.util.StringUtil;
import edu.harvard.i2b2.ontology.ws.GetChildrenDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;


public class GetChildrenHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetChildrenHandler.class);
	private GetChildrenDataMessage  getChildrenMsg = null;
	private GetChildrenType getChildrenType = null;
	private List categories = null;

	public GetChildrenHandler(GetChildrenDataMessage requestMsg) {
		try {
			getChildrenMsg = requestMsg;
			getChildrenType = requestMsg.getChildrenType();
			// test case for bad user
			//		getChildrenMsg.getMessageHeaderType().getSecurity().setUsername("aaaaaaa");
			ProjectType project = getRoleInfo(getChildrenMsg.getMessageHeaderType());
			// if project is null, then user was not validated
			if(project != null)
				categories = getCategories(project);				
		} catch (JAXBUtilException e) {
			log.error("error setting up getChildrenHandler");
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("error setting up getChildrenHandler");
			log.error(e.getMessage());
		}
	}
	public String execute() {
		// call ejb and pass input object
		GetChildrenDao childDao = new GetChildrenDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		
		// if categories == null, user was not validated
		if(categories == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "User was not validated");
			try {
				response = MessageFactory.convertToXMLString(responseMessageType);
				log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			} catch (I2B2Exception e) {
				log.error(e.getMessage());
			}
			return response;	 
		} 
		
		
		//extract table code
		String tableCd = StringUtil.getTableCd(getChildrenType.getParent());
		
	// test case for table access denied
	//	tableCd = "aaaaa";
		//make sure user has access to this tableCd -- compare to categories list
		//verify that tableCd is in list of categories' key
		Iterator itr = categories.iterator();
		Boolean found = false;
		while (itr.hasNext())
		{
			ConceptType node = (ConceptType)itr.next();
			String keyCd = StringUtil.getTableCd(node.getKey());
			if(tableCd.equals(keyCd)){
				found = true;
				break;
			}
		}
		if(found == false){
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database table access was denied");
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
			response = childDao.findChildrenByParent(getChildrenType, categories);
		} catch (DataAccessException e1) {
			log.error(e1.getMessage());
			responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
		}
		// no errors found 
		if(responseMessageType == null) {
			// no db error but response is empty
			if (response == null) {
				log.debug("query results are empty");
				responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Query results are empty");
			}
//			 No errors, non-empty response received
			// If max is specified, check that response is not > max
			else if(getChildrenType.getMax() != null) {
				// if max exceeded send error message
				if(response.size() > getChildrenType.getMax()){
					log.debug("Max request size of " + getChildrenType.getMax() + " exceeded ");
					responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "MAX_EXCEEDED");
				}
				// otherwise send results
				else {
					Iterator it = response.iterator();
					while (it.hasNext())
					{
						ConceptType node = (ConceptType)it.next();
						concepts.getConcept().add(node);
					}
					// create ResponseMessageHeader using information from request message header.
					MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
					responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
				}       
			}

			// max not specified so send results
			else {
				Iterator it = response.iterator();
				while (it.hasNext())
				{
					ConceptType node = (ConceptType)it.next();
					concepts.getConcept().add(node);
				}
				MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());          
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