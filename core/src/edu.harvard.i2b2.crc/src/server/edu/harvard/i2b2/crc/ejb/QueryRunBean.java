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
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;

/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Run"
 *  			display-name="QueryTool Query Run" 
 *  		  jndi-name="ejb.querytool.QueryRun"
 *           local-jndi-name="ejb.querytool.QueryRunLocal"
 *           name="querytool.QueryRun" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryRunRemote"
 * 
 * 
 */
public class QueryRunBean implements SessionBean {
	// RunQuery


	/**
	 * 
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * 
	 */
	public InstanceResponseType getQueryInstanceFromMasterId(String userId,MasterRequestType masterRequestType) {
		String queryMasterId =  masterRequestType.getQueryMasterId();
		QueryInstanceDao queryInstanceDao = new QueryInstanceDao();
		List<QtQueryInstance> queryInstanceList = queryInstanceDao
				.getQueryInstanceByMasterId(queryMasterId);
		InstanceResponseType instanceResponseType = new InstanceResponseType();

		DTOFactory dtoFactory = new DTOFactory();
		for (QtQueryInstance queryInstance : queryInstanceList) {
			QueryInstanceType qiType = new QueryInstanceType();
			qiType.setQueryInstanceId(queryInstance.getQueryInstanceId());
			qiType.setQueryMasterId(queryInstance.getQtQueryMaster()
					.getQueryMasterId());
			qiType.setUserId(queryInstance.getUserId());
			Date startDate = queryInstance.getStartDate();
			qiType.setStartDate(dtoFactory.getXMLGregorianCalendar(startDate
					.getTime()));
			Date endDate = queryInstance.getEndDate();
			if (endDate != null) {
				qiType.setEndDate(dtoFactory.getXMLGregorianCalendar(startDate
						.getTime()));
			}
			qiType.setBatchMode(queryInstance.getBatchMode());

			QueryStatusTypeType queryStatusType = new QueryStatusTypeType();
			queryStatusType.setName(queryInstance.getQtQueryStatusType()
					.getName());
			queryStatusType.setStatusTypeId(String.valueOf(queryInstance
					.getQtQueryStatusType().getStatusTypeId()));
			qiType.setQueryStatusType(queryStatusType);

			instanceResponseType.getQueryInstance().add(qiType);
		}
		return instanceResponseType;
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
