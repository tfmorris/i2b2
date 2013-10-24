/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */

package edu.harvard.i2b2.pm.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.pm.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.i2b2versionmessage.RequestMessageType;
import edu.harvard.i2b2.pm.datavo.pm.GetUserConfigurationType;

import javax.xml.stream.XMLStreamException;


import org.apache.axiom.om.OMElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;


/**
 * This is webservice skeleton class. It passes incoming report to PFT parser
 * and collects parsed pft concepts. Then these parsed concepts returned back to
 * webservice client in Patient Data Object XML format.
 *
 */
public class PMService {
	private static Log log = LogFactory.getLog(PMService.class);

	private static String msgVersion = "1.1";
	
	public OMElement getVersion(OMElement getPMDataElement)
	throws I2B2Exception, JAXBUtilException {
		log.error("Received Request PDO Element " + getPMDataElement);

		OMElement returnElement = null;


		if (getPMDataElement == null) {
			log.error("Incoming Version request is null");
			throw new I2B2Exception("Incoming Version request is null");
		}


		VersionMessage servicesMsg = new VersionMessage(getPMDataElement.toString());

        String version = servicesMsg.getRequestMessageType().getMessageBody().getGetMessageVersion().toString();
        if (version.equals(""))
        {
			edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType pmDataResponse = new edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType();

			edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType.MessageBody mb = new edu.harvard.i2b2.pm.datavo.i2b2versionmessage.ResponseMessageType.MessageBody();
			mb.setI2B2MessageVersion(msgVersion);
			pmDataResponse.setMessageBody(mb);
        	
			String xmlMsg = MessageFactory.convertToXMLString(pmDataResponse);

	        try {
	            returnElement = MessageFactory.createResponseOMElementFromString(xmlMsg);
	            log.debug("my pm repsonse is: " + pmDataResponse);
	            log.debug("my return is: " + returnElement);
	        } catch (XMLStreamException e) {
	            log.error("Error creating OMElement from response string " +
	            		pmDataResponse, e);
	        }

        }
		
		return returnElement;

	}
	
	/**
	 * This function is main webservice interface to get pulmonary data from
	 * pulmonary report. It uses AXIOM elements(OMElement) to conveniently parse
	 * xml messages.
	 *
	 * It excepts incoming request in i2b2 message format, which wraps PFT
	 * report inside patientdata object. The response is also will be in i2b2
	 * message, which will wrap patientdata object. Patient data object will
	 * have all the extracted pft concepts from the report.
	 *
	 *
	 * @param getServices
	 * @return OMElement in i2b2message format
	 * @throws PortletServiceNotFoundException 
	 * @throws PortletServiceUnavailableException 
	 * @throws Exception
	 */
	public OMElement getServices(OMElement getPMDataElement)
	throws I2B2Exception {
		log.error("Received Request PDO Element " + getPMDataElement);

		OMElement returnElement = null;


		if (getPMDataElement == null) {
			log.error("Incoming PM request is null");
			throw new I2B2Exception("Incoming PM request is null");
		}

		ServicesMessage servicesMsg = new ServicesMessage(getPMDataElement.toString());
		long waitTime = 0;

		if (servicesMsg.getRequestMessageType() != null) {
			if (servicesMsg.getRequestMessageType().getRequestHeader() != null) {
				waitTime = servicesMsg.getRequestMessageType()
				.getRequestHeader()
				.getResultWaittimeMs();
			}
		}


		//do PM processing inside thread, so that  
		// service could sends back message with timeout error.
		ExecutorRunnable er = new ExecutorRunnable();
		//er.setInputString(requestElementString);
		er.setRequestHandler(new RequestHandler(servicesMsg));
		Thread t = new Thread(er);
		String pmDataResponse = null;
		ResponseMessageType responseMsgType = null;
		
		synchronized (t) {
			t.start();

			try {
				if (waitTime > 0) {
					t.wait(waitTime);
				} else {
					t.wait();
				}
				pmDataResponse = er.getOutputString();

				if (pmDataResponse == null) {
					if (er.getJobException() != null) {
						pmDataResponse = "";
						throw new I2B2Exception("Portal is not property configured.");
					} 
					else if (er.isJobCompleteFlag() == false) {
						String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
						waitTime +
						" elapsed, try again with increased value";
						log.error(timeOuterror);

						responseMsgType = MessageFactory.doBuildErrorResponse(null,
								timeOuterror);
						pmDataResponse = MessageFactory.convertToXMLString(responseMsgType);
					} 
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new I2B2Exception("Thread error while running PM job " +
						getPMDataElement, e);
			} finally {
				t.interrupt();
				er = null;
				t = null;
			}
		}
		
        try {
            returnElement = MessageFactory.createResponseOMElementFromString(pmDataResponse);
            log.debug("my pm repsonse is: " + pmDataResponse);
            log.debug("my return is: " + returnElement);
        } catch (XMLStreamException e) {
            log.error("Error creating OMElement from response string " +
            		pmDataResponse, e);
        }
        
		return returnElement;
	}
}
