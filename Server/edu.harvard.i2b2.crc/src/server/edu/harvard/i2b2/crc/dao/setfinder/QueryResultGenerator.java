package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;

/**
 * Setfinder's result genertor class. This class calculates patient break down
 * for the result type.
 * 
 * Calls the ontology to get the children for the result type and then
 * calculates the patient count for each child of the result type.
 */
public class QueryResultGenerator extends CRCDAO implements IResultGenerator {

	/**
	 * Function accepts parameter in Map. The patient count will be obfuscated
	 * if the user is OBFUS
	 */
	public void generateResult(Map param) throws CRCTimeOutException,
			I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");

		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		// String itemKey = (String) param.get("ItemKey");
		String resultTypeName = (String) param.get("ResultOptionName");
		int transactionTimeout = (Integer) param.get("TransactionTimeout");
		TransactionManager tm = (TransactionManager) param
				.get("TransactionManager");
		this
				.setDbSchemaName(sfDAOFactory.getDataSourceLookup()
						.getFullSchema());
		Map ontologyKeyMap = (Map) param.get("setFinderResultOntologyKeyMap");
		String serverType = (String) param.get("ServerType");
		CallOntologyUtil ontologyUtil = (CallOntologyUtil) param
				.get("CallOntologyUtil");
		List<String> roles = (List<String>) param.get("Roles");
		String tempTableName = "";
		PreparedStatement stmt = null;
		boolean errorFlag = false, timeoutFlag = false;
		String itemKey = "";

		int actualTotal = 0, obsfcTotal = 0;
		boolean obfscDataRoleFlag = false;
		try {

			obfscDataRoleFlag = checkDataObscRole(sfDAOFactory
					.getOriginalDataSourceLookup(), roles);
			itemKey = getItemKeyFromResultType(sfDAOFactory, resultTypeName);

			log.debug("Result type's " + resultTypeName + " item key value "
					+ itemKey);
			ConceptsType conceptsType = ontologyUtil.callGetChildren(itemKey);

			String itemCountSql = " select count(distinct PATIENT_NUM) as item_count  from "
					+ this.getDbSchemaName()
					+ "observation_fact obs_fact  "
					+ " where obs_fact.patient_num in (select patient_num from "
					+ TEMP_DX_TABLE
					+ "    ) "
					+ " and obs_fact.concept_cd in (select concept_cd from "
					+ this.getDbSchemaName()
					+ "concept_dimension  where concept_path like ?)";
			ResultType resultType = new ResultType();
			resultType.setName(resultTypeName);
			stmt = sfConn.prepareStatement(itemCountSql);

			CancelStatementRunner csr = new CancelStatementRunner(stmt,
					transactionTimeout);
			Thread csrThread = new Thread(csr);
			csrThread.start();

			for (ConceptType conceptType : conceptsType.getConcept()) {
				stmt.setQueryTimeout(transactionTimeout);
				// build results
				stmt.setString(1, conceptType.getDimcode() + "%");
				log.debug("Executing count sql [" + itemCountSql + "]");
				ResultSet resultSet = stmt.executeQuery();
				if (csr.getSqlFinishedFlag()) {
					timeoutFlag = true;
					throw new CRCTimeOutException("The query was canceled.");
				}
				resultSet.next();
				int demoCount = resultSet.getInt("item_count");
				actualTotal += demoCount;
				if (obfscDataRoleFlag) {
					GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
					demoCount = (int) gaussianBoxMuller
							.getNormalizedValueForCount(demoCount);
					obsfcTotal += demoCount;
				}
				DataType mdataType = new DataType();
				mdataType.setValue(String.valueOf(demoCount));
				mdataType.setColumn(conceptType.getName());
				mdataType.setType("int");
				resultType.getData().add(mdataType);
			}
			csr.setSqlFinishedFlag();
			csrThread.interrupt();
			stmt.close();

			edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createResult(resultType));
			ResultEnvelopeType resultEnvelop = new ResultEnvelopeType();
			resultEnvelop.setBody(bodyType);

			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

			StringWriter strWriter = new StringWriter();

			jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop),
					strWriter);
			tm.begin();
			IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
			xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter
					.toString());
			tm.commit();
		} catch (com.microsoft.sqlserver.jdbc.SQLServerException sqlServerEx) {
			// if the setQueryTimeout worked, then the message would be timed
			// out
			if (sqlServerEx.getMessage().indexOf("timed out") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlServerEx.getMessage(),
						sqlServerEx);
			} else if (sqlServerEx.getMessage().indexOf( // if the stmt.cancel()
					// worked, then this
					// exception is
					// thrown
					"The query was canceled.") > -1) {

				timeoutFlag = true;
				throw new CRCTimeOutException(sqlServerEx.getMessage(),
						sqlServerEx);
			} else {

				errorFlag = true;
				log.error("Sqlserver error while executing sql", sqlServerEx);
				throw new I2B2DAOException(
						"Sqlserver error while executing sql", sqlServerEx);
			}

		} catch (SQLException sqlEx) {
			// catch oracle query timeout error ORA-01013
			if (sqlEx.toString().indexOf("ORA-01013") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			if (sqlEx.getMessage().indexOf("The query was canceled.") > -1) {
				timeoutFlag = true;
				throw new CRCTimeOutException(sqlEx.getMessage(), sqlEx);
			}
			errorFlag = true;
			log.error("Error while executing sql", sqlEx);
			throw new I2B2DAOException("Error while executing sql", sqlEx);
		} catch (Exception sqlEx) {

			errorFlag = true;
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);
		} finally {

			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				// set the setsize and the description of the result instance if
				// the user role is obfuscated
				if (timeoutFlag == false) { // check if the query completed
					try {
						tm.begin();

						String obfusMethod = "", description = null;
						if (obfscDataRoleFlag) {
							obfusMethod = IQueryResultInstanceDao.OBSUBTOTAL;
							// add () to the result type description
							// read the description from result type
							IQueryResultTypeDao resultTypeDao = sfDAOFactory
									.getQueryResultTypeDao();
							List<QtQueryResultType> resultTypeList = resultTypeDao
									.getQueryResultTypeByName(resultTypeName);

							// add "(Obfuscated)" in the description
							description = resultTypeList.get(0)
									.getDescription()
									+ " (Obfuscated) ";
						}
						resultInstanceDao.updatePatientSet(resultInstanceId,
								QueryStatusTypeId.STATUSTYPE_ID_FINISHED, null,
								obsfcTotal, actualTotal, obfusMethod);

						// set the result instance description
						resultInstanceDao.updateResultInstanceDescription(
								resultInstanceId, description);
						tm.commit();
					} catch (NotSupportedException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (SystemException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (SecurityException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (IllegalStateException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (RollbackException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (HeuristicMixedException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);
					} catch (HeuristicRollbackException e) {
						throw new I2B2DAOException(
								"Failed to write obfuscated description "
										+ e.getMessage(), e);

					}

				}
			}
		}

	}

	private String getItemKeyFromResultType(SetFinderDAOFactory sfDAOFactory,
			String resultTypeKey) {
		//
		IQueryBreakdownTypeDao queryBreakdownTypeDao = sfDAOFactory
				.getQueryBreakdownTypeDao();
		QtQueryBreakdownType queryBreakdownType = queryBreakdownTypeDao
				.getBreakdownTypeByName(resultTypeKey);
		String itemKey = queryBreakdownType.getValue();
		return itemKey;
	}

	private boolean checkDataObscRole(
			DataSourceLookup originalDataSourceLookup, List<String> roles)
			throws I2B2Exception {
		boolean noDataAggFlag = false, noDataObfscFlag = false;

		String domainId = originalDataSourceLookup.getDomainId();
		String projectId = originalDataSourceLookup.getProjectPath();
		String userId = originalDataSourceLookup.getOwnerId();

		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectId,
				userId);

		IDAOFactory daoFactory = helper.getDAOFactory();
		AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
				projectId, userId, daoFactory);

		try {
			authHelper.checkRoleForProtectionLabel(
					"SETFINDER_QRY_WITHOUT_DATAOBFSC", roles);
		} catch (MissingRoleException noRoleEx) {
			noDataAggFlag = true;
		} catch (I2B2Exception e) {
			throw e;
		}
		try {
			authHelper.checkRoleForProtectionLabel(
					"SETFINDER_QRY_WITH_DATAOBFSC", roles);
		} catch (MissingRoleException noRoleEx) {
			noDataObfscFlag = true;
		} catch (I2B2Exception e) {
			throw e;
		}
		if (noDataAggFlag && !noDataObfscFlag) {
			return true;
		} else {
			return false;
		}
	}
}
