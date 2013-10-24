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

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.CRCTimeOutException;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryExecutorDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * This class is the message driven bean to handle UploadMessages 
 * on the queue and handing them to the Upload coordinator.
 * 
 * @author rkuttan
 * 
 * @ejb.bean name="querytool.QueryExecutorMDB" 
 * 		 display-name="QueryTool Executor MDB" 
 * 		description="QueryTool Executor"
 *           destination-type="javax.jms.Queue"
 *           destination-jndi-name="jms.querytool.QueryExecutor"
 *           subscription-durability="Durable" 
 *           transaction-type="Bean"
 *           jndi-name="ejb.querytool.QueryExecutorMDB"
 *           local-jndi-name="ejb.querytool.QueryExecutorMDBLocal"
 *           view-type="both"
 *          
 *          
 * 
 * @ejb.resource-ref res-ref-name="jms.QueueFactory" res-type="Required"
 *                   res-auth="Container"
 * 
 *
 * @ejb.transaction type="Required"
 *   
 */
public class QueryExecutorMDB implements MessageDrivenBean, MessageListener {

	

	private MessageDrivenContext sessionContext;

	private static Log log = LogFactory.getLog(QueryExecutorMDB.class);

	public static final String  SMALL_QUEUE = "SMALL_QUEUE";
	public static final String  MEDIUM_QUEUE = "MEDIUM_QUEUE";
	public static final String  LARGE_QUEUE = "LARGE_QUEUE";
	
   private String callingMDBName = SMALL_QUEUE;	

	

	/**
	 * Creates a new UploadProcessorMDB object.
	 */
	public QueryExecutorMDB() {
	}
	
	public QueryExecutorMDB(MessageDrivenContext sessionContext,String callingMDBName) {
		this.sessionContext = sessionContext;
		this.callingMDBName  = callingMDBName ;
	} 

