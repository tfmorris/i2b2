/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Manager"
 *           display-name="QueryTool Query Manager"
 *           jndi-name="ejb.querytool.QueryManager"
 *           local-jndi-name="ejb.querytool.QueryManagerLocal"
 *           name="querytool.QueryManager" type="Stateless" view-type="both"
 *           transaction-type="Bean"
 * 
 * @ejb.transaction type="Required"
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryManagerRemote"
 * 
 * 
 */
public class QueryManagerBean implements SessionBean {
	private static Log log = LogFactory.getLog(QueryManagerBean.class);
	public static String RESPONSE_QUEUE_NAME = "queue/jms.querytool.QueryResponse";
	// public static String UPLOADPROCESSOR_QUEUE_NAME =
	// "queue/jms.querytool.QueryExecutor";

	SessionContext context;

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            publish request XML fileName
	 * 
	 * @return String publish response XML
	 */
	public MasterInstanceResultResponseType processQuery(
			DataSourceLookup dataSourceLookup, String xmlRequest)
			throws I2B2Exception {
		String responseXML = null;
		UserTransaction transaction = context.getUserTransaction();
		javax.transaction.TransactionManager tm = (javax.transaction.TransactionManager) context
				.lookup("java:/TransactionManager");
		if (tm != null) {
			log.debug("Transaction not null");
		}
		MasterInstanceResultResponseType masterInstanceResultType = null;

		try {
			String sessionId = String.valueOf(System.currentTimeMillis());
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			long timeout = qmBeanUtil.getTimeout(xmlRequest);

			DataSourceLookup dsLookupInput = qmBeanUtil
					.getDataSourceLookupInput(xmlRequest);
			SetFinderDAOFactory sfDAOFactory = null;
			// tm.begin();
			transaction.begin();
			if (dsLookupInput.getProjectPath() == null) {
				throw new I2B2Exception("project id is missing in the request");
			}
			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
					dsLookupInput.getDomainId(),
					dsLookupInput.getProjectPath(), dsLookupInput.getOwnerId());
			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			sfDAOFactory = daoFactory.getSetFinderDAOFactory();

			String generatedSql = null;
			String queryMasterId = saveQuery(sfDAOFactory, xmlRequest,
					generatedSql);

			// create query instance
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();
			UserType userType = getUserTypeFromSetfinderHeader(xmlRequest);
			String userId = userType.getLogin();
			String groupId = userType.getGroup();
			String queryInstanceId = queryInstanceDao.createQueryInstance(
					queryMasterId, userId, groupId,
					QueryExecutorMDB.SMALL_QUEUE, 5);
			log.debug("New Query instance id " + queryInstanceId);

			IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
					.getPatientSetResultDAO();
			String patientSetId = null;
			QueryDefinitionRequestType queryDefRequestType = getQueryDefinitionRequestType(xmlRequest);
			ResultOutputOptionListType resultOptionList = queryDefRequestType
					.getResultOutputList();

			if (resultOptionList != null
					&& resultOptionList.getResultOutput() != null
					&& resultOptionList.getResultOutput().size() > 0) {
				for (ResultOutputOptionType resultOption : resultOptionList
						.getResultOutput()) {

					patientSetId = patientSetResultDao.createPatientSet(
							queryInstanceId, resultOption.getName());
					log.debug("Patient Set ID [" + patientSetId
							+ "] for query instance= " + queryInstanceId);
				}
			} else {
				QueryProcessorUtil qp = QueryProcessorUtil.getInstance();
				BeanFactory bf = qp.getSpringBeanFactory();
				String defaultResultType = (String) bf
						.getBean(QueryProcessorUtil.DEFAULT_SETFINDER_RESULT_BEANNAME);
				patientSetId = patientSetResultDao.createPatientSet(
						queryInstanceId, defaultResultType);
				log.debug("Patient Set ID [" + patientSetId
						+ "] for query instance= " + queryInstanceId);
			}

			// tm.commit();
			transaction.commit();

			ResultResponseType responseType = executeSqlInQueue(dsLookupInput
					.getDomainId(), dsLookupInput.getProjectPath(),
					dsLookupInput.getOwnerId(), userId, generatedSql,
					sessionId, queryInstanceId, patientSetId, xmlRequest,
					timeout);

			transaction.begin();
			// responseXML = qmBeanUtil.buildQueryRequestResponse(xmlRequest,
			// status,
			// sessionId,queryMasterId,queryInstanceId,responseType);
			log.debug("after queue exectution");

			/*
			 * query instance status is updated in the query executor class
			 * QtQueryInstance queryInstance = updateQueryInstanceStatus(
			 * sfDAOFactory, responseType, userId, queryInstanceId);
			 */

			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			QtQueryMaster queryMaster = queryMasterDao
					.getQueryDefinition(queryMasterId);
			masterInstanceResultType = new MasterInstanceResultResponseType();

			QueryMasterType queryMasterType = PSMFactory
					.buildQueryMasterType(queryMaster);
			// set query master
			masterInstanceResultType.setQueryMaster(queryMasterType);

			// fetch query instance by queryinstance id and build response
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			QueryInstanceType queryInstanceType = PSMFactory
					.buildQueryInstanceType(queryInstance);
			// set query instance
			masterInstanceResultType.setQueryInstance(queryInstanceType);
			// set status
			masterInstanceResultType.setStatus(responseType.getStatus());

			QueryResultBean queryResultBean = new QueryResultBean();
			ResultResponseType responseType1 = queryResultBean
					.getResultInstanceFromQueryInstanceId(dataSourceLookup,
							userId, queryInstanceId);

			log.debug("Size of result when called thru ejb "
					+ responseType1.getQueryResultInstance().size());

			// set result instance
			masterInstanceResultType.getQueryResultInstance().addAll(
					responseType1.getQueryResultInstance());
			transaction.commit();
		} catch (Throwable ex) {
			ex.printStackTrace();

			try {
				transaction.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new I2B2Exception(ex.getMessage());
		}

		return masterInstanceResultType;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param String
	 *            userId
	 * @param int master id
	 * @param long timeout
	 * 
	 * @return InstanceResultResponseType
	 */
	public InstanceResultResponseType runQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId,
			long timeout) throws I2B2Exception {
		return null;

	}

