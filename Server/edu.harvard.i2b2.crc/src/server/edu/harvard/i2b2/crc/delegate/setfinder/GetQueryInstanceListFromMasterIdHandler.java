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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryRunLocal;
import edu.harvard.i2b2.crc.ejb.QueryRunLocalHome;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import javax.ejb.CreateException;


/**
 * GetQueryInstanceListFromMasterIdHandler class
 * implements execute method
 * $Id: GetQueryInstanceListFromMasterIdHandler.java,v 1.6 2008/03/19 22:36:37 rk903 Exp $
 * @author rkuttan
 */
public class GetQueryInstanceListFromMasterIdHandler extends RequestHandler {
    MasterRequestType masterRequestType = null;
    PsmQryHeaderType headerType = null;

    /**
    * Constuctor which accepts i2b2 request message xml
    * @param requestXml
    * @throws I2B2Exception
    */
    public GetQueryInstanceListFromMasterIdHandler(String requestXml)
        throws I2B2Exception {
        try {
            headerType = (PsmQryHeaderType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType.class);
            masterRequestType = (MasterRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType.class);
            this.setDataSourceLookup(requestXml);
            
        } catch (JAXBUtilException jaxbUtilEx) {
            throw new I2B2Exception("Error ", jaxbUtilEx);
        }
    }

    /**
      * Perform operation for the given request
      * using business class(ejb) and return response
      */
    public BodyType execute() throws I2B2Exception {
        //		 call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

        String responseString = null;
        BodyType bodyType = new BodyType();
        InstanceResponseType instanceResponseType = null;
        try {
            QueryRunLocalHome queryRunLocalHome = qpUtil.getQueryRunLocalHome();
            QueryRunLocal queryRunLocal = queryRunLocalHome.create();
            UserType userType = headerType.getUser();
            String userId = null;

            if (userType != null) {
                userId = userType.getLogin();
            }

            instanceResponseType = queryRunLocal.getQueryInstanceFromMasterId(getDataSourceLookup(),userId,masterRequestType);
            instanceResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.DONE_TYPE, "DONE"));

//            ResponseMessageType responseMessageType = new ResponseMessageType();
//            responseMessageType.setMessageBody(bodyType);
//            responseString = getResponseString(responseMessageType);
        } catch (I2B2Exception e) { 
        	instanceResponseType = new InstanceResponseType();
        	instanceResponseType.setStatus(this.buildCRCStausType(RequestHandlerDelegate.ERROR_TYPE, e.getMessage()));
        } catch (ServiceLocatorException e) {
            log.error(e);
            throw new I2B2Exception("Servicelocator exception", e);
        } catch (CreateException e) {
            log.error(e);
            throw new I2B2Exception("Ejb create exception", e);
        } finally { 
        	 edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
             bodyType.getAny().add(of.createResponse(instanceResponseType));
        }

        return bodyType;
    }
}
