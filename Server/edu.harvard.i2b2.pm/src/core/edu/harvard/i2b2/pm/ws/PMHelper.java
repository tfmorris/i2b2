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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Helper class to extract PM report and build
 * PM response.
 *
 */
public class PMHelper {
    private static Log log = LogFactory.getLog(PMHelper.class);

    /**
     *
     * @param requestPdo
     * @return String i2b2 message which contain patient data object message
     *                         with each PFT concepts as ObservationFact element.
     *
     * @throws I2B2Exception
     */
    public static String extractPFT(String requestPdo)
        throws I2B2Exception {
        String responsePdo = null;

        log.debug("PM ResponsePdo = " + responsePdo);

        return responsePdo;
    }
}
