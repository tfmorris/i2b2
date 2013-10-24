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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryToolUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * Helper class for setfinder operation.
 * Builds sql from query definition, executes the generated sql and
 * create query results instance
 * $Id: QueryRequestDao.java,v 1.17 2007/08/31 19:39:50 rk903 Exp $
 * @author rkuttan
 */
public class QueryRequestDao extends CRCDAO {
    /** Global temp table to store intermediate setfinder results**/
    private String TEMP_TABLE = "QUERY_GLOBAL_TEMP";

    /** Global temp table to store intermediate patient list  **/
    private String TEMP_DX_TABLE = "DX";

    /**
     * Function to execute the given setfinder sql
     * And creates query instance and query result instance
     * @param generatedSql
     * @param queryInstanceId
     * @return query result instance id
     * @throws I2B2DAOException
     */
    public String getPatientCount(String generatedSql, String queryInstanceId,String patientSetId)
        throws I2B2DAOException {
        String returnedPatientSetId = "";
        Connection conn = null;

        try {
            conn = getConnection();

            if (generatedSql != null) {
                // execute sql
                returnedPatientSetId = executeSQL(conn, generatedSql, queryInstanceId,patientSetId);
                
                // clear temp table
                // deleteTable(conn);
            }
        } catch (SQLException sqlEx) {
            throw new I2B2DAOException("Sql exception" + sqlEx, sqlEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, null, conn);
            } catch (SQLException e) {
                log.error("Error closing db connection", e);
            }
        }

