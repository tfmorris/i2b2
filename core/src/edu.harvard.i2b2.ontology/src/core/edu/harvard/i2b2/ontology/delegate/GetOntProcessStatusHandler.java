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

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.OntProcessStatusDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetOntProcessStatusType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.ws.GetOntProcessStatusMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

public class GetOntProcessStatusHandler extends RequestHandler {
	private GetOntProcessStatusMessage getOntProcessStatusMsg = null;
	private GetOntProcessStatusType getOntProcessStatusType = null;
	private ProjectType projectInfo = null;
	private MessageHeaderType messageHeaderType = null;

	public GetOntProcessStatusHandler(GetOntProcessStatusMessage requestMsg)
			throws I2B2Exception {

		getOntProcessStatusMsg = requestMsg;
		getOntProcessStatusType = requestMsg.getChild();
		this.messageHeaderType = requestMsg.getMessageHeaderType();
		projectInfo = getRoleInfo(requestMsg.getMessageHeaderType());
		setDbInfo(requestMsg.getMessageHeaderType());

	}

	@Override
	public String execute() throws I2B2Exception {
		// call ejb and pass input object

		ResponseMessageType responseMessageType = null;
		int numAdded = -1;
		String errorMessage = null;

		// check to see if we have projectInfo (if not indicates PM service
		// problem)
		OntologyProcessStatusType ontProcessStatusType = null;
		if (projectInfo == null) {
			String response = null;
			responseMessageType = MessageFactory.doBuildErrorResponse(
					getOntProcessStatusMsg.getMessageHeaderType(),
					"User was not validated");
			response = MessageFactory.convertToXMLString(responseMessageType);
			log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
			return response;
		}

		else {

			try {
				// return the message
				SecurityType securityType = getSecurityType(messageHeaderType);

				// update the process status
				OntProcessStatusDao ontProcessStatusDao = new OntProcessStatusDao(
						getDataSource(this.getDbInfo().getDb_dataSource()),
						projectInfo, this.getDbInfo());
				String processId = getOntProcessStatusType.getProcessId();
				int processIdInt = Integer.parseInt(processId);
				ontProcessStatusType = ontProcessStatusDao
						.findById(processIdInt);
			} catch (Throwable t) {
				t.printStackTrace();
				errorMessage = t.toString();
			}
		}
		// no errors found
		if (ontProcessStatusType != null) {
			// no db error but response is empty
			MessageHeaderType messageHeader = MessageFactory
					.createResponseMessageHeader(getOntProcessStatusMsg
							.getMessageHeaderType());
			responseMessageType = MessageFactory.createProcessStatusResponse(
					messageHeader, ontProcessStatusType);

		} else {
			MessageHeaderType messageHeader = MessageFactory
					.createResponseMessageHeader(getOntProcessStatusMsg
							.getMessageHeaderType());
			responseMessageType = MessageFactory.doBuildErrorResponse(
					messageHeaderType, errorMessage);
		}
		String responseVdo = null;
		responseVdo = MessageFactory.convertToXMLString(responseMessageType);
		return responseVdo;
	}

	private DataSource getDataSource(String dataSourceName) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSourceName);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			;
		}
		return ds;
	}

	private SecurityType getSecurityType(MessageHeaderType messageHeaderType) {
		SecurityType securityType = messageHeaderType.getSecurity();
		return securityType;
	}

	private String getProjectId(MessageHeaderType messageHeaderType) {
		return messageHeaderType.getProjectId();
	}

}