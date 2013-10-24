/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.axis2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;


/**
 * This class test pdo/timeline requests.
 * @author rk903
 */
public class CRCQueryClientTest {
    private static String setfinderUrl = "http://localhost:8080/i2b2/rest/QueryToolService/request";
    private static String pdoUrl = "http://localhost:8080/i2b2/rest/QueryToolService/pdorequest";
    private static String testFileDir = null;

    @BeforeClass
    public static void init() throws Exception {
        testFileDir = System.getProperty("testfiledir");
        System.out.println("test file dir " + testFileDir);

        if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
            throw new Exception(
                "please provide test file directory info -Dtestfiledir");
        }
    }

    @Ignore
    @Test(timeout = 180000)
    public void testSetfinderMessage() throws Exception {
        //read test file and store query master;
        String filename = testFileDir + "\\setfinder_query.xml";
        String requestString = PdoQueryTest.getQueryString(filename);
        System.out.println("test file dir " + testFileDir);

        OMElement requestElement = PdoQueryTest.convertStringToOMElement(requestString);
        OMElement responseElement = PdoQueryTest.getServiceClient(setfinderUrl)
                                                .sendReceive(requestElement);

        //read test file and store query instance ;
        //unmarshall this response string 
        JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
                                              .unMashallFromString(responseElement.toString());
        ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
        assertEquals("checking i2b2 message status 'DONE'", "DONE",
                r.getResponseHeader().getResultStatus().getStatus().getType());


        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType) helper.getObjectByClass(r.getMessageBody()
                                                                                                                            .getAny(),
                MasterInstanceResultResponseType.class);
        QueryResultInstanceType queryResultInstance = masterInstanceResult.getQueryResultInstance()
                                                                          .get(0);
        assertNotNull(queryResultInstance);
    }

    @Test
    public void testPDOMessage() throws Exception {
        String filename = testFileDir + "\\pdo_query.xml";
        String requestString = PdoQueryTest.getQueryString(filename);
        System.out.println("test file dir " + testFileDir);

        OMElement requestElement = PdoQueryTest.convertStringToOMElement(requestString);
        OMElement responseElement = PdoQueryTest.getServiceClient(pdoUrl)
                                                .sendReceive(requestElement);

        //read test file and store query instance ;
        //unmarshall this response string 
        JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
                                              .unMashallFromString(responseElement.toString());
        ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
        assertEquals("checking i2b2 message status 'DONE'", "DONE",
            r.getResponseHeader().getResultStatus().getStatus().getType());

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        PatientDataResponseType patientDataResponseType = (PatientDataResponseType) helper.getObjectByClass(r.getMessageBody()
                                                                                                             .getAny(),
                PatientDataResponseType.class);
        assertTrue("checking patient set size > 0 ",
            patientDataResponseType.getPatientData().getPatientSet().getPatient()
                                   .size() > 0);
        assertTrue("checking observatiob set size > 0",
            patientDataResponseType.getPatientData().getObservationSet().get(0)
                                   .getObservation().size() > 0);
    }
    
    public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(CRCQueryClientTest.class);
	}
    
}
