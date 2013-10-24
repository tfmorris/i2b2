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

import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

/**
 * Class to support provider section of plain pdo query $Id:
 * PdoQueryProviderDao.java,v 1.10 2008/03/19 22:42:08 rk903 Exp $
 * 
 * @author rkuttan
 */
public class PdoQueryProviderDao extends CRCDAO implements IPdoQueryProviderDao {
	private DataSourceLookup dataSourceLookup = null;

	public PdoQueryProviderDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		setDbSchemaName(dataSourceLookup.getFullSchema());
		setDataSource(dataSource);
	}

	/**
	 * Function to return provider/observer section of plain pdo for the given
	 * id list
	 * 
	 * @param providerIdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.ProviderDimensionSet
	 * @throws I2B2DAOException
	 */
	public ObserverSet getProviderById(List<String> providerIdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException {
		ObserverSet providerDimensionSet = new ObserverSet();
		log.debug("provider list size " + providerIdList.size());
		Connection conn = null;
		PreparedStatement query = null;
		try {
			conn = getDataSource().getConnection();
			ProviderFactRelated providerRelated = new ProviderFactRelated(
					buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = providerRelated.getSelectClause();
			String serverType = dataSourceLookup.getServerType();
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
				oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn)
						.getUnderlyingConnection();
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "provider_dimension provider WHERE provider.provider_id IN (SELECT * FROM TABLE (?))";
				log.debug("Executing sql[" + finalSql + "]");
				query = conn1.prepareStatement(finalSql);

				ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
						"QT_PDO_QRY_STRING_ARRAY", conn1);
				oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
						providerIdList.toArray(new String[] {}));
				query.setArray(1, paramArray);
			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				log.debug("creating temp table");
				java.sql.Statement tempStmt = conn.createStatement();

				try {
					tempStmt
							.executeUpdate("drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
				} catch (SQLException sqlex) {
					;
				}

				uploadTempTable(tempStmt, providerIdList);
				String finalSql = "SELECT "
						+ selectClause
						+ " FROM "
						+ getDbSchemaName()
						+ "provider_dimension provider WHERE provider.provider_id IN (select distinct input_id FROM "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ ") order by provider_id";
				log.debug("Executing [" + finalSql + "]");

				query = conn.prepareStatement(finalSql);

			}
			ResultSet resultSet = query.executeQuery();
			I2B2PdoFactory.ProviderBuilder providerBuilder = new I2B2PdoFactory().new ProviderBuilder(
					detailFlag, blobFlag, statusFlag);
			while (resultSet.next()) {
				ObserverType providerDimensionType = providerBuilder
						.buildObserverSet(resultSet);
				providerDimensionSet.getObserver().add(providerDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("", sqlEx);
			throw new I2B2DAOException("sql exception", sqlEx);
		} catch (IOException ioex) {
			log.error("", ioex);
			throw new I2B2DAOException("io exception", ioex);
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
		return providerDimensionSet;
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
