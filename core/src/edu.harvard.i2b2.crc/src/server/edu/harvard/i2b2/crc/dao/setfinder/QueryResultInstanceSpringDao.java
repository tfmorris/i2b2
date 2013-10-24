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
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;

import org.hibernate.Session;
import org.hibernate.type.CustomType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

/**
 * This is class handles persistance of result instance and its update operation
 * $Id: QueryResultInstanceSpringDao.java,v 1.4 2008/06/30 14:42:35 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryResultInstanceSpringDao extends CRCDAO implements
		IQueryResultInstanceDao {

	JdbcTemplate jdbcTemplate = null;
	SavePatientSetResult savePatientSetResult = null;
	PatientSetResultRowMapper patientSetMapper = null;
	DataSourceLookup dataSourceLookup = null; 
	
	public QueryResultInstanceSpringDao(DataSource dataSource,DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		
		patientSetMapper = new PatientSetResultRowMapper();
	}

	/**
	 * Function to create result instance for given query instance id. The
	 * result instance status is set to running. Use updatePatientSet function
	 * to change the status to completed or error
	 * 
	 * @param queryInstanceId
	 * @return
	 */
	public String createPatientSet(String queryInstanceId,String resultName) {
		QtQueryResultInstance resultInstance = new QtQueryResultInstance();
		resultInstance.setDeleteFlag("N");

		QueryResultTypeSpringDao resultTypeDao = new QueryResultTypeSpringDao(dataSource,dataSourceLookup);
		QtQueryResultType resultType = resultTypeDao.getQueryResultTypeByName(resultName);
		resultInstance.setQtQueryResultType(resultType);

		QtQueryInstance queryInstance = new QtQueryInstance();
		queryInstance.setQueryInstanceId(queryInstanceId);
		resultInstance.setQtQueryInstance(queryInstance);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(1);
		resultInstance.setQtQueryStatusType(queryStatusType);

		Date startDate = new Date(System.currentTimeMillis());
		resultInstance.setStartDate(startDate);
		savePatientSetResult = new SavePatientSetResult(getDataSource(),getDbSchemaName(),dataSourceLookup);
		savePatientSetResult.save(resultInstance);

		return resultInstance.getResultInstanceId();
	}

	/**
	 * Function used to update result instance Particularly its status and size
	 * 
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			int setSize) {
		Date endDate = new Date(System.currentTimeMillis());
		String sql = "update " + getDbSchemaName() +"qt_query_result_instance set set_size = ?, status_type_id =?, end_date = ? where result_instance_id = ?";
		jdbcTemplate.update(sql, new Object[] { setSize, statusTypeId, endDate,
				resultInstanceId });
	}

	/**
	 * Return list of query result instance by query instance id
	 * 
	 * @param queryInstanceId
	 * @return List<QtQueryResultInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryResultInstance> getResultInstanceList(
			String queryInstanceId) {
		String sql = "select *  from " + getDbSchemaName() + "qt_query_result_instance where query_instance_id = ? ";
		List<QtQueryResultInstance> queryResultInstanceList = jdbcTemplate
				.query(sql, new Object[] { queryInstanceId }, patientSetMapper);
		return queryResultInstanceList;
	}

	/**
	 * Return list of query result instance by query result id
	 * 
	 * @param queryResultId
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceById(
			String queryResultId) throws I2B2DAOException {
		String sql = "select *  from " + getDbSchemaName() + "qt_query_result_instance where result_instance_id = ? ";
		List<QtQueryResultInstance> queryResultInstanceList = jdbcTemplate
				.query(sql, new Object[] { queryResultId }, patientSetMapper);
		if (queryResultInstanceList.size()>0) { 
			return queryResultInstanceList.get(0);
		}
		else { 
			throw new I2B2DAOException("Query result id " + queryResultId + " not found");
		}
		
	}
	
	
	/**
	 * Return list of query result instance by query instance id and result name
	 * 
	 * @param queryInstanceId
	 * @param resultName
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceByQueryInstanceIdAndName(
			String queryInstanceId,String resultName) {
		String sql = "select *  from " + getDbSchemaName() + "qt_query_result_instance ri, " + getDbSchemaName()+"qt_query_result_type rt where ri.query_instance_id = ? and ri.result_type_id = rt.result_type_id and rt.name=?";
		QtQueryResultInstance queryResultInstanceList = (QtQueryResultInstance)jdbcTemplate
				.queryForObject(sql, new Object[] { queryInstanceId,resultName }, patientSetMapper);
		return queryResultInstanceList;
	}
	
	
	private static class SavePatientSetResult extends SqlUpdate {

		private  String INSERT_ORACLE = "";
		
		private  String INSERT_SQLSERVER = "";
		private String SEQUENCE_ORACLE = "";
		DataSourceLookup dataSourceLookup = null;
	
		public SavePatientSetResult(DataSource dataSource,String dbSchemaName, DataSourceLookup dataSourceLookup) {
			super();
			setDataSource(dataSource);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
				INSERT_ORACLE = "INSERT INTO " + dbSchemaName + "QT_QUERY_RESULT_INSTANCE "
					+ "(RESULT_INSTANCE_ID, QUERY_INSTANCE_ID, RESULT_TYPE_ID, SET_SIZE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
					+ "VALUES (?,?,?,?,?,?,?,?)";
	            setSql(INSERT_ORACLE);
	            SEQUENCE_ORACLE = "select "+ dbSchemaName + "QT_SQ_QRI_QRIID.nextval from dual";
	            declareParameter(new SqlParameter(Types.INTEGER));
	            
	            } else if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) { 
	            	INSERT_SQLSERVER = "INSERT INTO " + dbSchemaName + "QT_QUERY_RESULT_INSTANCE "
	        			+ "( QUERY_INSTANCE_ID, RESULT_TYPE_ID, SET_SIZE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
	        			+ "VALUES (?,?,?,?,?,?,?)";
	            	setSql(INSERT_SQLSERVER);
	            }
			
			
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			this.dataSourceLookup = dataSourceLookup;
		
			compile();
		}

		public void save(QtQueryResultInstance resultInstance) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int resultInstanceId = 0;
			Object[] object = null;
			if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) {
				 
				object = new Object[] { resultInstance.getQtQueryInstance().getQueryInstanceId(),
						
						resultInstance.getQtQueryResultType().getResultTypeId(),
						resultInstance.getSetSize(), resultInstance.getStartDate(),
						resultInstance.getEndDate(),
						resultInstance.getQtQueryStatusType().getStatusTypeId(),
						resultInstance.getDeleteFlag()

				};
	      	  } else if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
	      		resultInstanceId = jdbc.queryForInt(SEQUENCE_ORACLE);
	      		resultInstance.setResultInstanceId(String.valueOf(resultInstanceId));
	      		object = new Object[] { resultInstance.getResultInstanceId(),
						resultInstance.getQtQueryInstance().getQueryInstanceId(),
						resultInstance.getQtQueryResultType().getResultTypeId(),
						resultInstance.getSetSize(), resultInstance.getStartDate(),
						resultInstance.getEndDate(),
						resultInstance.getQtQueryStatusType().getStatusTypeId(),
						resultInstance.getDeleteFlag()

				};
	      	  } 
			
			update(object);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) { 
	          	  int resultInstanceIdentityId = jdbc.queryForInt("SELECT @@IDENTITY");
	          	  
	          	  resultInstance.setResultInstanceId(String.valueOf(resultInstanceIdentityId));
	          	  System.out.println(resultInstanceIdentityId);
	            }

		}
	}

	private class PatientSetResultRowMapper implements RowMapper {
		QueryStatusTypeSpringDao statusTypeDao = new QueryStatusTypeSpringDao(dataSource,dataSourceLookup);
		QueryResultTypeSpringDao resultTypeDao = new QueryResultTypeSpringDao(dataSource,dataSourceLookup);
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryResultInstance resultInstance = new QtQueryResultInstance();
			resultInstance.setResultInstanceId(rs
					.getString("RESULT_INSTANCE_ID"));

			QtQueryInstance queryInstance = new QtQueryInstance();
			queryInstance.setQueryInstanceId(rs.getString("QUERY_INSTANCE_ID"));
			resultInstance.setQtQueryInstance(queryInstance);
			
			
			int resultTypeId = rs.getInt("RESULT_TYPE_ID");
			resultInstance.setQtQueryResultType(resultTypeDao.getQueryResultTypeById(resultTypeId));
			resultInstance.setSetSize(rs.getInt("SET_SIZE"));
			resultInstance.setStartDate(rs.getTimestamp("START_DATE"));
			resultInstance.setEndDate(rs.getTimestamp("END_DATE"));
		//	QtQueryStatusType queryStatusType = new QtQueryStatusType();
			int statusTypeId = rs.getInt("STATUS_TYPE_ID");
			resultInstance.setQtQueryStatusType(statusTypeDao.getQueryStatusTypeById(statusTypeId));
			//resultInstance.setQtQueryStatusType(queryStatusType);
			resultInstance.setDeleteFlag(rs.getString("DELETE_FLAG"));
			return resultInstance;
		}
	}
}
