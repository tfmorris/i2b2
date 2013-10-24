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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.dao.GetCategoriesDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.pm.ConfigureType;
import edu.harvard.i2b2.ontology.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.pm.ws.PMResponseMessage;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;



public abstract class RequestHandler {
    private static Log log = LogFactory.getLog(RequestHandler.class);
    public abstract String  execute();
    
    public ProjectType getRoleInfo(MessageHeaderType header) 
    {
    	ProjectType projectType = null;
    	
		// Are we bypassing the PM cell?  Look in properties file.
		Boolean pmBypass = false;
		String pmBypassRole = null;
		String pmBypassProject = null;
		try {
			pmBypass = OntologyUtil.getInstance().isPmBypass();
			pmBypassRole = OntologyUtil.getInstance().getPmBypassRole();
			pmBypassProject = OntologyUtil.getInstance().getPmBypassProject();
			log.debug(pmBypass + pmBypassRole + pmBypassProject);
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}
    	
		if(pmBypass == true){
			projectType = new ProjectType();
			projectType.getRole().add(pmBypassRole);
			projectType.setId(pmBypassProject);
		}
		else {
			try {
				GetUserConfigurationType userConfigType = new GetUserConfigurationType();

				PMResponseMessage msg = new PMResponseMessage();
				StatusType procStatus = null;	
				String response = PMServiceDriver.getRoles(userConfigType,header);		
				log.debug(response);
				procStatus = msg.processResult(response);
				if(procStatus.getType().equals("ERROR"))
					return null;

				ConfigureType pmConfigure = msg.readUserInfo();
				projectType = pmConfigure.getUser().getProject().get(0);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return projectType;
    }
    
    public List getCategories(ProjectType projectInfo){
    	GetCategoriesDao categoriesDao = new GetCategoriesDao();
    	GetReturnType getReturnType = new GetReturnType();
    	getReturnType.setType("default");
    	List categories = null;
    	if(projectInfo != null) {
    		try {
    			categories = categoriesDao.findRootCategories(getReturnType, projectInfo);
    		} catch (DataAccessException e) {
    			log.error(e.getMessage());
    		}
    	}
	    return categories;
    }
}
