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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.PatientDataDAOFactory;
import edu.harvard.i2b2.crc.dao.pdo.IObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionNameType;

/**
 * This is the PDO Query ejb class. It passes pdo query request's to the
 * appropriate dao classes to get response in pdo format.
 * 
 * @ejb.bean description="Patient data Query bean"
 *           display-name="Patient data Query"
 *           jndi-name="ejb.querytool.PdoQuery"
 *           local-jndi-name="ejb.querytool.PdoQueryLocal"
 *           name="querytool.PdoQuery" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.PdoQueryRemote"
 * 
 * @author rkuttan
 */
public class PdoQueryBean implements SessionBean {
	// RunQuery
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Function to get plain pdo from the given pdo request
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public PatientDataType getPlainPatientData(
			DataSourceLookup dataSourceLookup,
			GetPDOFromInputListRequestType getPDOFromInputListReqType)
			throws I2B2Exception {
		PatientDataType patientDataType = null;

		DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
				.getDomainId(), dataSourceLookup.getProjectPath(),
				dataSourceLookup.getOwnerId());
		PatientDataDAOFactory pdoDaoFactory = helper.getDAOFactory()
				.getPatientDataDAOFactory();

		try {
			OutputOptionNameType ot = getPDOFromInputListReqType
					.getOutputOption().getNames();

			if ((ot != null)
					&& ot.name().equalsIgnoreCase(
							OutputOptionNameType.ASATTRIBUTES.name())) {
				PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(
						pdoDaoFactory, PdoQueryHandler.TABLE_PDO_TYPE,
						getPDOFromInputListReqType.getInputList(),
						getPDOFromInputListReqType.getFilterList(),
						getPDOFromInputListReqType.getOutputOption());
				pdoQueryHandler.processPDORequest();
				patientDataType = pdoQueryHandler.getTablePdo();
			} else {
				PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(
						pdoDaoFactory, PdoQueryHandler.PLAIN_PDO_TYPE,
						getPDOFromInputListReqType.getInputList(),
						getPDOFromInputListReqType.getFilterList(),
						getPDOFromInputListReqType.getOutputOption());
				pdoQueryHandler.processPDORequest();
				patientDataType = pdoQueryHandler.getPlainPdo();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
			throw new I2B2Exception(ex.getMessage(), ex);
		}

		return patientDataType;
	}

	/**
	 * Function to get observation fact by its primary key
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public PatientDataType getObservationFactByPrimaryKey(
			DataSourceLookup dataSourceLookup,
			GetObservationFactByPrimaryKeyRequestType getObservationFactByPrimaryKeyRequestType)
			throws I2B2Exception {
		PatientDataType patientDataType = null;

		try {

			PatientDataDAOFactory pdoDaoFactory = getPatientDataDaoFactory(
					dataSourceLookup.getDomainId(), dataSourceLookup
							.getProjectPath(), dataSourceLookup.getOwnerId());

			IObservationFactDao observationFactDao = pdoDaoFactory
					.getObservationFactDAO();
			patientDataType = observationFactDao
					.getObservationFactByPrimaryKey(
							getObservationFactByPrimaryKeyRequestType
									.getFactPrimaryKey(),
							getObservationFactByPrimaryKeyRequestType
									.getFactOutputOption());
		} catch (I2B2DAOException ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
			throw new I2B2Exception(ex.getMessage(), ex);
		}

		return patientDataType;
	}

	private PatientDataDAOFactory getPatientDataDaoFactory(String domainId,
			String projectPath, String ownerId) throws I2B2DAOException {
		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
				ownerId);
		PatientDataDAOFactory pdoDaoFactory = helper.getDAOFactory()
				.getPatientDataDAOFactory();
		return pdoDaoFactory;
	}

	public void ejbCreate() throws CreateException {
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext arg0) throws EJBException,
			RemoteException {
	}
}
