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
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.VisitListTypeHandler;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;


/**
  * Class to support event section of table pdo query
  * $Id: TablePdoQueryVisitDao.java,v 1.9 2007/09/11 01:55:12 rk903 Exp $
  * @author rkuttan
  */
public class TablePdoQueryVisitDao extends CRCDAO {
    /**
     * Function to return EventSet from visit information
     * @param encounterNumList
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return EventSet
     * @throws I2B2DAOException
     */
    public EventSet getVisitsByEncounterNum(List<String> encounterNumList,
        boolean detailFlag, boolean blobFlag, boolean statusFlag)
        throws I2B2DAOException {
        EventSet eventSet = new EventSet();

        RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(detailFlag,
                blobFlag, statusFlag);
        System.out.println("input encounter list size " +
            encounterNumList.size());

        Connection conn = null;
        PreparedStatement query = null;

        try {
            conn = getConnection();

            // create type StudentIdArrayType as varray(1000) of integer;
            String selectClause = getSelectClause(detailFlag, blobFlag,
                    statusFlag);
            String joinClause = getLookupJoinClause(detailFlag, blobFlag,
                    statusFlag);
            oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn).getUnderlyingConnection();
            String finalSql = "SELECT " + selectClause +
                " FROM visit_dimension visit \n" + joinClause +
                " WHERE visit.encounter_num IN (SELECT * FROM TABLE (?))";
            log.debug("Executing sql[" + finalSql + "]");
            query = conn1.prepareStatement(finalSql);

            ArrayDescriptor desc = ArrayDescriptor.createDescriptor("QT_PDO_QRY_STRING_ARRAY",
                    conn1);
            oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
                    encounterNumList.toArray(new String[] {  }));
            query.setArray(1, paramArray);

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                EventType event = eventBuilder.buildEventSet(resultSet,
                        "i2b2");
                eventSet.getEvent().add(event);
            }
        } catch (SQLException sqlEx) {
            log.error("", sqlEx);
            throw new I2B2DAOException("sql exception", sqlEx);
        } catch (IOException ioEx) {
            log.error("", ioEx);
            throw new I2B2DAOException("IO exception", ioEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, query, conn);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        return eventSet;
    }

    /**
     * Function to return EventSet from visit information
     * @param visitListType
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return EventSet
     * @throws I2B2DAOException
     */
    public EventSet getVisitDimensionSetFromVisitList(
        EventListType visitListType, boolean detailFlag, boolean blobFlag,
        boolean statusFlag) throws I2B2DAOException {
        VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(visitListType);
        String inSqlClause = visitListTypeHandler.generateWhereClauseSql();
        String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
        String joinClause = getLookupJoinClause(detailFlag, blobFlag, statusFlag);
        String mainSqlString = " SELECT " + selectClause +
            "  FROM visit_dimension visit " + joinClause +
            " WHERE visit.encounter_num IN ( ";
        mainSqlString += inSqlClause;
        mainSqlString += " )\n";

        EventSet eventSet = new EventSet();
        RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(detailFlag,
                blobFlag, statusFlag);
        Connection conn = null;
        PreparedStatement preparedStmt = null;

        try {
            // execute fullsql
            conn = getConnection();
            oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
            log.debug("Executing sql[" + mainSqlString + "]");
            preparedStmt = conn1.prepareStatement(mainSqlString);

            if (visitListTypeHandler.isCollectionId()) {
                String patientEncCollectionId = visitListTypeHandler.getCollectionId();
                preparedStmt.setInt(1, Integer.parseInt(patientEncCollectionId));
            } else if (visitListTypeHandler.isEnumerationSet()) {
                ArrayDescriptor desc = ArrayDescriptor.createDescriptor("QT_PDO_QRY_STRING_ARRAY",
                        conn1);
                oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
                        visitListTypeHandler.getEnumerationList()
                                            .toArray(new String[] {  }));
                preparedStmt.setArray(1, paramArray);
            }

            ResultSet resultSet = preparedStmt.executeQuery();

            while (resultSet.next()) {
                //VisitDimensionType visitDimensionType = getVisitDimensionType(resultSet);
                EventType event = eventBuilder.buildEventSet(resultSet,
                        "i2b2");
                eventSet.getEvent().add(event);
            }
        } catch (SQLException sqlEx) {
            log.error("", sqlEx);
            throw new I2B2DAOException("sql exception", sqlEx);
        } catch (IOException ioEx) {
            log.error("", ioEx);
            throw new I2B2DAOException("IO exception", ioEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        return eventSet;
    }

    /**
     * Function to return EventSet for given patient set
     * @param patientListType
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return
     * @throws I2B2DAOException
     */
    public EventSet getVisitDimensionSetFromPatientList(
        PatientListType patientListType, boolean detailFlag, boolean blobFlag,
        boolean statusFlag) throws I2B2DAOException {
        String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
        String joinClause = getLookupJoinClause(detailFlag, blobFlag, statusFlag);

        String mainSqlString = " SELECT " + selectClause +
            "  FROM visit_dimension visit " + joinClause +
            " WHERE visit.patient_num IN ( ";
        PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(patientListType);
        String inSqlClause = patientListTypeHandler.generateWhereClauseSql();

        mainSqlString += inSqlClause;
        mainSqlString += " )\n";

        System.out.println("Visit dimension sql " + mainSqlString);

        EventSet eventSet = new EventSet();
        RPDRPdoFactory.EventBuilder eventBuilder = new RPDRPdoFactory.EventBuilder(detailFlag,
                blobFlag, statusFlag);

        Connection conn = null;
        PreparedStatement preparedStmt = null;

        try {
            // execute fullsql
            conn = getConnection();
            oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
            log.debug("Executing sql[" + mainSqlString + "]");
            preparedStmt = conn1.prepareStatement(mainSqlString);

            if (patientListTypeHandler.isCollectionId()) {
                preparedStmt.setInt(1, Integer.parseInt(patientListTypeHandler.getCollectionId()));
            } else if (patientListTypeHandler.isEnumerationSet()) {
                ArrayDescriptor desc = ArrayDescriptor.createDescriptor("QT_PDO_QRY_STRING_ARRAY",
                        conn1);
                oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
                        patientListTypeHandler.getIntListFromPatientNumList()
                                              .toArray(new String[] {  }));
                preparedStmt.setArray(1, paramArray);
            } else if (patientListTypeHandler.isEntireSet()) {
                //log.debug("No need to pass parameter to sql");
            }

            ResultSet resultSet = preparedStmt.executeQuery();

            while (resultSet.next()) {
                EventType event = eventBuilder.buildEventSet(resultSet,
                        "i2b2");
                eventSet.getEvent().add(event);
            }
        } catch (SQLException sqlEx) {
            log.error("", sqlEx);
            throw new I2B2DAOException("sql exception", sqlEx);
        } catch (IOException ioEx) {
            log.error("", ioEx);
            throw new I2B2DAOException("IO exception", ioEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        return eventSet;
    }

    private String getSelectClause(boolean detailFlag, boolean blobFlag,
        boolean statusFlag) {
        String selectClause = "";
        selectClause = " visit.encounter_num visit_encounter_num, visit.patient_num visit_patient_num ";

        if (detailFlag) {
            selectClause += ", visit.inout_cd visit_inout_cd, visit.location_cd visit_location_cd, visit.location_path visit_location_path, visit.start_date visit_start_date,visit.end_date visit_end_date ";
            selectClause += ", inout_lookup.name_char inout_name,location_lookup.name_char location_name ";
        }

        if (blobFlag) {
            selectClause += ", visit.visit_blob visit_visit_blob ";
        }

        if (statusFlag) {
            selectClause += " , visit.update_date visit_update_date, visit.download_date visit_download_date, visit.import_date visit_import_date, visit.sourcesystem_cd visit_sourcesystem_cd, visit.upload_id visit_upload_id ";
        }

        return selectClause;
    }

    private String getLookupJoinClause(boolean detailFlag, boolean blobFlag,
        boolean statusFlag) {
        String joinClause = " ";

        if (detailFlag) {
            joinClause = " left JOIN visit_dim_lookup inout_lookup \n" +
                " ON (visit.inout_cd = inout_lookup.concept_Cd AND inout_lookup.dim_column = 'INOUT_CD') \n" +
                " left JOIN visit_dim_lookup location_lookup \n" +
                " ON (visit.location_Cd = location_lookup.concept_Cd AND location_lookup.dim_column = 'LOCATION_CD') \n";
        }

        return joinClause;
    }
}
