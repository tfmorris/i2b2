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
import edu.harvard.i2b2.crc.datavo.db.QtPatientSetCollection;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;

import org.hibernate.Session;


/**
 * Class to support batch inserts of patients to PatientSetCollection
 * This class uses single Session to persist all the patients
 * to collection. To have limited memory usage, it will flush and clear the session manually
 * for every 1000 inserts.
 * $Id: PatientSetCollectionDao.java,v 1.4 2007/08/31 14:44:26 rk903 Exp $
 * @author rkuttan
 * @see QtPatientSetCollection
 */
public class PatientSetCollectionDao extends CRCDAO {
    /** patient set collection index **/
    private int setIndex = 0;

    
    /** master table for patient set collection **/
    QtQueryResultInstance resultInstance = null;

    /** database session created at the time of creating this object**/
    private Session session = null;

    /**
     * Construc with patientset id
     * Initialize hibernate session and creates Query reuslt instance class
     * @param patientSetId
     */
    public PatientSetCollectionDao(String patientSetId) {
        resultInstance = new QtQueryResultInstance();
        resultInstance.setResultInstanceId(patientSetId);
        session = getSession();
    }

    /**
     * function to add patient to patient set
     * without out creating new db session
     * @param patientId
     */
    public void addPatient(long patientId) {
        setIndex++;

        QtPatientSetCollection collElement = new QtPatientSetCollection();
        collElement.setPatientId(patientId);
        collElement.setQtQueryResultInstance(resultInstance);
        collElement.setSetIndex(setIndex);

        session.save(collElement);

        if ((setIndex % 1000) == 0) {
            session.flush();
            session.clear();
        }
    }

    /**
     * Call this function at the end. i.e. after loading
     * all patient with addPatient function, finally call
     * this function to clear
     * session
     */
    public void flush() {
        session.flush();
        session.clear();
        setIndex = 0;
    }
}
