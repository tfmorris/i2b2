/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

import java.util.ArrayList;
import java.util.List;


/**
 * Handler class for patient list type to generate "where" clause for pdo request
 * $Id: PatientListTypeHandler.java,v 1.5 2007/08/31 14:42:36 rk903 Exp $
 * @author rkuttan
 */
public class PatientListTypeHandler implements IInputOptionListHandler {
    private PatientListType patientListType = null;
    private int minIndex = 0;
    private int maxIndex = 0;
    private String patientSetCollId ="";
    private List<String> patientNumList = null;

    /**
     * Constructor accepts {@link PatientListType}
     * @param patientListType
     * @throws I2B2DAOException
     */
    public PatientListTypeHandler(PatientListType patientListType)
        throws I2B2DAOException {
        if (patientListType == null) {
            throw new I2B2DAOException("Patient List Type is null");
        }

        this.patientListType = patientListType;

        if (patientListType.getMin() != null) {
            minIndex = patientListType.getMin();
        }

        if (patientListType.getMax() != null) {
            maxIndex = patientListType.getMax();
        }
    }

    public int getMinIndex() {
        return minIndex;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    /**
     * Function to generate "where" clause for patient list
     */
    public String generateWhereClauseSql() {
        String sqlString = null;

        if (patientListType.getPatientSetCollId() != null) {
            //set patient set coll id
            this.patientSetCollId = patientListType.getPatientSetCollId();

            //set sql string
            sqlString = "select pset.patient_num from qt_patient_set_collection pset where pset.result_instance_id =  ?  ";

            if (minIndex < maxIndex) {
                sqlString += (" and pset.set_index between " + minIndex +
                " and " + maxIndex);
            }
        } else if ((patientListType.getPatientId() != null) &&
                (patientListType.getPatientId().size() > 0)) {
            ArrayList<String> patientNumArrayList = new ArrayList<String>(patientListType.getPatientId()
                                                                                           .size());

            for (PatientListType.PatientId patientNum : patientListType.getPatientId()) {
                //TODO see if we can use index value from patientNum
                patientNumArrayList.add(patientNum.getValue());
            }

            // set int List
            if (minIndex < maxIndex) {
                if (maxIndex > patientListType.getPatientId().size()) {
                    maxIndex = patientListType.getPatientId().size();
                }

                this.patientNumList = patientNumArrayList.subList(minIndex,
                        maxIndex);
            } else {
                this.patientNumList = patientNumArrayList.subList(minIndex,
                        patientNumArrayList.size());
            }

            //set sql string
            sqlString = " SELECT * FROM TABLE (?) ";
        } else if (patientListType.getEntirePatientSet() != null) {
            // by default get first 100 rows
            if ((minIndex == 0) && (maxIndex == 0)) {
                minIndex = 0;
                maxIndex = 100;
            }

            //do nothing
            sqlString = " select patient_num from (select p.*, ROWNUM rnum from ( select patient_num from patient_dimension  order by patient_num) p " +
                "	where ROWNUM<=" + maxIndex + " ) where  rnum>=" + minIndex;
        }

        return sqlString;
    }

    public List<String> getIntListFromPatientNumList() {
        return this.patientNumList;
    }

    public String getCollectionId() {
        return patientSetCollId;
    }

    public List<String> getEnumerationList() {
        return this.patientNumList;
    }

    public boolean isCollectionId() {
        if (patientListType.getPatientSetCollId() != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEntireSet() {
        if (patientListType.getEntirePatientSet() != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEnumerationSet() {
        if ((patientListType.getPatientId() != null) &&
                (patientListType.getPatientId().size() > 0)) {
            return true;
        } else {
            return false;
        }
    }
}