	/**
	 * Take the XML based message and delegate to 
	 * the system coordinator to  handle the 
	 * actual processing
	 * @param msg th JMS TextMessage 
	 * 				object containing XML data
	 */
	public void onMessage(Message msg) {
		MapMessage message = null;
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		Queue replyToQueue = null;
		UserTransaction transaction = sessionContext.getUserTransaction();
		//default timeout three minutes
		int transactionTimeout = 180;
		try {
			
			if (callingMDBName.equalsIgnoreCase(QueryManagerBeanUtil.MEDIUM_QUEUE_NAME)) {
				//four hours
				transactionTimeout = 14400;
			} else if (callingMDBName.equalsIgnoreCase(QueryManagerBeanUtil.LARGE_QUEUE_NAME)) {
				//twelve hours
				transactionTimeout = 43200;
			} 
			
			transaction.setTransactionTimeout(transactionTimeout);
			
			
			transaction.begin();
			message = (MapMessage) msg;
			String sessionId = msg.getJMSCorrelationID();
			replyToQueue = (Queue) msg.getJMSReplyTo();
			log.debug("Extracting the message [" + msg.getJMSMessageID());
			String patientSetId = "";
			transaction.commit();
			if (message != null) {
				String sqlString = message.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM);
				String queryInstanceId = message.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM);
				patientSetId = message.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM);
				String xmlRequest = message.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM);
				
				String dsLookupDomainId = message.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID);
				String dsLookupProjectId = message.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID);
				String dsLookupOwnerId = message.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID);
				
				DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);
			
				DataSourceLookupHelper dataSourceHelper = new DataSourceLookupHelper();
				DataSourceLookup dsLookup = dataSourceHelper.matchDataSource(dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);
				
				IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
				
				SetFinderDAOFactory sfDAOFactory = daoFactory.getSetFinderDAOFactory();
				try { 
					 
					patientSetId = processQueryRequest(transaction,transactionTimeout,dsLookup,sfDAOFactory ,xmlRequest,sqlString, sessionId,queryInstanceId,patientSetId);
					log.debug("QueryExecutorMDB completed processing query instance [" + queryInstanceId + "]");
				} catch (CRCTimeOutException daoEx) {
					//catch this error and ignore. send general reply message.  
					log.error(daoEx.getMessage(),daoEx);
					if (callingMDBName.equalsIgnoreCase(LARGE_QUEUE)) { 
						
						// set status to error
						setQueryInstanceStatus(sfDAOFactory,queryInstanceId, 4) ;
						
					} else { 
						//send message to next queue and if the there is no next queue then update query instance to error
						tryNextQueue(sfDAOFactory,sessionId, message,queryInstanceId);
					}
				} catch (I2B2DAOException daoEx) {
					//catch this error and ignore. send general reply message.  
					log.error(daoEx.getMessage(),daoEx);
				}
			}
			
			sendReply(sessionId,patientSetId, replyToQueue);
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				transaction.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			log.error("Error extracting message", ex);
		} finally {
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			qmBeanUtil.closeAll(sender, null, conn, session);
		}
	}

	private void sendReply(String sessionId,String patientSetId, Queue replyToQueue) throws JMSException, ServiceLocatorException { 
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender sender = null;
		try { 
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		ServiceLocator serviceLocator = ServiceLocator.getInstance();
		conn = serviceLocator.getQueueConnectionFactory(QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
				.createQueueConnection();
		session = conn.createQueueSession(false,
				javax.jms.Session.AUTO_ACKNOWLEDGE);
		MapMessage mapMessage = session.createMapMessage();
		//mapMessage.setString("para1", responseXML);
		log.debug("message session id " + sessionId);
		mapMessage.setJMSCorrelationID(sessionId);
		mapMessage.setString(QueryManagerBeanUtil.QT_QUERY_RESULT_INSTANCE_ID_PARAM, patientSetId);
		sender = session.createSender(replyToQueue);
		sender.send(mapMessage);
		} catch (JMSException jmse) { 
			throw jmse;
		}finally { 
			QueryManagerBeanUtil qmBeanUtil = new QueryManagerBeanUtil();
			qmBeanUtil.closeAll(sender, null, conn, session);
		}
		
	}
	
	private void tryNextQueue(SetFinderDAOFactory sfDAOFactory,String sessionId,MapMessage msg,String queryInstanceId) throws JMSException, ServiceLocatorException {
		String jmsQueueName = null;
		
		
		
		//check which queue is this
		if (callingMDBName.equalsIgnoreCase(SMALL_QUEUE)) { 
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.MEDIUM_QUEUE_NAME;
			
		} else if (callingMDBName .equalsIgnoreCase(MEDIUM_QUEUE)) { 
			// set status to running
			jmsQueueName = QueryManagerBeanUtil.LARGE_QUEUE_NAME;
		}
		
		if (jmsQueueName != null) { 
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			ServiceLocator serviceLocator = ServiceLocator.getInstance();
			QueueConnection conn = serviceLocator.getQueueConnectionFactory(QueryManagerBeanUtil.QUEUE_CONN_FACTORY_NAME)
					.createQueueConnection();
			Queue responseQueue = serviceLocator.getQueue(QueryManagerBeanUtil.RESPONSE_QUEUE_NAME);
			Queue sendQueue = serviceLocator.getQueue(jmsQueueName);
			
			QueueSession session = conn.createQueueSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);
			String id = sessionId;
			String selector = "JMSCorrelationID='" + id + "'";
			QueueSender sender = session.createSender(sendQueue);
			MapMessage mapMsg = session.createMapMessage();
			mapMsg.setJMSCorrelationID(id);
			mapMsg.setJMSReplyTo(responseQueue);
	
			mapMsg.setString(QueryManagerBeanUtil.XML_REQUEST_PARAM, msg.getString(QueryManagerBeanUtil.XML_REQUEST_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM , msg.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM, msg.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM, msg.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID, msg.getString(QueryManagerBeanUtil.DS_LOOKUP_DOMAIN_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID, msg.getString(QueryManagerBeanUtil.DS_LOOKUP_PROJECT_ID));
			mapMsg.setString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID, msg.getString(QueryManagerBeanUtil.DS_LOOKUP_OWNER_ID));
			
			sender.send(mapMsg);
		}
	}
		
	private String processQueryRequest(UserTransaction transaction,int transactionTimeout,DataSourceLookup dsLookup,SetFinderDAOFactory sfDAOFactory,String xmlRequest,String sqlString, String sessionId, String queryInstanceId, String patientSetId) 
		throws I2B2DAOException,I2B2Exception {
	
			//QueryRequestDao queryRequestDao = new QueryRequestDao();
			//returnedPatientSetId = queryRequestDao.getPatientCount(queryRequestXml, queryInstanceId,patientSetId);
			QueryDefinitionRequestType qdRequestType = getQueryDefinitionRequestType(xmlRequest);
			ResultOutputOptionListType resultOutputList = qdRequestType.getResultOutputList();
			DataSource dataSource = ServiceLocator.getInstance().getAppServerDataSource(dsLookup.getDataSource());
			QueryExecutorDao queryExDao = new QueryExecutorDao(dataSource,dsLookup);
			
			queryExDao.executeSQL(transaction,transactionTimeout,dsLookup, sfDAOFactory, sqlString, queryInstanceId, patientSetId,resultOutputList);
			return patientSetId;
	}
	
	
	  private QueryDefinitionRequestType getQueryDefinitionRequestType(String xmlRequest) throws I2B2Exception {
	        String queryName = null;
	        QueryDefinitionType queryDef = null;
	        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
	        JAXBElement jaxbElement = null;
	        QueryDefinitionRequestType queryDefReqType = null;
			try {
				jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);
				
	
		        if (jaxbElement == null) {
		            throw new I2B2Exception(
		                "null value in after unmarshalling request string ");
		        }
	
		        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
		        BodyType bodyType = requestMessageType.getMessageBody();
		        JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		        queryDefReqType = (QueryDefinitionRequestType) unWrapHelper.getObjectByClass(bodyType.getAny(),
		                QueryDefinitionRequestType.class);
			} catch (JAXBUtilException e) {
				log.error(e.getMessage(),e);
				throw new I2B2Exception(e.getMessage(),e);
			}
	        return queryDefReqType ;
	        
	        
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
	
	
	//--------------------------------
	//ejb functions
	//--------------------------------
	// Set the context.
	public void setMessageDrivenContext(MessageDrivenContext context) {
		this.sessionContext = context;
	}
	//ejb create
	public void ejbCreate() {
	}
	//ejb remove
	public void ejbRemove() throws EJBException {
	}
}
