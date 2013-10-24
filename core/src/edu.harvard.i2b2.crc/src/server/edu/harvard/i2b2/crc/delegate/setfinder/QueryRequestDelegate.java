/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.setfinder;

import java.io.StringWriter;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.InfoType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.PollingUrlType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;

import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmRequestTypeType;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.delegate.pm.PMResponseMessage;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Setfinder query request delegate class
 * $Id: QueryRequestDelegate.java,v 1.15 2007/09/21 17:07:55 rk903 Exp $
 * @author rkuttan
 */
public class QueryRequestDelegate extends RequestHandlerDelegate {
   
	/** log **/
    protected final Log log = LogFactory.getLog(getClass());

    
	/**
	 * @see edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate#handleRequest(java.lang.String)
	 */
	public String handleRequest(String requestXml) throws I2B2Exception {
        PsmQryHeaderType headerType = null;
        String response = null;
        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

        try {
            JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);

            if (jaxbElement == null) {
                throw new I2B2Exception("Request is null after unmashall" +
                    requestXml);
            }

            RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
            BodyType bodyType = requestMessageType.getMessageBody();

            
            if (bodyType == null) {
                log.error("null value in body type");
                throw new I2B2Exception("null value in body type");
            }
            //Call PM cell to validate user
            ProjectType projectType = null;
            StatusType procStatus = null;
            try {
            	SecurityType securityType = null;
            	if (requestMessageType.getMessageHeader() != null ){
            		if (requestMessageType.getMessageHeader().getSecurity() != null) {
            			securityType = requestMessageType.getMessageHeader().getSecurity();
            		}
            	}
            	if (securityType == null) {
            		procStatus = new StatusType();
            		procStatus.setType("ERROR");
            		procStatus.setValue("Request message missing user/password");
            		response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            		return response;
            	}

            	//String pmResponse = PMServiceDriver.checkValidUser(securityType);
            	projectType = PMServiceDriver.checkValidUser(securityType);
            	
            	if (projectType == null) { 
            		procStatus = new StatusType();
            		procStatus.setType("ERROR");
            		procStatus.setValue("Invalid user/password for the given domain");
            		response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            		return response;
            	}
            	
            	log.info("project name from PM " + projectType.getName());
            	log.info("project id from PM " + projectType.getId());
            	log.info("Project role from PM " + projectType.getRole().get(0));
            } catch (AxisFault e) {
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Could not connect to server");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            } catch (I2B2Exception e) {
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Message error connecting Project Management cell");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            } catch (JAXBUtilException e) {
            	procStatus = new StatusType();
            	procStatus.setType("ERROR");
            	procStatus.setValue("Message error from Project Management cell");
            	response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            	return response;
            }
            
            JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
            headerType = (PsmQryHeaderType) unWrapHelper.getObjectByClass(bodyType.getAny(),
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            BodyType responseBodyType = null;
            if (headerType.getRequestType()
                              .equals(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID)) {
                GetQueryMasterListFromUserIdHandler handler = new GetQueryMasterListFromUserIdHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                    .equals(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_GROUP_ID)) {
            		//check if user have right permission to access this request
            		if (projectType != null && projectType.getRole().size()>0) { 
            			if ((!projectType.getRole().contains("ADMIN"))  && (!projectType.getRole().contains("MANAGER"))) { 
            				//Not authorized
            				procStatus = new StatusType();
            				procStatus.setType("ERROR");
                			procStatus.setValue("Authorization failure, should have Admin role");
            				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            				return response;
            			}
            		}
            		else { 
            			//Not authorized
            			procStatus = new StatusType();
            			procStatus.setType("ERROR");
            			procStatus.setValue("Authorization failure, should have Admin role");
            			response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, bodyType);
            			return response;
            		}
            		
            		GetQueryMasterListFromGroupIdHandler handler = new GetQueryMasterListFromGroupIdHandler(requestXml);
            		responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION)) {
                RunQueryInstanceFromQueryDefinitionHandler handler = new RunQueryInstanceFromQueryDefinitionHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_MASTER_ID)) {
                RunQueryInstanceFromQueryMasterHandler handler = new RunQueryInstanceFromQueryMasterHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID)) {
                GetQueryResultInstanceListFromQueryInstanceIdHandler handler = new GetQueryResultInstanceListFromQueryInstanceIdHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_GET_QUERY_INSTANCE_LIST_FROM_QUERY_MASTER_ID)) {
                GetQueryInstanceListFromMasterIdHandler handler = new GetQueryInstanceListFromMasterIdHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID)) {
                GetRequestXmlFromQueryMasterIdHandler handler = new GetRequestXmlFromQueryMasterIdHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_DELETE_QUERY_MASTER)) {
                DeleteQueryMasterHandler handler = new DeleteQueryMasterHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PsmRequestTypeType.CRC_QRY_RENAME_QUERY_MASTER)) {
                RenameQueryMasterHandler handler = new RenameQueryMasterHandler(requestXml);
                responseBodyType = handler.execute();
            }
            procStatus = new StatusType();
            procStatus.setType("DONE");
            procStatus.setValue("DONE");
            
            response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, responseBodyType);
            
            
        } catch (JAXBUtilException e) {
        	log.error("JAXBUtilException",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(requestXml + "\n\n" + StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(null, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
            //throw new I2B2Exception("JAXBUtil exception",e);
        }catch (I2B2Exception e) {
        	log.error("I2B2Exception",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
        }catch (Throwable e) { 
        	log.error("Throwable",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
		}
        

        return response;
    }
	
	
	
	
	 
	 	
	 	    
}