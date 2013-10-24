/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.pdo;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.I2B2MessageResponseFactory;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.delegate.pm.PMResponseMessage;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceDriver;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;


/**
 * PDO query request delegate class
 * $Id: PdoQueryRequestDelegate.java,v 1.16 2007/09/14 19:33:37 rk903 Exp $
 * @author rkuttan
 */
public class PdoQueryRequestDelegate extends RequestHandlerDelegate {
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate#handleRequest(java.lang.String)
     */
    public String handleRequest(String requestXml) throws I2B2Exception {
        PdoQryHeaderType headerType = null;
        String response = null;
        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

        try {
            JAXBElement jaxbElement = jaxbUtil.unMashallFromString(requestXml);
            RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
            BodyType bodyType = requestMessageType.getMessageBody();

            if (bodyType == null) {
                log.error("null value in body type");
                throw new I2B2Exception("null value in body type");
            }

            //Call PM cell to validate user
            
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

            	ProjectType projectType = PMServiceDriver.checkValidUser(securityType);
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
            headerType = (PdoQryHeaderType) unWrapHelper.getObjectByClass(bodyType.getAny(),
                    edu.harvard.i2b2.crc.datavo.pdo.query.PdoQryHeaderType.class);

            BodyType responseBodyType = null;
            if (headerType.getRequestType()
                              .equals(PdoRequestTypeType.GET_PDO_FROM_INPUT_LIST)) {
                GetPDOFromInputListHandler handler = new GetPDOFromInputListHandler(requestXml);
                responseBodyType = handler.execute();
            } else if (headerType.getRequestType()
                                     .equals(PdoRequestTypeType.GET_OBSERVATIONFACT_BY_PRIMARY_KEY)) {
                GetObservationFactFromPrimaryKeyHandler handler = new GetObservationFactFromPrimaryKeyHandler(requestXml);
                responseBodyType = handler.execute();
            }
            procStatus = new StatusType();
            procStatus.setType("DONE");
            procStatus.setValue("DONE");
            
            response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, responseBodyType,true);
            
        } catch (JAXBUtilException e) {
        	log.error("JAXBUtil exception",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(requestXml + "\n\n" + StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(null, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
        } catch (I2B2Exception e) {
        	log.error("I2B2Exception",e);
        	StatusType procStatus = new StatusType();
        	procStatus.setType("ERROR");
        	procStatus.setValue(StackTraceUtil.getStackTrace(e));
        	try {
				response = I2B2MessageResponseFactory.buildResponseMessage(requestXml, procStatus, null);
			} catch (JAXBUtilException e1) {
				e1.printStackTrace();
			}
        } catch (Throwable e) { 
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