/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.QueryInstanceDao;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;

/**
 * Ejb manager class for query operation
 * 
 * 
 * @ejb.bean description="QueryTool Query Result"
 *  			display-name="QueryTool Query Result" 
 *  		  jndi-name="ejb.querytool.QueryResult"
 *           local-jndi-name="ejb.querytool.QueryResultLocal"
 *           name="querytool.QueryResult" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryResultRemote"
 * 
 * 
 */
public class QueryResultBean implements SessionBean {
	
	
	/**
	 * 
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * 
	 */
	public ResultResponseType getResultInstanceFromQueryInstanceId(String userId, String  queryInstanceId) {
		
		QueryInstanceDao queryInstanceDao = new QueryInstanceDao(); 
		List<QtQueryResultInstance> queryResultInstanceList = queryInstanceDao.getResultInstanceList( queryInstanceId);
		ResultResponseType resultResponseType = new ResultResponseType();
		DTOFactory dtoFactory = new DTOFactory(); 
		for(QtQueryResultInstance resultInstance: queryResultInstanceList) { 
			QueryResultInstanceType queryResultInstanceType = new QueryResultInstanceType();
			queryResultInstanceType.setResultInstanceId(resultInstance.getResultInstanceId());
			queryResultInstanceType.setQueryInstanceId(resultInstance.getQtQueryInstance().getQueryInstanceId());
			queryResultInstanceType.setSetSize((resultInstance.getSetSize()!=null)?resultInstance.getSetSize():0);
			Date startDate = resultInstance.getStartDate();
			if (startDate != null) { 
				queryResultInstanceType.setStartDate(dtoFactory.getXMLGregorianCalendar(startDate.getTime()));
			}
			Date endDate = resultInstance.getEndDate();
			if (endDate != null) { 
				queryResultInstanceType.setEndDate(dtoFactory.getXMLGregorianCalendar(endDate.getTime()));
			}
			QtQueryResultType qtQueryResultType = resultInstance.getQtQueryResultType();
			QueryResultTypeType queryResultType = new QueryResultTypeType();
			queryResultType.setName(qtQueryResultType.getName());
			queryResultType.setResultTypeId(String.valueOf(qtQueryResultType.getResultTypeId()));
			queryResultInstanceType.setQueryResultType(queryResultType);
			
			QtQueryStatusType qtQueryStatusType = resultInstance.getQtQueryStatusType();
			QueryStatusTypeType queryStatusType = new QueryStatusTypeType();
			queryStatusType.setName(qtQueryStatusType.getName());
			queryStatusType.setStatusTypeId(String.valueOf(qtQueryStatusType.getStatusTypeId()));
			queryResultInstanceType.setQueryStatusType(queryStatusType);
			
			System.out.println("RESULT INSTANCE " + resultInstance.getResultInstanceId() );
			resultResponseType.getQueryResultInstance().add(queryResultInstanceType);
		}
		System.out.print("SIZE OF RESULT INSTANCE "+ resultResponseType.getQueryResultInstance().size());
		return resultResponseType;
	}
	
	
	
	public void ejbCreate() throws CreateException {
	}

	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void ejbRemove() throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
		// TODO Auto-generated method stub
		
	}
	
}
