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
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryToolUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * Helper class for setfinder operation.
 * Builds sql from query definition, executes the generated sql and
 * create query results instance
 * $Id: QueryRequestSpringDao.java,v 1.5 2008/07/10 20:11:21 rk903 Exp $
 * @author rkuttan
 */
public class QueryRequestSpringDao extends CRCDAO implements IQueryRequestDao {
    /** Global temp table to store intermediate setfinder results**/
    private String TEMP_TABLE = "QUERY_GLOBAL_TEMP";

    /** Global temp table to store intermediate patient list  **/
    private String TEMP_DX_TABLE = "DX";
    
    JdbcTemplate jdbcTemplate = null;
    DataSourceLookup dataSourceLookup = null;
    
    public QueryRequestSpringDao(DataSource dataSource,DataSourceLookup dataSourceLookup) { 
    	setDataSource(dataSource);
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		
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
            //conn = getConnection();
        	conn = dataSource.getConnection();
            QueryToolUtil queryUtil = new QueryToolUtil(dataSourceLookup);
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

    

   
}
