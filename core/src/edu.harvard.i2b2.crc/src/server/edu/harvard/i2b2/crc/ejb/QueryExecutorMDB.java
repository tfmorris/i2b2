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
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.crc.dao.setfinder.QueryRequestDao;
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

	

	

	/**
	 * Creates a new UploadProcessorMDB object.
	 */
	public QueryExecutorMDB() {
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
		try {
			transaction.begin();
			message = (MapMessage) msg;
			String sessionId = msg.getJMSCorrelationID();
			replyToQueue = (Queue) msg.getJMSReplyTo();
			log.debug("Extracting the message [" + msg.getJMSMessageID());
			String patientSetId = "";
			transaction.commit();
			if (message != null) {
				String queryRequestXML = message.getString(QueryManagerBeanUtil.QUERY_MASTER_GENERATED_SQL_PARAM);
				String queryInstanceId = message.getString(QueryManagerBeanUtil.QUERY_INSTANCE_ID_PARAM);
				patientSetId = message.getString(QueryManagerBeanUtil.QUERY_PATIENT_SET_ID_PARAM);
				try { 
					patientSetId = processQueryRequest(queryRequestXML, sessionId,queryInstanceId,patientSetId);
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
	
		
	private String processQueryRequest(String queryRequestXml, String sessionId, String queryInstanceId, String patientSetId) throws Exception {
		String returnedPatientSetId = "";
		try {

			QueryRequestDao queryRequestDao = new QueryRequestDao();
			returnedPatientSetId = queryRequestDao.getPatientCount(queryRequestXml, queryInstanceId,patientSetId);
		} catch (Exception e) {
			throw e;
		}
		return returnedPatientSetId;
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
