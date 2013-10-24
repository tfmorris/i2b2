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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.VisitListTypeHandler;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;


/**
 * Class to support Patient section of table pdo query
 * $Id: TablePdoQueryPatientDao.java,v 1.9 2007/09/11 01:55:12 rk903 Exp $ 
 * @author rkuttan
 */
public class TablePdoQueryPatientDao extends CRCDAO {


	public TablePdoQueryPatientDao() {
	}


	/**
	 * Function returns Patient information for given list of patient number in TablePDO format
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientByPatientNum(
			List<String> patientNumList,boolean detailFlag, boolean blobFlag, boolean statusFlag) throws I2B2DAOException {

		Connection conn = null;

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(detailFlag, blobFlag, statusFlag);
		PreparedStatement query = null;
		try {
			// execute fullsql
			conn = getConnection();

			String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
			String joinClause =  getLookupJoinClause(detailFlag, blobFlag, statusFlag);
			oracle.jdbc.driver.OracleConnection conn1 = 		(oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
			String finalSql = "SELECT " + selectClause + " FROM patient_dimension patient "
			+ joinClause
			+ " WHERE patient.patient_num IN (SELECT * FROM TABLE (?))";
			log.debug("Executing sql[" + finalSql + "]");
			query = conn1
					.prepareStatement(finalSql);

			ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
					"QT_PDO_QRY_STRING_ARRAY", conn1);

			oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
					patientNumList.toArray(new String[] {}));
			query.setArray(1, paramArray);

			ResultSet resultSet = query.executeQuery();
			//JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,"i2b2");
				patientSet.getPatient().add(patient);

			}
		} catch (SQLException sqlEx) {
			log.error("",sqlEx);
			throw new I2B2DAOException("sql exception",sqlEx);
		}catch (IOException ioEx) {
			log.error("",ioEx);
			throw new I2B2DAOException("IO exception",ioEx);
		}finally { 
			try { 
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) { 
				sqlEx.printStackTrace();
			}
		}
		return patientSet;
	}

	/**
	 * 
	 * @param patientListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromPatientSet(
			PatientListType patientListType,boolean detailFlag, boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause =  getLookupJoinClause(detailFlag, blobFlag, statusFlag);
		String mainSqlString = " SELECT " + selectClause + "  FROM patient_dimension patient "
							  + joinClause
							  + " WHERE patient.patient_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " )\n";

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(detailFlag, blobFlag, statusFlag);
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getConnection();
			oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
			System.out.println("Executing sql[" + mainSqlString + "]");
			preparedStmt = conn1
					.prepareStatement(mainSqlString);

			if (patientListTypeHandler.isCollectionId()) {
				String patientSetCollectionId = patientListTypeHandler
						.getCollectionId();
				preparedStmt.setInt(1, Integer.parseInt(patientSetCollectionId));

			} else if (patientListTypeHandler.isEnumerationSet()) {
				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);
				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						patientListTypeHandler.getIntListFromPatientNumList()
								.toArray(new String[] {}));
				preparedStmt.setArray(1, paramArray);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			//JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,"i2b2");
				patientSet.getPatient().add(patient);
			}

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		} catch (IOException ioEx) {
			log.error("",ioEx);
			throw new I2B2DAOException("IO exception",ioEx);
		} finally {
			try { 
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) { 
				sqlEx.printStackTrace();
			}

		}
		return patientSet;
	}

	/**
	 * Function returns patient information for given list of encounters
	 * @param visitListType
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientSet
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromVisitSet(
			EventListType visitListType,boolean detailFlag, boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				visitListType);

		String inSqlClause = null;
		String selectClause = getSelectClause(detailFlag, blobFlag, statusFlag);
		String joinClause =  getLookupJoinClause(detailFlag, blobFlag, statusFlag);
		String mainSqlString = " select " + selectClause + "  from patient_dimension patient " 
								+ joinClause 
								+  " where patient.patient_num in ";

		// if visit set id, then take patient num directly from qt_patient_enc_collection table, else go thru visit dimension to get patient num
		if (visitListTypeHandler.isCollectionId()) {
			inSqlClause = visitListTypeHandler.generatePatentSql();
			mainSqlString += " ( " + inSqlClause + " ) ";
		} else {
			inSqlClause = visitListTypeHandler.generateWhereClauseSql();
			mainSqlString += " (select distinct patient_num from visit_dimension where "
					+ " encounter_num in ( " + inSqlClause + " ))";
		}

		PatientSet patientSet = new PatientSet();
		RPDRPdoFactory.PatientBuilder patientBuilder = new RPDRPdoFactory.PatientBuilder(detailFlag, blobFlag, statusFlag);
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getConnection();
			log.debug("Executing sql[" + mainSqlString + "]");
			oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
			preparedStmt = conn1
					.prepareStatement(mainSqlString);
			

			if (visitListTypeHandler.isCollectionId()) {
				String encounterSetCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt.setInt(1, Integer.parseInt(encounterSetCollectionId));

			} else if (visitListTypeHandler.isEnumerationSet()) {
				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);
				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						visitListTypeHandler.getEnumerationList().toArray(
								new String[] {}));
				preparedStmt.setArray(1, paramArray);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			//JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
			while (resultSet.next()) {
				PatientType patient = patientBuilder.buildPatientSet(resultSet,"i2b2");
				patientSet.getPatient().add(patient);
			}

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		} catch (IOException ioEx) {
			log.error("",ioEx);
			throw new I2B2DAOException("IO exception",ioEx);
		} finally {
			try { 
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) { 
				sqlEx.printStackTrace();
			}

		}
		return patientSet;

	}

		
	/**
	 * Function to generate select clause based on input flags
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 */
	private String getSelectClause(boolean detailFlag, boolean blobFlag, boolean statusFlag) {
		String selectClause = "";
		selectClause = "  patient.patient_num patient_patient_num";

			if (detailFlag) {
				selectClause += " ,patient.vital_status_cd patient_vital_status_cd, patient.birth_date patient_birth_date, patient.death_date patient_death_date, patient.sex_cd patient_sex_cd, patient.age_in_years_num patient_age_in_years_num, patient.language_cd patient_language_cd, patient.race_cd patient_race_cd, patient.marital_status_cd patient_marital_status_cd, patient.religion_cd patient_religion_cd, patient.zip_cd patient_zip_cd, patient.statecityzip_path patient_statecityzip_path";
				selectClause += " ,vital_status_lookup.name_char vital_status_name, sex_lookup.name_char sex_name, language_lookup.name_char language_name, race_lookup.name_char race_name, religion_lookup.name_char religion_name, marital_status_lookup.name_char marital_status_name ";
			}
			if (blobFlag) {
				selectClause += ", patient.patient_blob patient_patient_blob ";
			} 
			if (statusFlag) {
				selectClause += " , patient.update_date patient_update_date, patient.download_date patient_download_date, patient.import_date patient_import_date, patient.sourcesystem_cd patient_sourcesystem_cd, patient.upload_id patient_upload_id ";
			}

		
		return selectClause;
	}
	
