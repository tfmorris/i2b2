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
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryToolUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

/**
 * Helper class for setfinder operation. Builds sql from query definition,
 * executes the generated sql and create query results instance $Id:
 * QueryRequestSpringDao.java,v 1.5 2008/07/10 20:11:21 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryRequestSpringDao extends CRCDAO implements IQueryRequestDao {
	/** Global temp table to store intermediate setfinder results **/
	private String TEMP_TABLE = "QUERY_GLOBAL_TEMP";

	/** Global temp table to store intermediate patient list **/
	private String TEMP_DX_TABLE = "DX";

	JdbcTemplate jdbcTemplate = null;
	DataSourceLookup dataSourceLookup = null;

	public QueryRequestSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Function to build sql from given query definition This function uses
	 * QueryToolUtil class to build sql
	 * 
	 * @param queryRequestXml
	 * @return sql string
	 * @throws I2B2DAOException
	 */
	public String[] buildSql(String queryRequestXml) throws I2B2DAOException {
		String sql = null, ignoredItemMessage = null;
		Connection conn = null;

		try {
			// conn = getConnection();
			conn = dataSource.getConnection();
			QueryToolUtil queryUtil = new QueryToolUtil(dataSourceLookup);
			sql = queryUtil.generateSQL(conn, queryRequestXml);
			ignoredItemMessage = queryUtil.getIgnoredItemMessage();
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

		return new String[] { sql, ignoredItemMessage };
	}

}
