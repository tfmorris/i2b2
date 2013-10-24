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
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.PatientListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.VisitListTypeHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

/**
 * Class to build patient section of plain pdo $Id: PdoQueryPatientDao.java,v
 * 1.11 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryPatientDao extends CRCDAO implements IPdoQueryPatientDao {

	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryPatientDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	/**
	 * Function to return patient dimension data for given list of patient num
	 * 
	 * @param patientNumList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws Exception
	 */
	public PatientSet getPatientByPatientNum(List<String> patientNumList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		Connection conn = null;
		PreparedStatement query = null;
		PatientSet patientDimensionSet = new PatientSet();

		try {
			// execute fullsql
			conn = getDataSource().getConnection();
			PatientFactRelated patientRelated = new PatientFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = patientRelated.getSelectClause();
			ResultSet resultSet = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
						.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_dimension patient WHERE patient.patient_num IN (SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY))) order by 1";
				log.debug("Executing [" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);

				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						patientNumList.toArray(new String[] {}));
				query.setArray(1, paramArray);
				resultSet = query.executeQuery();
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				// create temp table
				// load to temp table
				// execute sql
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				try {
					tempStmt
							.executeUpdate("drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, patientNumList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "patient_dimension patient WHERE patient.patient_num IN (select distinct input_id FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ ") order by patient_num";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);
				resultSet = query.executeQuery();
			}

			I2B2PdoFactory.PatientBuilder patientBuilder = new I2B2PdoFactory().new PatientBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				PatientType patientDimensionType = patientBuilder
						.buildPatientSet(resultSet);
				patientDimensionSet.getPatient().add(patientDimensionType);
			}

		} catch (SQLException ex) {
			log.error("", ex);
			throw new I2B2DAOException("sql exception", ex);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, query, conn);

			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}

		}
		return patientDimensionSet;
	}

	/**
	 * Get Patient dimension data based on patientlist present in input option
	 * list
	 * 
	 * @param patientListType
	 *            {@link PatientListType}
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromPatientSet(PatientListType patientListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		PatientListTypeHandler patientListTypeHandler = new PatientListTypeHandler(
				dataSourceLookup, patientListType);
		String inSqlClause = patientListTypeHandler.generateWhereClauseSql();

		PatientFactRelated patientRelated = new PatientFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = patientRelated.getSelectClause();
		String mainSqlString = " SELECT " + selectClause + "  FROM "
				+ getDbSchemaName()
				+ "patient_dimension patient WHERE patient.patient_num IN ( ";
		mainSqlString += inSqlClause;
		mainSqlString += " ) order by patient_num \n";

		PatientSet patientDimensionSet = new PatientSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing sql[" + mainSqlString + "]");

			if (patientListTypeHandler.isCollectionId()) {
				String patientSetCollectionId = patientListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setString(1, patientSetCollectionId);

			} else if (patientListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();

				if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
					oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
							.getUnderlyingConnection();
					ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
							"QT_PDO_QRY_STRING_ARRAY", conn1);
					oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc,
							conn1, patientListTypeHandler
									.getIntListFromPatientNumList().toArray(
											new String[] {}));
					preparedStmt = conn1.prepareStatement(mainSqlString);
					preparedStmt.setArray(1, paramArray);
				} else if (serverType
						.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
					log.debug("creating temp table");
					java.sql.Statement tempStmt = conn.createStatement();
					uploadTempTable(tempStmt, patientListTypeHandler
							.getIntListFromPatientNumList());
					preparedStmt = conn.prepareStatement(mainSqlString);
				}

			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}
			ResultSet resultSet = preparedStmt.executeQuery();
			I2B2PdoFactory.PatientBuilder patientBuilder = new I2B2PdoFactory().new PatientBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				PatientType patientDimensionType = patientBuilder
						.buildPatientSet(resultSet);
				patientDimensionSet.getPatient().add(patientDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("SQLException", sqlEx);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		return patientDimensionSet;
	}

	/**
	 * Get Patient dimension data based on visitlist present in input option
	 * list
	 * 
	 * @param eventListType
	 *            {@link EventListType}
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.PatientDimensionSet
	 * @throws I2B2DAOException
	 */
	public PatientSet getPatientFromVisitSet(EventListType visitListType,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {

		VisitListTypeHandler visitListTypeHandler = new VisitListTypeHandler(
				dataSourceLookup, visitListType);

		String inSqlClause = null;
		PatientFactRelated patientRelated = new PatientFactRelated(
				buildOutputOptionType(detailFlag, blobFlag, statusFlag));
		String selectClause = patientRelated.getSelectClause();

		String mainSqlString = " select " + selectClause + "  from "
				+ getDbSchemaName()
				+ "patient_dimension patient where patient.patient_num in ";

		// if visit set id, then take patient num directly from
		// qt_patient_enc_collection table, else go thru visit dimension to get
		// patient num
		if (visitListTypeHandler.isCollectionId()) {
			inSqlClause = visitListTypeHandler.generatePatentSql();
			mainSqlString += " ( " + inSqlClause + " ) ";
		} else {
			inSqlClause = visitListTypeHandler.generateWhereClauseSql();
			mainSqlString += " (select distinct patient_num from "
					+ getDbSchemaName() + "visit_dimension where "
					+ " encounter_num in ( " + inSqlClause + " ))";
		}
		mainSqlString += " order by patient_num";

		PatientSet patientDimensionSet = new PatientSet();
		Connection conn = null;
		PreparedStatement preparedStmt = null;

		try {
			// execute fullsql
			conn = getDataSource().getConnection();

			log.debug("Executing sql [" + mainSqlString + "]");

			if (visitListTypeHandler.isCollectionId()) {
				String encounterSetCollectionId = visitListTypeHandler
						.getCollectionId();
				preparedStmt = conn.prepareStatement(mainSqlString);
				preparedStmt.setString(1, encounterSetCollectionId);

			} else if (visitListTypeHandler.isEnumerationSet()) {
				String serverType = dataSourceLookup.getServerType();
				if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
					oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
							.getUnderlyingConnection();
					ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
							"QT_PDO_QRY_STRING_ARRAY", conn1);
					oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc,
							conn1, visitListTypeHandler.getEnumerationList()
									.toArray(new String[] {}));
					preparedStmt = conn1.prepareStatement(mainSqlString);
					preparedStmt.setArray(1, paramArray);
				} else if (serverType
						.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
					log.debug("creating temp table");
					java.sql.Statement tempStmt = conn.createStatement();
					uploadTempTable(tempStmt, visitListTypeHandler
							.getEnumerationList());
					preparedStmt = conn.prepareStatement(mainSqlString);
				}
			} else {
				preparedStmt = conn.prepareStatement(mainSqlString);
			}

			ResultSet resultSet = preparedStmt.executeQuery();
			I2B2PdoFactory.PatientBuilder patientBuilder = new I2B2PdoFactory().new PatientBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				PatientType patientDimensionType = patientBuilder
						.buildPatientSet(resultSet);
				patientDimensionSet.getPatient().add(patientDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("Sql exception", sqlEx);
		} catch (IOException ioEx) {
			log.error("", ioEx);
			throw new I2B2DAOException("IO exception", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
			try {
				JDBCUtil.closeJdbcResource(null, preparedStmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return patientDimensionSet;

	}

	private void uploadTempTable(Statement tempStmt, List<String> patientNumList)
			throws SQLException {
		String createTempInputListTable = "create table "
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
				+ " ( input_id varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		log.debug("created temp table"
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : patientNumList) {
			tempStmt.addBatch("insert into "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ " values ('" + singleValue + "' )");
			log.debug("adding batch" + singleValue);
			i++;
			if (i % 100 == 0) {
				log.debug("batch insert");
				tempStmt.executeBatch();

			}
		}
		log.debug("batch insert1");
		tempStmt.executeBatch();
	}

	private void deleteTempTable(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			conn
					.createStatement()
					.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		} catch (SQLException sqle) {
			;
		} finally {
			try {
				deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
