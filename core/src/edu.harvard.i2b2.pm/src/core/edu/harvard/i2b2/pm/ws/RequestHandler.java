/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.pm.ws;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.pm.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.CellDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ConfigureType;
import edu.harvard.i2b2.pm.datavo.pm.GlobalDataType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;
import edu.harvard.i2b2.pm.datavo.pm.UserType;
import edu.harvard.i2b2.pm.services.EnvironmentData;
import edu.harvard.i2b2.pm.services.GlobalData;
import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.services.GroupData;
import edu.harvard.i2b2.pm.services.RegisteredCell;
import edu.harvard.i2b2.pm.services.RegisteredCellParam;
import edu.harvard.i2b2.pm.services.RoleData;
import edu.harvard.i2b2.pm.services.UserData;
import edu.harvard.i2b2.pm.services.VariableData;



public class RequestHandler {
	private static Log log = LogFactory.getLog(RequestHandler.class);
	//public abstract String  execute();
	private  ServicesMessage getServicesMsg = null;

	public RequestHandler(ServicesMessage servicesMsg) {
		try {
			getServicesMsg = servicesMsg;
		} catch (Exception e) {
			log.error("error setting up getChildrenHandler");
			log.error(e.getMessage());
		}
	}

	public String execute() throws Exception  {
		log.debug("I am in the RequestHandler");

		ConfigureType cType = new ConfigureType();
		UserType uType = new UserType();
		CellDatasType aType = new CellDatasType();

		ResponseMessageType responseMessageType = null;

		try {
			SecurityType rmt = getServicesMsg.getRequestMessageType().getMessageHeader().getSecurity();

			log.debug("My username: " + rmt.getUsername());

			PortletServiceFactory factory = SportletServiceFactory.getInstance();

			GroupManagerService groupService = (GroupManagerService) factory.createPortletService(GroupManagerService.class, null, true);
			PasswordManagerService passwordManagerService = (PasswordManagerService) factory.createPortletService(PasswordManagerService.class, null, true);
			UserManagerService userManagerService =  (UserManagerService) factory.createPortletService(UserManagerService.class, null, true);
			GlobalDataService globaldataservice = (GlobalDataService) factory.createPortletService(GlobalDataService.class, null,true);

			User user = userManagerService.getUserByUserName(rmt.getUsername());


			if (user == null)
				throw new Exception ("Username does not exist");

			passwordManagerService.validateSuppliedPassword(user, rmt.getPassword());

			uType.setFullName(user.getFullName());
			uType.setUserName(rmt.getUsername());
			uType.setDomain(rmt.getDomain());
			uType.setPassword(rmt.getPassword());
			List groups = groupService.getGroups(user);
			Iterator itsg = groups.iterator();

			if (groups.size() == 1)
				throw new Exception (rmt.getUsername() + " is not associated with any groups");
			boolean found = false;
			while (itsg.hasNext()) {
				found = true;
				ProjectType pType = new ProjectType();

				PortletGroup g = (PortletGroup) itsg.next();

				//Only get valid projects, 

				GroupData pData = globaldataservice.getGroupDataByOid(g.getName());


				if (pData != null){
					pType.setId(pData.getOid());
					if ((pData.getKey() != null) && !(pData.getKey().equals("")))
						pType.setKey(pData.getKey());
					if ((pData.getName() != null) && !(pData.getName().equals("")))
						pType.setName(pData.getName());
					else
						pType.setName(g.getName());
					if ((pData.getWiki() != null) && !(pData.getWiki().equals("")))
						pType.setWiki(pData.getWiki());
					else
						pType.setWiki("http://www.i2b2.org");

					//Get Roles
					List roles = globaldataservice.getRoleData(g.getName(), rmt.getUsername());

					if (roles!=null) {
						for (int i=0;i<roles.size();i++) {

							RoleData globaldata = (RoleData)roles.get(i);
							pType.getRole().add(globaldata.getRole());
						}
					}
					//uType.getProject().add(pType);

					//Get Variables for the group
					List variables = globaldataservice.getVariableData(g.getName());
					if (variables!=null) {
						for (int i=0;i<variables.size();i++) {

							VariableData vardata = (VariableData)variables.get(i);

							ParamType parmType = new ParamType();
							parmType.setName(vardata.getName());
							parmType.setValue(vardata.getValue());
							pType.getParam().add(parmType);
						}
					}
					//Get user variables for the group
					variables = globaldataservice.getUserData(g.getName(), rmt.getUsername());
					if (variables!=null) {
						for (int i=0;i<variables.size();i++) {

							UserData vardata = (UserData)variables.get(i);

							ParamType parmType = new ParamType();
							parmType.setName(vardata.getName());
							parmType.setValue(vardata.getValue());
							pType.getParam().add(parmType);
						}
					}


					uType.getProject().add(pType);


					cType.setUser(uType);
				}

			}

			if (cType.getUser() == null)
				throw new Exception ("Group Data has not been filled out");
			if (found == false)
				throw new Exception (rmt.getUsername() + " is not part of project " + rmt.getDomain());

			EnvironmentData pData = globaldataservice.getEnvironmentData();

			if (pData != null)
			{



				//Make sure domain is set correctly
				if (!pData.getDomain().toUpperCase().equals(rmt.getDomain().toUpperCase()))
					throw new Exception (rmt.getDomain() + " is not associated with this domain " + pData.getDomain() + ". Please check the i2b2workbench.properties file.");

				//Get wiki and environment data
				cType.setHelpURL(pData.getUrl());
				cType.setEnvironment(pData.getEnvironment());
			}
			else
			{
				throw new Exception ("Environment grouphas not been setup");
			}

			//Get Cell Data
			List cell = globaldataservice.getRegisteredCell();
			if (cell!=null) {
				log.debug(" I am in cell : " + cell.size());
				for (int i=0;i<cell.size();i++) {
					RegisteredCell regCell = (RegisteredCell)cell.get(i);


					CellDataType cellType = new CellDataType();
					cellType.setName(regCell.getName());
					cellType.setId(regCell.getId());
					cellType.setUrl(regCell.getUrl());
					cellType.setMethod(regCell.getWebservice());
					log.debug("my url is " + regCell.getUrl());	

					Set cellParams  = new HashSet();
					cellParams =  regCell.getParams();
					for (Iterator ii = cellParams.iterator(); ii.hasNext(); ) {
						RegisteredCellParam regparam = (RegisteredCellParam) ii.next();						

						ParamType parmType = new ParamType();
						parmType.setName(regparam.getName());
						parmType.setValue(regparam.getValue());
						cellType.getParam().add(parmType);		
					}
					aType.getCellData().add(cellType);					
				}
				cType.setCellDatas(aType);
			}			
			else
			{
				throw new Exception ("No cells are be registered yet");
			}

			// Get Global Data
			List gData = globaldataservice.getGlobalData();
			GlobalDataType gValue = new GlobalDataType();

			if (gData!=null) {
				for (int i=0;i<gData.size();i++) {
					GlobalData globaldata = (GlobalData)gData.get(i);

					ParamType parmType = new ParamType();
					parmType.setName(globaldata.getName());
					parmType.setValue(globaldata.getValue());

					gValue.getParam().add(parmType);
					cType.setGlobalData(gValue);
				}
			}			

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.createBuildResponse(messageHeader,cType);

		}
		catch (Exception ee)
		{
			log.error(ee.getMessage());
			// throw new Exception (ee.getMessage());
			//ee.printStackTrace();

			MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getServicesMsg.getRequestMessageType().getMessageHeader());          
			responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader,
					ee.getMessage());			
		}
		log.error("here");

		String responseVdo = "DONE";
		try {
			responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		} catch (I2B2Exception e) {
			log.error(e.getMessage());
		}
		return responseVdo;
	}
}
