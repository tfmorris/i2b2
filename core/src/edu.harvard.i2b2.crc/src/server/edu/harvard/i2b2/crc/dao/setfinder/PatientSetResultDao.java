/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.IntegerStringUserType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

import org.hibernate.Session;
import org.hibernate.type.CustomType;

import java.util.Date;


/**
 * This is class handles persistance of result instance
 * and its update operation
 * $Id: PatientSetResultDao.java,v 1.5 2007/08/31 14:44:26 rk903 Exp $
 * @author rkuttan
 */
public class PatientSetResultDao extends CRCDAO {
    /**
     * Function to create result instance for given
     * query instance id. The result instance status is set to
     * running. Use updatePatientSet function to change the status to completed or error
     * @param queryInstanceId
     * @return
     */
    public String createPatientSet(String queryInstanceId) {
        QtQueryResultInstance resultInstance = new QtQueryResultInstance();
        resultInstance.setDeleteFlag("N");

        QtQueryResultType resultType = new QtQueryResultType();
        resultType.setResultTypeId(1);
        resultInstance.setQtQueryResultType(resultType);

        QtQueryInstance queryInstance = new QtQueryInstance();
        queryInstance.setQueryInstanceId(queryInstanceId);
        resultInstance.setQtQueryInstance(queryInstance);

        QtQueryStatusType queryStatusType = new QtQueryStatusType();
        queryStatusType.setStatusTypeId(1);
        resultInstance.setQtQueryStatusType(queryStatusType);

        Date startDate = new Date();
        resultInstance.setStartDate(startDate);

        Session session = getSession();
        session.save(resultInstance);

        return resultInstance.getResultInstanceId();
    }

    /**
     * Function used to update result instance
     * Particularly its status and size
     * @param resultInstanceId
     * @param statusTypeId
     * @param setSize
     */
    public void updatePatientSet(String resultInstanceId, int statusTypeId,
        int setSize) {
        Session session = getSession();
        QtQueryResultInstance resultInstance = (QtQueryResultInstance) session.load(QtQueryResultInstance.class,
                resultInstanceId);
        resultInstance.setSetSize(setSize);
        resultInstance.setEndDate(new Date(System.currentTimeMillis()));

        QtQueryStatusType statusType = new QtQueryStatusType();
        statusType.setStatusTypeId(statusTypeId);
        resultInstance.setQtQueryStatusType(statusType);
        session.update(resultInstance);
    }
}