	private ResultResponseType executeSqlInQueue(String domainId,
			String projectId, String ownerId, String userId,
			String generatedSql, String sessionId, String queryInstanceId,
			String patientSetId, String xmlRequest, long timeout)
			throws Exception {

		QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();

		// process query in queue
		Map returnValues = qmBeanUtil.testSend(domainId, projectId, ownerId,
				generatedSql, sessionId, queryInstanceId, patientSetId,
				xmlRequest, timeout);

		// build response message, if query completed before given timeout
		String status = (String) returnValues
				.get(QueryManagerBeanUtil.QUERY_STATUS_PARAM);
		int queryResultInstanceId = (Integer) returnValues
				.get(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM);
		log.debug("Query Result Instance id " + queryResultInstanceId);
		StatusType statusType = new StatusType();
		StatusType.Condition condition = new StatusType.Condition();
		condition.setValue(status);
		condition.setType(status);
		statusType.getCondition().add(condition);
		ResultResponseType responseType = new ResultResponseType();
		responseType.setStatus(statusType);
		return responseType;
	}

	private String saveQuery(SetFinderDAOFactory sfDAOFactory,
			String requestXml, String generatedSql) throws Exception {
		IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
		QtQueryMaster queryMaster = new QtQueryMaster();
		UserType userType = getUserTypeFromSetfinderHeader(requestXml);
		String userId = userType.getLogin();
		String groupId = userType.getGroup();
		QueryDefinitionType queryDefType = getQueryDefinition(requestXml);
		edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();

		queryMaster.setUserId(userId);
		StringWriter queryDefWriter = new StringWriter();
		CRCJAXBUtil.getQueryDefJAXBUtil().marshaller(
				of.createQueryDefinition(queryDefType), queryDefWriter);

		queryMaster.setRequestXml(queryDefWriter.toString());
		queryMaster.setGroupId(groupId);
		queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
		queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
		queryMaster.setGeneratedSql(generatedSql);
		queryMaster.setName(queryDefType.getQueryName());

		String queryMasterId = queryMasterDao.createQueryMaster(queryMaster,
				requestXml);

		return queryMasterId;
	}

	private UserType getUserTypeFromSetfinderHeader(String xmlRequest)
			throws Exception {

		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

		if (jaxbElement == null) {
			throw new Exception(
					"null value in after unmarshalling request string ");
		}

		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		UserType userType = new UserType();
		userType.setLogin(requestMessageType.getMessageHeader().getSecurity()
				.getUsername());
		userType.setGroup(requestMessageType.getMessageHeader().getProjectId());

		return userType;
	}

	private QueryDefinitionRequestType getQueryDefinitionRequestType(
			String xmlRequest) throws Exception {
		String queryName = null;
		QueryDefinitionType queryDef = null;
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

		if (jaxbElement == null) {
			throw new Exception(
					"null value in after unmarshalling request string ");
		}

		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		BodyType bodyType = requestMessageType.getMessageBody();
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		QueryDefinitionRequestType queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(bodyType.getAny(),
						QueryDefinitionRequestType.class);
		return queryDefReqType;

	}

	public QueryDefinitionType getQueryDefinition(String xmlRequest)
			throws Exception {
		QueryDefinitionRequestType queryDefReqType = getQueryDefinitionRequestType(xmlRequest);
		QueryDefinitionType queryDef = null;
		if (queryDefReqType != null) {
			queryDef = queryDefReqType.getQueryDefinition();
		}
		return queryDef;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return String publish response XML
	 */
	public String getResponseXML(String sessionId) {
		QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
		String status = qmBeanUtil.getStatus(sessionId);
		String response = qmBeanUtil.buildGetQueryResultResponse(sessionId,
				status);

		return response;
	}

	public void setSessionContext(SessionContext context) throws EJBException,
			RemoteException {
		this.context = context;
	}

	public void ejbCreate() throws CreateException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}
}