	/**
	 * Function returns sql join clause, which joins lookup tables
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return String joinclause required for table pdo lookup
	 */
	private String getLookupJoinClause(boolean detailFlag, boolean blobFlag, boolean statusFlag) { 
		String joinClause = " ";
		 
		if (detailFlag) {
			joinClause = " left JOIN patient_dim_lookup vital_status_lookup \n" + 
		    " ON (patient.vital_status_Cd = vital_status_lookup.concept_Cd AND vital_status_lookup.dim_column = 'VITAL_STATUS_CD') \n" 
			+
			" left JOIN patient_dim_lookup sex_lookup \n" + 
		    " ON (patient.sex_Cd = sex_lookup.concept_Cd AND sex_lookup.dim_column = 'SEX_CD') \n" 
			+ 
			" left JOIN patient_dim_lookup language_lookup \n" + 
		    " ON (patient.language_Cd = language_lookup.concept_Cd AND language_lookup.dim_column = 'LANGUAGE_CD') \n" 
			+
			" left JOIN patient_dim_lookup race_lookup \n" + 
		    " ON (patient.race_Cd = race_lookup.concept_Cd AND race_lookup.dim_column = 'RACE_CD') \n" 
			+ 
			" left JOIN patient_dim_lookup marital_status_lookup \n" + 
		    " ON (patient.marital_status_cd = marital_status_lookup.concept_Cd AND marital_status_lookup.dim_column = 'MARITAL_STATUS_CD') \n" 
			+ 
			" left JOIN patient_dim_lookup religion_lookup \n" + 
		    " ON (patient.religion_Cd = religion_lookup.concept_Cd AND religion_lookup.dim_column = 'RELIGION_CD') \n"; 

		}
		return joinClause;
	}
	

	

}