        return returnedPatientSetId;
    }

    /**
     * Function to build sql from given query definition
     * This function uses QueryToolUtil class to build sql
     * @param queryRequestXml
     * @return sql string
     * @throws I2B2DAOException
     */
    public String buildSql(String queryRequestXml) throws I2B2DAOException {
        String sql = null;
        Connection conn = null;

        try {
            conn = getConnection();

            QueryToolUtil queryUtil = new QueryToolUtil();
            sql = queryUtil.generateSQL(conn, queryRequestXml);
        } catch (SQLException ex) {
            log.error("Error while building sql", ex);
            throw new I2B2DAOException("Error while building sql ", ex);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, null, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return sql;
    }

    /**
     * clear global temp table for this connection
     * @param conn
     * @throws I2B2DAOException
     */
    private void deleteTable(Connection conn) throws I2B2DAOException {
        String deleteGlobalTempTable = "delete from " + TEMP_TABLE;
        String deleteCountTable = "delete from " + TEMP_DX_TABLE;

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(deleteGlobalTempTable);
            stmt.executeUpdate(deleteCountTable);
        } catch (SQLException sqlEx) {
            log.error("Error while deleting global temp table", sqlEx);
            throw new I2B2DAOException("Error while deleting global temp table",
                sqlEx);
        }

        //dont close connection it will be closed in main function
    }

    /**
     * This function executes the given sql and create
     * query result instance and its collection
     * @param conn db connection
     * @param sqlString
     * @param queryInstanceId
     * @return query result instance id
     * @throws I2B2DAOException
     */
    private String executeSQL(Connection conn, String sqlString,
        String queryInstanceId,String patientSetId) throws I2B2DAOException {
        // StringTokenizer st = new StringTokenizer(sqlString,"<*>");
        String singleSql = null;
        int recordCount = 0;
        //int patientSetId = 0;
        javax.transaction.TransactionManager tm = null;
        boolean errorFlag = false;
        Statement stmt = null;
        ResultSet resultSet = null;
        Connection manualConnection = null;
        try {
        	InitialContext context = new InitialContext();
        	tm = (javax.transaction.TransactionManager)context.lookup("java:/TransactionManager");
            if (tm == null) { 
            	log.error("TransactionManager is null");
            }
            
            tm.begin();
            //change status of result instance to running
            PatientSetResultDao psResultDao = new PatientSetResultDao();
            psResultDao.updatePatientSet(patientSetId, 2, 0);
            tm.commit();
            String[] sqls = sqlString.split("<\\*>");
            manualConnection = QueryProcessorUtil.getInstance().getManualConnection();
            stmt = manualConnection.createStatement();
            int count = 0;
          
            while (count < sqls.length) {
                singleSql = sqls[count++];
                log.debug("Executing sql [" + singleSql + "]" +
                    " for query instance= " + queryInstanceId);
                stmt.executeUpdate(singleSql);
            }

            String fetchSql = " select count(patient_num) as patient_num_count from " +
                TEMP_DX_TABLE;
            Statement countStmt = manualConnection.createStatement();
            resultSet = countStmt.executeQuery(fetchSql);
            int i = 0;

            while (resultSet.next() && (i++ < 10)) {
                recordCount = resultSet.getInt("patient_num_count");
                log.debug("Calculated Patient set size :[" + recordCount +
                    "] for query instance= " + queryInstanceId);
            }
            countStmt.close();
            resultSet.close();
            
            String patientIdSql = " select patient_num from " + TEMP_DX_TABLE +
            " order by patient_num ";
            Statement readQueryStmt = manualConnection.createStatement();
            resultSet = readQueryStmt.executeQuery(patientIdSql);

            tm.begin();
            PatientSetResultDao patientSetResultDao = new PatientSetResultDao();
            i = 0;
            PatientSetCollectionDao patientSetCollectionDao = new PatientSetCollectionDao(patientSetId);
            int loadCount = 0;

            while (resultSet.next()) {
                long patientNum = resultSet.getLong("patient_num");
                patientSetCollectionDao.addPatient(patientNum);
                i++;
                loadCount++;

                if ((i % 500) == 0) {
                    log.debug("Loading [" + loadCount + "] patients" +
                        " for query instanse = " + queryInstanceId);
                }
            }
            readQueryStmt.close();

            log.debug("Total patients loaded for query instance =" +
                queryInstanceId + " is [" + loadCount + "]");
            patientSetCollectionDao.flush();
            
            //delete temp table
            String deleteGlobalTempTable = "delete from " + TEMP_TABLE;
            String deleteCountTable = "delete from " + TEMP_DX_TABLE;
            Statement deleteStmt = manualConnection.createStatement(); 
            Statement deleteStmt1 = manualConnection.createStatement();
            
            deleteStmt.executeUpdate(deleteGlobalTempTable);
            deleteStmt1.executeUpdate(deleteCountTable);
            
            deleteStmt.close();
            deleteStmt1.close();
            
            //update set size and result status
            patientSetResultDao.updatePatientSet(patientSetId, 3, loadCount);
            //update query instance restult status
            QueryInstanceDao queryInstanceDao = new QueryInstanceDao();
            QtQueryInstance queryInstance = queryInstanceDao.getQueryInstanceByInstanceId(queryInstanceId);
            QtQueryStatusType queryStatusType = new QtQueryStatusType();
            queryStatusType.setStatusTypeId(6);
            queryInstance.setQtQueryStatusType(queryStatusType);
            queryInstanceDao.update(queryInstance);
            tm.commit();
        } catch (SQLException sqlEx) {
        	errorFlag = true;
            log.error("Error while executing sql", sqlEx);
            throw new I2B2DAOException("Error while executing sql", sqlEx);
        } catch (I2B2Exception i2b2Ex) {
        	errorFlag = true;
            log.error("Error getting manual connection ", i2b2Ex);
            throw new I2B2DAOException("Error getting manual connection ", i2b2Ex);
        } catch (NamingException nEx) {
        	errorFlag = true;
        	log.error("Naming exception", nEx);
            throw new I2B2DAOException("Naming exception", nEx);
     	} catch (IllegalStateException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("IllegalState exception", e);
		} catch (SystemException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("System exception", e);
		} catch (NotSupportedException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("System exception", e);
		} catch (SecurityException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("SecurityException", e);
		} catch (RollbackException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("RollbackException", e);
		} catch (HeuristicMixedException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("HeuristicMixedException", e);
		} catch (HeuristicRollbackException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("HeuristicRollbackException", e);
		} finally {
			//close resultset and statement
			try { 
				if (resultSet != null) {
					resultSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (manualConnection != null) {
					manualConnection.close();
				}
				
				
			} catch (SQLException sqle) { 
				log.error("Error closing statement/resultset ",sqle);
			}
			
			if (tm != null && errorFlag) { 
				try {
					tm.rollback();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
				if (tm != null) {
					try {
						log.info("Trying to update error status to result instance[" + patientSetId + "]");
						//update set size and result status
						tm.begin();
						PatientSetResultDao patientSetResultDao = new PatientSetResultDao();
						patientSetResultDao.updatePatientSet(patientSetId, 4, 0);
						tm.commit();
						log.info("Updated error status to result instance[" + patientSetId + "]");
					} catch (Exception e) {
						log.error("Updating error status to result instance failed",e);
					}
				}
			}
		}

        //dont close connection it will be closed in main function
        return patientSetId;
    }
}
