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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.setfinder.PatientSetResultDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryRequestDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;


/**
 * Ejb manager class for query operation
 *
 * @author rkuttan
 *
 * @ejb.bean
 *                           description="QueryTool Query Manager"
 *                           display-name="QueryTool Query Manager"
 *                           jndi-name="ejb.querytool.QueryManager"
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
    public static String UPLOADPROCESSOR_QUEUE_NAME = "queue/jms.querytool.QueryExecutor";

   
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
    public MasterInstanceResultResponseType processQuery(String xmlRequest) throws I2B2Exception {
        String responseXML = null;
        UserTransaction transaction = context.getUserTransaction();
        javax.transaction.TransactionManager tm = (javax.transaction.TransactionManager)context.lookup("java:/TransactionManager");
        if (tm != null) { 
        	log.debug("Transaction not null");
        }
        MasterInstanceResultResponseType masterInstanceResultType = null;

        try {
            String sessionId = String.valueOf(System.currentTimeMillis());
            QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
            long timeout = qmBeanUtil.getTimeout(xmlRequest);

            tm.begin();
            
            //get timeout information
            QueryRequestDao requestDao = new QueryRequestDao();
            String generatedSql = requestDao.buildSql(xmlRequest);
            if (generatedSql == null) { 
            	throw new I2B2Exception("Database error unable to generate sql from query definition");
            } else if (generatedSql.trim().length()<1) { 
            	throw new I2B2Exception("Database error unable to generate sql from query definition");
            }
            
            String queryMasterId = saveQuery(xmlRequest, generatedSql);

            // create query instance
            QueryInstanceDao queryInstanceDao = new QueryInstanceDao();
            UserType userType = getUserTypeFromSetfinderHeader(xmlRequest);
            String userId = userType.getLogin();
            String groupId = userType.getGroup();
            String queryInstanceId = queryInstanceDao.createQueryInstance(queryMasterId,
                    userId, groupId, "batch_mode", 5);
            log.debug("New Query instance id " + queryInstanceId);
            
            PatientSetResultDao patientSetResultDao = new PatientSetResultDao();

            String patientSetId = patientSetResultDao.createPatientSet(queryInstanceId);
            log.debug("Patient Set ID [" + patientSetId +
                "] for query instance= " + queryInstanceId);
 
            tm.commit();
            

            ResultResponseType responseType = executeSqlInQueue(userId, generatedSql,
                    sessionId, queryInstanceId, patientSetId,timeout);

            
            transaction.begin();
            //responseXML = qmBeanUtil.buildQueryRequestResponse(xmlRequest, status,
            //		sessionId,queryMasterId,queryInstanceId,responseType);
            log.debug("after queue exectution");
            
            QtQueryInstance queryInstance = updateQueryInstanceStatus(responseType,
                    userId, queryInstanceId);
            QueryMasterDao queryMasterDao = new QueryMasterDao();
            QtQueryMaster queryMaster = queryMasterDao.getQueryDefinition(
                    queryMasterId);
            masterInstanceResultType = new MasterInstanceResultResponseType();

            QueryMasterType queryMasterType = PSMFactory.buildQueryMasterType(queryMaster);
            //set query master
            masterInstanceResultType.setQueryMaster(queryMasterType);

            QueryInstanceType queryInstanceType = PSMFactory.buildQueryInstanceType(queryInstance);
            //set query instance
            masterInstanceResultType.setQueryInstance(queryInstanceType);
            //set status
            masterInstanceResultType.setStatus(responseType.getStatus());

            QueryResultBean queryResultBean = new QueryResultBean();
            ResultResponseType responseType1 = queryResultBean.getResultInstanceFromQueryInstanceId(userId,
                    queryInstanceId);
            
            log.debug("Size of result when called thru ejb " + responseType1.getQueryResultInstance().size());

            //set result instance
            masterInstanceResultType.getQueryResultInstance().addAll(responseType1.getQueryResultInstance());
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();

            try {
                transaction.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new I2B2Exception(ex.getMessage(),ex);
        }
        

        return masterInstanceResultType;
    }

    /**
     * Function to publish patients using publish message format.
     *
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     *
     * @param String userId
     * @param int master id
      * @param long timeout
      *
     * @return InstanceResultResponseType
     */
    public InstanceResultResponseType runQueryMaster(String userId,
        String masterId, long timeout) throws I2B2Exception {
        InstanceResultResponseType instanceResultResponse = null;
        UserTransaction transaction = context.getUserTransaction();

        try {
            transaction.begin();

            String sessionId = String.valueOf(System.currentTimeMillis());

            //get querymaster from masterid
            QueryMasterDao queryMasterDao = new QueryMasterDao();
            QtQueryMaster queryMaster = queryMasterDao.getQueryDefinition(
                    masterId);

            if (queryMaster == null) {
                throw new I2B2Exception(
                    "Could not find Query master for userid=" + userId +
                    " masterId=" + masterId);
            }

            QueryInstanceDao queryInstanceDao = new QueryInstanceDao();

            //create queryinstance using masterid
            String queryInstanceId = queryInstanceDao.createQueryInstance(queryMaster.getQueryMasterId(),
                    userId, "group_id", "batch_mode", 5);

            //get sql
            String generatedSql = queryMaster.getGeneratedSql();
            PatientSetResultDao patientSetResultDao = new PatientSetResultDao();

            String patientSetId = patientSetResultDao.createPatientSet(queryInstanceId);
            log.debug("Patient Set ID [" + patientSetId +
                "] for query instance= " + queryInstanceId);

            transaction.commit();
            
            ResultResponseType responseType = executeSqlInQueue(userId, generatedSql,
                    sessionId, queryInstanceId, patientSetId, timeout);

            transaction.begin();
            QueryResultBean queryResultBean = new QueryResultBean();
            ResultResponseType responseType1 = queryResultBean.getResultInstanceFromQueryInstanceId(userId,
                    queryInstanceId);
            
            log.debug("Size of result when called thru ejb " +
                responseType.getQueryResultInstance().size());

            responseType.getQueryResultInstance().addAll(responseType1.getQueryResultInstance());
            
            //build InstanceResultResponse
            QtQueryInstance queryInstance = updateQueryInstanceStatus(responseType,
                    userId, queryInstanceId);
            instanceResultResponse = new InstanceResultResponseType();

            QueryInstanceType queryInstanceType = PSMFactory.buildQueryInstanceType(queryInstance);
            instanceResultResponse.setQueryInstance(queryInstanceType);
            instanceResultResponse.getQueryResultInstance().addAll(responseType.getQueryResultInstance());
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();

            try {
                transaction.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            throw new I2B2Exception(ex.getMessage(),ex);
        }

        return instanceResultResponse;
    }

    private ResultResponseType executeSqlInQueue(String userId, String generatedSql,
        String sessionId, String queryInstanceId,String patientSetId, long timeout)
        throws Exception {
        QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();

        //process query in queue
        Map returnValues = qmBeanUtil.testSend(generatedSql, sessionId,
                queryInstanceId, patientSetId, timeout);

        //build response message, if query completed before given timeout
        String status = (String) returnValues.get(QueryManagerBeanUtil.QUERY_STATUS_PARAM);
        int queryResultInstanceId = (Integer) returnValues.get(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM);
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

    private QtQueryInstance updateQueryInstanceStatus(
        ResultResponseType responseType, String userId, String queryInstanceId) {
        QtQueryInstance queryInstance = null;
        String status = ((StatusType.Condition) responseType.getStatus()
                                                            .getCondition()
                                                            .get(0)).getValue();
        QueryInstanceDao queryInstanceDao = new QueryInstanceDao();
        queryInstance = queryInstanceDao.getQueryInstanceByInstanceId(
                queryInstanceId);

        queryInstance.setEndDate(new Date(System.currentTimeMillis()));
        QtQueryStatusType statusType = new QtQueryStatusType();

        if (status.equalsIgnoreCase("done")) {
            statusType.setStatusTypeId(6);
        } else if (status.equalsIgnoreCase("running")) {
            statusType.setStatusTypeId(5);
        } else if (status.equalsIgnoreCase("error")) {
            statusType.setStatusTypeId(4);
        }

        queryInstance.setQtQueryStatusType(statusType);
        queryInstance = queryInstanceDao.update(queryInstance);

        return queryInstance;
    }

    private String saveQuery(String requestXml, String generatedSql)
        throws Exception {
        QueryMasterDao queryMasterDao = new QueryMasterDao();
        QtQueryMaster queryMaster = new QtQueryMaster();
        UserType userType = getUserTypeFromSetfinderHeader(requestXml);
        String userId = userType.getLogin();
        String groupId = userType.getGroup();
        QueryDefinitionType queryDefType = getQueryDefinition(requestXml);
        edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
        
        queryMaster.setUserId(userId);
        StringWriter queryDefWriter = new StringWriter();
        CRCJAXBUtil.getQueryDefJAXBUtil().marshaller(of.createQueryDefinition(queryDefType), queryDefWriter);
        
        queryMaster.setRequestXml(queryDefWriter.toString());
        queryMaster.setGroupId(groupId);
        queryMaster.setCreateDate(new Date());
        queryMaster.setDeleteFlag(QtQueryMaster.DELETE_OFF_FLAG);
        queryMaster.setGeneratedSql(generatedSql);
        queryMaster.setName(queryDefType.getQueryName());

        String queryMasterId = queryMasterDao.createQueryMaster(queryMaster);

        return queryMasterId;
    }

    private UserType getUserTypeFromSetfinderHeader(String xmlRequest) throws Exception {
        UserType userType  = null;
        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

        if (jaxbElement == null) {
            throw new Exception(
                "null value in after unmarshalling request string ");
        }

        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
        BodyType bodyType = requestMessageType.getMessageBody();
        JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
        PsmQryHeaderType headerType = (edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType) unWrapHelper.getObjectByClass(bodyType.getAny(),
                edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);

        if (headerType != null) {
            userType = headerType.getUser();
        }
        if (userType != null && userType.getGroup() == null) { 
        	userType.setGroup("no_group_id");
        }

        return userType;
    }

    private QueryDefinitionType getQueryDefinition(String xmlRequest) throws Exception {
        String queryName = null;
        QueryDefinitionType queryDef = null;
        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

        if (jaxbElement == null) {
            throw new Exception(
                "null value in after unmarshalling request string ");
        }

        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
        BodyType bodyType = requestMessageType.getMessageBody();
        JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
        QueryDefinitionRequestType queryDefReqType = (QueryDefinitionRequestType) unWrapHelper.getObjectByClass(bodyType.getAny(),
                QueryDefinitionRequestType.class);

        if (queryDefReqType != null) {
            queryDef = queryDefReqType.getQueryDefinition();

            if (queryDef != null) {
                queryName = queryDef.getQueryName();
            }
        }

        return queryDef;
    }

    /**
     * Function to publish patients using publish message format.
     *
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Required"
     *
     * @param int
     *            session id publish request XML fileName
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


    public void setSessionContext(SessionContext context)
        throws EJBException, RemoteException {
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
