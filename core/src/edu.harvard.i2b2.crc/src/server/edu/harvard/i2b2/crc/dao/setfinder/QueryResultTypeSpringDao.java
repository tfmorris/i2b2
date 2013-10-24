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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.IntegerStringUserType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.CustomType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;


/**
 * Class to manager persistance operation of
 * QtQueryMaster
 * $Id: QueryResultTypeSpringDao.java,v 1.4 2008/05/07 21:38:56 rk903 Exp $
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryResultTypeSpringDao extends CRCDAO implements IQueryResultTypeDao {
	
	JdbcTemplate jdbcTemplate = null;
	
	QtResultTypeRowMapper queryResultTypeMapper = new QtResultTypeRowMapper();
	

	

	private DataSourceLookup dataSourceLookup = null;
	
	
	public QueryResultTypeSpringDao(DataSource dataSource,DataSourceLookup dataSourceLookup) { 
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		
	}
	
    /**
     * Returns list of query master by user id
     * @param userId
     * @return List<QtQueryMaster>
     */
    @SuppressWarnings("unchecked")
    public QtQueryResultType getQueryResultTypeById(int resultTypeId) {
    	
        String sql = "select * from " + getDbSchemaName() + "qt_query_result_type where result_type_id = ?" ;
        QtQueryResultType queryResultType = (QtQueryResultType)jdbcTemplate.queryForObject(sql,new Object[]{resultTypeId},queryResultTypeMapper );
        return queryResultType;
    }
    
    
    /**
     * Returns list of query master by user id
     * @param userId
     * @return List<QtQueryMaster>
     */
    @SuppressWarnings("unchecked")
    public QtQueryResultType getQueryResultTypeByName(String resultName) {
    	
        String sql = "select * from " + getDbSchemaName() + "qt_query_result_type where name = ?" ;
        QtQueryResultType queryResultType = (QtQueryResultType)jdbcTemplate.queryForObject(sql,new Object[]{resultName.toUpperCase()},queryResultTypeMapper );
        return queryResultType;
    }
   
    @SuppressWarnings("unchecked")
	public List<QtQueryResultType> getAllQueryResultType() { 
    	String sql = "select * from " + getDbSchemaName() + "qt_query_result_type order by result_type_id" ;
        List<QtQueryResultType> queryResultTypeList = jdbcTemplate.query(sql,queryResultTypeMapper );
        return queryResultTypeList;
    }
    
  private static class QtResultTypeRowMapper implements RowMapper {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
    	  QtQueryResultType queryResultType = new QtQueryResultType();
    	  
    	  queryResultType.setResultTypeId(rs.getInt("RESULT_TYPE_ID"));
    	  queryResultType.setName(rs.getString("NAME"));
    	  queryResultType.setDescription(rs.getString("DESCRIPTION"));
          
          return queryResultType;
      }
  }
    
    
}
