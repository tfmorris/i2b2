package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryExecutorDao extends CRCDAO implements IQueryExecutorDao {

	private DataSourceLookup dataSourceLookup = null;
	private static Map generatorMap = null;
	private static String defaultResultType = null;

	static {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		generatorMap = (Map) bf.getBean("setFinderResultGeneratorMap");
		defaultResultType = (String) bf.getBean("defaultSetfinderResultType");
	}

	public QueryExecutorDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	/**
	 * This function executes the given sql and create query result instance and
	 * its collection
	 * 
	 * @param conn
	 *            db connection
	 * @param sqlString
	 * @param queryInstanceId
	 * @return query result instance id
	 * @throws I2B2DAOException
	 */
	public String executeSQL(UserTransaction transaction, int transactionTimeout,DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String sqlString,
			String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList)
			throws CRCTimeOutException,I2B2DAOException {
		// StringTokenizer st = new StringTokenizer(sqlString,"<*>");
		String singleSql = null;
		int recordCount = 0;
		// int patientSetId = 0;
		javax.transaction.TransactionManager tm = null;
		UserTransaction ut = transaction;
		boolean errorFlag = false, timeOutErrorFlag = false;
		Statement stmt = null;
		ResultSet resultSet = null;
		Connection manualConnection = null;
		/** Global temp table to store intermediate setfinder results* */
		String TEMP_TABLE = "#GLOBAL_TEMP_TABLE";

		/** Global temp table to store intermediate patient list * */
		String TEMP_DX_TABLE = "#DX";
		if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			TEMP_TABLE = getDbSchemaName() + "#GLOBAL_TEMP_TABLE";
			TEMP_DX_TABLE = getDbSchemaName() + "#DX";

		} else if (dsLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			TEMP_TABLE = getDbSchemaName() + "QUERY_GLOBAL_TEMP";
			TEMP_DX_TABLE = getDbSchemaName() + "DX";
		}
		try {
			InitialContext context = new InitialContext();
			tm = (javax.transaction.TransactionManager) context
					.lookup("java:/TransactionManager");
			if (tm == null) {
				log.error("TransactionManager is null");
			}

			// ut = sessionContext.getUserTransaction();
			// ut.begin();
		
			tm.begin();
			// change status of result instance to running

			IQueryResultInstanceDao psResultDao = sfDAOFactory
					.getPatientSetResultDAO();
			psResultDao.updatePatientSet(patientSetId, 2, 0);
			tm.commit();
			// ut.commit();
			String[] sqls = sqlString.split("<\\*>");
			// manualConnection =
			// QueryProcessorUtil.getInstance().getManualConnection();
			manualConnection = ServiceLocator.getInstance()
					.getAppServerDataSource(dsLookup.getDataSource())
					.getConnection();
			// manualConnection =
			// QueryProcessorUtil.getInstance().getSpringDataSource(dsLookup.getDataSource()).getConnection();
			// manualConnection =
			// QueryProcessorUtil.getInstance().getConnection();
			stmt = manualConnection.createStatement();
			int count = 0;

			if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				String checkDeleteGlobalTempTable = "drop table " + TEMP_TABLE;
				String checkDeleteCountTable = "drop table " + TEMP_DX_TABLE;
				Statement clearTempStmt = manualConnection.createStatement();
				try {
					clearTempStmt.executeUpdate(checkDeleteGlobalTempTable);
				} catch (SQLException dEx) {
					;
				}
				try {
					clearTempStmt.executeUpdate(checkDeleteCountTable);
				} catch (SQLException dEx) {
					;
				}
				clearTempStmt.close();
				String createSql = "CREATE  TABLE " + TEMP_TABLE + " ( "
						+ " ENCOUNTER_NUM int, " + " PATIENT_NUM int, "
						+ " PANEL_COUNT int, " + " fact_count int, "
						+ " fact_panels int " + " )";

				stmt.executeUpdate(createSql);
				createSql = " CREATE  TABLE " + TEMP_DX_TABLE + "  ( "
						+ " ENCOUNTER_NUM int, " + " PATIENT_NUM int " + " ) ";
				stmt.executeUpdate(createSql);
				if (dsLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
					String indexSql = "create index tempIndex on "
							+ this.getDbSchemaName()
							+ "#global_temp_table (patient_num,panel_count)";
					log.debug("Executing sql [ " + indexSql + " ]");
					stmt.executeUpdate(indexSql);
				}
			}
			//set transaction timeout
			stmt.setQueryTimeout(transactionTimeout);
			
			
			while (count < sqls.length) {
				singleSql = sqls[count++];
				log.debug("Executing sql [" + singleSql + "]"
						+ " for query instance= " + queryInstanceId);
				int rows = stmt.executeUpdate(singleSql);
				log.debug("Rows affected [" + rows + "] for query instance");
				if (this.dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
					log.debug("UPDATE STATISTICS " + this.getDbSchemaName()
							+ "#global_temp_table ");
					stmt.executeUpdate("UPDATE STATISTICS "
							+ this.getDbSchemaName() + "#global_temp_table ");
				}

			}

			String fetchSql = " select count(patient_num) as patient_num_count from "
					+ TEMP_DX_TABLE;
			Statement countStmt = manualConnection.createStatement();
			resultSet = countStmt.executeQuery(fetchSql);
			int i = 0;

			while (resultSet.next() && (i++ < 10)) {
				recordCount = resultSet.getInt("patient_num_count");
				log.debug("Calculated Patient set size :[" + recordCount
						+ "] for query instance= " + queryInstanceId);
			}
			countStmt.close();
			resultSet.close();

			tm.begin();
			//ut.begin();

			callResultGenerator(resultOutputList, manualConnection,
					sfDAOFactory, patientSetId, queryInstanceId, TEMP_DX_TABLE);

			// delete temp table
			String deleteGlobalTempTable = "";
			String deleteCountTable = "";
			if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteGlobalTempTable = "drop table " + TEMP_TABLE;
				deleteCountTable = "drop table " + TEMP_DX_TABLE;
			} else if (dsLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {

				deleteGlobalTempTable = "delete from " + TEMP_TABLE;
				deleteCountTable = "delete from " + TEMP_DX_TABLE;
			}

			Statement deleteStmt = manualConnection.createStatement();
			Statement deleteStmt1 = manualConnection.createStatement();
			deleteStmt.executeUpdate(deleteGlobalTempTable);
			deleteStmt1.executeUpdate(deleteCountTable);
			deleteStmt.close();
			deleteStmt1.close();

			// update set size and result status
			// IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
			// .getPatientSetResultDAO();
			// patientSetResultDao.updatePatientSet(patientSetId, 3,
			// recordCount);
			// update query instance restult status
			setQueryInstanceStatus(sfDAOFactory,queryInstanceId,6) ;
			tm.commit();
			 //ut.commit();
			 log.debug("Query executor completed processing query instance[ " + queryInstanceId + " ]" );
		} catch (com.microsoft.sqlserver.jdbc.SQLServerException sqlServerEx) {
			errorFlag = true;
			if (sqlServerEx.getMessage().indexOf("timed out")>-1) {
				timeOutErrorFlag = true;
				throw new CRCTimeOutException(sqlServerEx.getMessage(),sqlServerEx);
			} else { 
				log.error("Sqlserver error while executing sql", sqlServerEx);
				throw new I2B2DAOException("Sqlserver error while executing sql", sqlServerEx);
			}
			
		} catch (SQLException sqlEx) {
			if (sqlEx.toString().indexOf("ORA-01013")>-1) { 
				timeOutErrorFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(),sqlEx);
			}
			errorFlag = true;
			log.error("Error while executing sql", sqlEx);
			throw new I2B2DAOException("Error while executing sql", sqlEx);
		} catch (I2B2Exception i2b2Ex) {
			errorFlag = true;
			log.error("Error getting manual connection ", i2b2Ex);
			throw new I2B2DAOException("Error getting manual connection ",
					i2b2Ex);

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
		} catch (NamingException e) {
			errorFlag = true;
			e.printStackTrace();
			throw new I2B2DAOException("HeuristicRollbackException", e);
		}  finally {
			// close resultset and statement
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
				log.error("Error closing statement/resultset ", sqle);
			}

			if (tm != null && errorFlag) {
				try {
					tm.rollback();
					 //ut.rollback();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
				if (tm != null) {
					try {
						log
								.info("Trying to update error status to query instance["
										+ queryInstanceId + "]");
						if (sfDAOFactory != null) {
							// update set size and result status
							tm.begin();
						
							setQueryInstanceStatus(sfDAOFactory,queryInstanceId,4) ;
							tm.commit();
							log.info("Updated error status to query instance["
									+ queryInstanceId + "]");
						}
					} catch (Exception e) {
						log
								.error(
										"Error while updating error status to query instance",
										e);
						try {
							tm.rollback();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} 
			}
		}

		return patientSetId;
	}

	@SuppressWarnings("unchecked")
	private void callResultGenerator(
			ResultOutputOptionListType resultOutputList,
			Connection manualConnection, SetFinderDAOFactory sfDAOFactory,
			String patientSetId, String queryInstanceId, String TEMP_DX_TABLE)
			throws I2B2DAOException {

		Map param = new HashMap();
		SetFinderConnection sfConn = new SetFinderConnection(manualConnection);
		param.put("SetFinderConnection", sfConn);
		param.put("SetFinderDAOFactory", sfDAOFactory);
		param.put("PatientSetId", patientSetId);
		param.put("QueryInstanceId", queryInstanceId);
		param.put("TEMP_DX_TABLE", TEMP_DX_TABLE);

		if (resultOutputList != null) {
			if (resultOutputList.getResultOutput() != null
					&& resultOutputList.getResultOutput().size() > 0) {
				List<ResultOutputOptionType> resultOptionList = resultOutputList
						.getResultOutput();
				for (ResultOutputOptionType resultOutputOption : resultOptionList) {
					String resultName = resultOutputOption.getName()
							.toUpperCase();
					String resultInstanceId = getQueryResultInstanceId(
							sfDAOFactory, queryInstanceId, resultName);
					param.put("ResultInstanceId", resultInstanceId);
					param.put("ResultOptionName", resultName);
					runGenerator(resultName, param);
				}
			} else { 
				log.warn("No result output process to run, the <result_output_option> is empty");
			}

		} else {
			String resultType = defaultResultType;
			// perform patient set
			String resultInstanceId = getQueryResultInstanceId(sfDAOFactory,
					queryInstanceId, defaultResultType);
			param.put("ResultInstanceId", resultInstanceId);
			runGenerator(resultType, param);
		}
	}

	private void runGenerator(String resultName, Map param)
			throws I2B2DAOException {
		String generatorClassName = (String) generatorMap.get(resultName);
		Class generatorClass;
		IResultGenerator resultGenerator;
		try {
			generatorClass = Class.forName(generatorClassName, true, Thread
					.currentThread().getContextClassLoader());
			if (generatorClass == null) {
				throw new I2B2DAOException(
						"Generator class not configured for result name["
								+ resultName + "] ");
			}
			resultGenerator = (IResultGenerator) generatorClass.newInstance();
			resultGenerator.generateResult(param);
		} catch (ClassNotFoundException e) {
			throw new I2B2DAOException(
					"Class not found for the generator class["
							+ generatorClassName + "] ", e);
		} catch (InstantiationException e) {
			throw new I2B2DAOException("Could not initialize generator class["
					+ generatorClassName + "] ", e);
		} catch (IllegalAccessException e) {
			throw new I2B2DAOException(
					"Illegal Access Exception for generator class["
							+ generatorClassName + "] ", e);
		}
	}

	private String getQueryResultInstanceId(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, String resultName) {
		IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
				.getPatientSetResultDAO();
		QtQueryResultInstance resultInstance = resultInstanceDao
				.getResultInstanceByQueryInstanceIdAndName(queryInstanceId,
						resultName);
		return resultInstance.getResultInstanceId();
	}
	
	private void setQueryInstanceStatus(SetFinderDAOFactory sfDAOFactory,String queryInstanceId,int statusTypeId) { 
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao.getQueryInstanceByInstanceId(queryInstanceId);

		QtQueryStatusType queryStatusType = new QtQueryStatusType();
		queryStatusType.setStatusTypeId(6);
		queryInstance.setQtQueryStatusType(queryStatusType);
		queryInstance.setEndDate(new Date(System.currentTimeMillis()));
		queryInstanceDao.update(queryInstance);
	}

}
