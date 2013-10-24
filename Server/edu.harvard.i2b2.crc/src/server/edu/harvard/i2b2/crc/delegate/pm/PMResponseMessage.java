/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.pm;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;

public class PMResponseMessage {

	public static final String THIS_CLASS_NAME = PMResponseMessage.class
			.getName();
	private Log log = LogFactory.getLog(THIS_CLASS_NAME);
	private ResponseMessageType pmRespMessageType = null;

	public PMResponseMessage() {
	}

	public StatusType processResult(String response) throws JAXBUtilException {
		StatusType status = null;
		try {

			JAXBElement jaxbElement = CRCJAXBUtil.getJAXBUtil()
					.unMashallFromString(response);
			pmRespMessageType = (ResponseMessageType) jaxbElement.getValue();

			// Get response message status
			ResponseHeaderType responseHeader = pmRespMessageType
					.getResponseHeader();
			status = responseHeader.getResultStatus().getStatus();
			String procStatus = status.getType();
			String procMessage = status.getValue();

			if (procStatus.equals("ERROR")) {
				log.info("Error reported by CRC web Service " + procMessage);
			} else if (procStatus.equals("WARNING")) {
				log.info("Warning reported by CRC web Service" + procMessage);
			}

		} catch (JAXBUtilException e) {
			log.error("Unable to read i2b2 response message type ", e);
			throw e;
		}
		return status;
	}

	public ConfigureType readUserInfo() throws JAXBUtilException {
		ConfigureType pmResponse = null;

		BodyType bodyType = pmRespMessageType.getMessageBody();
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		pmResponse = (ConfigureType) helper.getObjectByClass(bodyType.getAny(),
				ConfigureType.class);

		return pmResponse;
	}

}
