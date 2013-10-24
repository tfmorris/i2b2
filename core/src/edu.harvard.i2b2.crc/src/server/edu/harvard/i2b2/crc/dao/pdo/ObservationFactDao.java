/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.pdo.I2B2PdoFactory.ObservationFactBuilder;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactPrimaryKeyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * DAO class for observation fact
 * $Id: ObservationFactDao.java,v 1.9 2007/08/31 14:40:23 rk903 Exp $
 * @author rkuttan
 * @see FactPrimaryKeyType
 * @see OutputOptionType
 */
public class ObservationFactDao extends CRCDAO {
    
    /**
     * Function returns Observation fact from the primary key.
     * <p>Required fields : <b>patient_num, concept_cd, encounter_num</b>
     * <p>Optional field  : <b>provider_id,start_date</b>
     * @param factPrimaryKey
     * @param factOutputOption
     * @return PatientDataType
     * @throws I2B2Exception
     */
    public PatientDataType getObservationFactByPrimaryKey(
        FactPrimaryKeyType factPrimaryKey, OutputOptionType factOutputOption)
        throws I2B2DAOException {
        PatientDataType patientDataType = new PatientDataType();

        ObservationFactFactRelated factRelated = new ObservationFactFactRelated(factOutputOption);

        String sql = " SELECT " + factRelated.getSelectClause() + " \n " +
            " FROM observation_fact obs \n" +
            " WHERE obs.encounter_num = ? AND \n " +
            " obs.patient_num  = ? AND \n" + " obs.concept_cd = ?  \n";

        if (factPrimaryKey.getObserverId() != null) {
            sql += " AND obs.provider_id = ? \n";
        }

        //make given start date to 'mm-dd-yyyy hh24:mi' format
        if (factPrimaryKey.getStartDate() != null) {
            GregorianCalendar gc = factPrimaryKey.getStartDate()
                                                 .toGregorianCalendar();
            String startDateFormat = gc.get(Calendar.MONTH) + "-" +
                gc.get(Calendar.DAY_OF_MONTH) + "-" + gc.get(Calendar.YEAR) +
                " " + gc.get(Calendar.HOUR_OF_DAY) + ":" +
                gc.get(Calendar.MINUTE);
            sql += (" AND obs.start_date = to_date('" + startDateFormat +
            " ', 'mm-dd-yyyy hh24:mi') ");
        }

        if (factPrimaryKey.getModifierCd() != null) {
            sql += " AND obs.modifier_cd = ? ";
        }

        log.debug("Generated Sql from ObservationFactDAO[" + sql + "]");

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            //get db connection
            conn = getConnection();

            //create prepared statement
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(factPrimaryKey.getEventId()));
            stmt.setInt(2, Integer.parseInt(factPrimaryKey.getPatientId()));
            stmt.setString(3, factPrimaryKey.getConceptCd());

            int i = 4;
            String providerId = factPrimaryKey.getObserverId();

            //if provider id is not null add it to sql parameter
            if (providerId != null) {
                stmt.setString(i, providerId);
                i++;
            }

            //if modifier cd is not null add it to sql parameter
            if (factPrimaryKey.getModifierCd() != null) {
                stmt.setString(i, factPrimaryKey.getModifierCd());
            }

            ResultSet resultSet = stmt.executeQuery();
            ObservationSet obsFactSet = new ObservationSet();
            
            I2B2PdoFactory.ObservationFactBuilder observationFactBuilder = new I2B2PdoFactory().new ObservationFactBuilder(factRelated.isSelectDetail(), factRelated.isSelectBlob(), factRelated.isSelectStatus()); 
            while (resultSet.next()) {
            	ObservationType observationFactType = observationFactBuilder.buildObservationSet(resultSet);
                obsFactSet.getObservation().add(observationFactType);
            }

            patientDataType.getObservationSet().add(obsFactSet);
        } catch (SQLException sqlEx) {
            log.error(sqlEx);
            throw new I2B2DAOException("", sqlEx);
        } catch (IOException ioEx) {
            log.error(ioEx);
            throw new I2B2DAOException("", ioEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, stmt, conn);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        return patientDataType;
    }

    
}
