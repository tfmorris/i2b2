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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.pdo.ObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionNameType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;


/**
 * This is the PDO Query ejb class
 * It passes pdo query request's to appropriate dao classes
 * to get response in pdo format
 *
 * @ejb.bean description="Pdo Query"
 *                          display-name="Pdo Query"
 *                    jndi-name="ejb.querytool.PdoQuery"
 *           local-jndi-name="ejb.querytool.PdoQueryLocal"
 *           name="querytool.PdoQuery" type="Stateless" view-type="both"
 *           transaction-type="Container"
 *
 *
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
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public PatientDataType getPlainPatientData(
        GetPDOFromInputListRequestType getPDOFromInputListReqType)
        throws I2B2Exception {
        PatientDataType patientDataType = null;

        try {
            OutputOptionNameType ot = getPDOFromInputListReqType.getOutputOption()
                                                                .getNames();

            if ((ot != null) &&
                    ot.name()
                          .equalsIgnoreCase(OutputOptionNameType.ASATTRIBUTES.name())) {
                PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(PdoQueryHandler.TABLE_PDO_TYPE,
                        getPDOFromInputListReqType.getInputList(),
                        getPDOFromInputListReqType.getFilterList(),
                        getPDOFromInputListReqType.getOutputOption());
                pdoQueryHandler.processPDORequest();
                patientDataType = pdoQueryHandler.getTablePdo();
            } else {
                PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(PdoQueryHandler.PLAIN_PDO_TYPE,
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
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     */
    public PatientDataType getObservationFactByPrimaryKey(
        GetObservationFactByPrimaryKeyRequestType getObservationFactByPrimaryKeyRequestType)
        throws I2B2Exception {
        PatientDataType patientDataType = null;

        try {
            ObservationFactDao observationFactDao = new ObservationFactDao();
            patientDataType = observationFactDao.getObservationFactByPrimaryKey(getObservationFactByPrimaryKeyRequestType.getFactPrimaryKey(),
                    getObservationFactByPrimaryKeyRequestType.getFactOutputOption());
        } catch (I2B2DAOException ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
            throw new I2B2Exception(ex.getMessage(), ex);
        }

        return patientDataType;
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
    }
}
