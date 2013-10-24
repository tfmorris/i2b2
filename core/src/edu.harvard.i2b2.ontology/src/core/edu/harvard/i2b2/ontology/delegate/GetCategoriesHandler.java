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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.ontology.dao.GetCategoriesDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.ws.GetCategoriesDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class GetCategoriesHandler extends RequestHandler {
    private static Log log = LogFactory.getLog(GetCategoriesHandler.class);
	private GetCategoriesDataMessage  getCategoriesMsg = null;
	private GetReturnType getReturnType = null;
	private ProjectType projectInfo = null;

	public GetCategoriesHandler(GetCategoriesDataMessage requestMsg) {
			try {
				getCategoriesMsg = requestMsg;
				getReturnType = requestMsg.getReturnType();
				projectInfo = getRoleInfo(getCategoriesMsg.getMessageHeaderType());	
			} catch (JAXBUtilException e) {
				log.error("error setting up getCategoriesHandler");
				log.error(e.getMessage());
			} catch (Exception e) {
				log.error("error setting up getCategoriesHandler");
				log.error(e.getMessage());
			}

	}
	public String execute() {
		// call ejb and pass input object
		GetCategoriesDao categoriesDao = new GetCategoriesDao();
		ConceptsType concepts = new ConceptsType();
		ResponseMessageType responseMessageType = null;
		List response = null;
		
		// check to see if we have projectInfo (if not indicates PM service problem)
		if(projectInfo == null) {
			log.error("PM service not responding");
			responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "PM service is not responding");
		}
			
		else {
			try {
				response = categoriesDao.findRootCategories(getReturnType, projectInfo);
			} catch (Exception e1) {
				log.error(e1.getMessage());
				responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "Database error");
			}
		}
		// no db error, but response is empty
		if ((response == null) && (responseMessageType == null)) {
			log.debug("query results are empty");
			responseMessageType = MessageFactory.doBuildErrorResponse(getCategoriesMsg.getMessageHeaderType(), "Query results are empty");
		}
		
		// no db error; non-empty response received
		else if(responseMessageType == null) {
			Iterator it = response.iterator();
			while (it.hasNext())
			{
				ConceptType node = (ConceptType)it.next();
				concepts.getConcept().add(node);
			}
			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getCategoriesMsg.getMessageHeaderType());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,concepts);
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
