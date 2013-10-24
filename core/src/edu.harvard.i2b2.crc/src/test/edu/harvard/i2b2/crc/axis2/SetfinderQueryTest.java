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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterDeleteRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRenameRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PsmRequestTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.StatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType;

/**
 * Class to test different setfinder request's 
 * @author rkuttan
 */
public class SetfinderQueryTest  extends CRCAxisAbstract {

	private static QueryMasterType queryMaster = null; 
	private static QueryInstanceType queryInstance = null;
	private static String testFileDir = null;
	
	private static  String setfinderUrl = 
			"http://localhost:8080/i2b2/rest/QueryToolService/request";			
		
	@BeforeClass 
	public static void runQueryInstanceFromQueryDefinition() throws Exception  {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);
		if (!(testFileDir != null && testFileDir.trim().length()>0)) {
			throw new Exception("please provide test file directory info -Dtestfiledir");
		}
		//read test file and store query master;
		String filename = testFileDir + "\\setfinder_query.xml";
		String requestString = getQueryString(filename);
		OMElement requestElement = convertStringToOMElement(requestString); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		System.out.println(responseElement.toString());
		//read test file and store query instance ;
		//unmarshall this response string 
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterInstanceResultResponseType masterInstanceResult = (MasterInstanceResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterInstanceResultResponseType.class);
		queryMaster = masterInstanceResult.getQueryMaster();
		queryInstance = masterInstanceResult.getQueryInstance();
		assertNotNull("not null check for querymaster",queryMaster);
		assertNotNull("not null check for queryinstance",queryInstance);
	}
	
	
	@Test
	public void testGetQueryDefinitionFromQueryMaster() throws Exception {
		System.out.println(queryMaster.getQueryMasterId());
		//create request type
		//call 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID);
		MasterRequestType masterRequestType = new MasterRequestType();
		masterRequestType.setQueryMasterId(queryMaster.getQueryMasterId());
		
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,masterRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		System.out.println(responseElement.toString());
		displayIfError(r,responseElement.toString());
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterResponseType masterResponseType = (MasterResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterResponseType.class);
		StatusType.Condition condition = masterResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertNotNull("not null check for query definition",masterResponseType.getQueryMaster().get(0).getRequestXml().getContent().get(0));
		
		Element element = (Element)masterResponseType.getQueryMaster().get(0).getRequestXml().getContent().get(0);
		if (element != null) { 
			System.out.print("element not null");
		}
		
	    String domString = edu.harvard.i2b2.common.util.xml.XMLUtil.convertDOMElementToString(element);
		System.out.println("string output"+domString);
		
		JAXBContext jc1 = JAXBContext.newInstance(edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory.class);
		Unmarshaller unMarshaller = jc1.createUnmarshaller();
		JAXBElement jaxbElement = (JAXBElement)unMarshaller.unmarshal(new StringReader(domString));
		
		System.out.println("query definition name " + ((QueryDefinitionType)jaxbElement.getValue()).getQueryName());
	}
	
	private void displayIfError(ResponseMessageType r,String responseXml) { 
		if (r != null && r.getResponseHeader().getResultStatus().getStatus().getType().equals("ERROR")) { 
			System.out.println(responseXml);
		}
	}

	
	@Test
	public void testGetQueryMasterListFromUserId() throws Exception {
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID);
		
		UserRequestType userRequestType = new UserRequestType();
		userRequestType.setUserId(queryMaster.getUserId());
		userRequestType.setFetchSize(100);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,userRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterResponseType masterResponseType = (MasterResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterResponseType.class);
		StatusType.Condition condition = masterResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query master list  size > 0 ",masterResponseType.getQueryMaster().size()>0);
	}
	
	@Test
	public void testGetQueryMasterListFromGroupId() throws Exception { 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_GROUP_ID);
		
		UserRequestType userRequestType = new UserRequestType();
		userRequestType.setGroupId(queryMaster.getGroupId());
		userRequestType.setFetchSize(100);
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,userRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		
		System.out.println("by group " + responseElement.toString());
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());		
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterResponseType masterResponseType = (MasterResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterResponseType.class);
		StatusType.Condition condition = masterResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query master list  size > 0 ",masterResponseType.getQueryMaster().size()>0);	}
	
	@Ignore
	public void testGetQueryMasterFromMaster() { 
	}
	
	
	@Test
	public void testGetQueryInstanceFromMaster() throws Exception { 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_INSTANCE_LIST_FROM_QUERY_MASTER_ID);
		
		MasterRequestType masterRequestType = new MasterRequestType();
		masterRequestType.setQueryMasterId(queryMaster.getQueryMasterId());
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,masterRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		

		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());		
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		InstanceResponseType instanceResponseType = (InstanceResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),InstanceResponseType.class);
		StatusType.Condition condition = instanceResponseType .getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query instance list  size > 0 ",instanceResponseType.getQueryInstance().size()>0);	
	}
	
	
	
	@Test
	public void testGetQueryResultInstanceFromQueryInstance() throws Exception { 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID);
		
		InstanceRequestType instanceRequestType = new InstanceRequestType();
		instanceRequestType.setQueryInstanceId(queryInstance.getQueryInstanceId());
		
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,instanceRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);

		System.out.println(responseElement.toString());
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());		
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		ResultResponseType resultResponseType = (ResultResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),ResultResponseType.class);
		StatusType.Condition condition = resultResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query result instance list  size > 0 ",resultResponseType.getQueryResultInstance().size()>0);
	}
	
	
	@Test
	public void testRenameQueryMaster() throws Exception { 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_RENAME_QUERY_MASTER);
		
		MasterRenameRequestType renameRequestType = new MasterRenameRequestType();
		renameRequestType.setQueryMasterId(queryMaster.getQueryMasterId());
		renameRequestType.setQueryName(queryMaster.getName() + "rename query");
		renameRequestType.setUserId(queryMaster.getUserId())	;
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,renameRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());		
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterResponseType masterResponseType = (MasterResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterResponseType.class);
		StatusType.Condition condition = masterResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query master list  size > 0 ",masterResponseType.getQueryMaster().size()>0);
	}
	
	@Test
	public void testDeleteQueryMaster() throws Exception { 
		PsmQryHeaderType requestHeaderType = new PsmQryHeaderType();
		requestHeaderType.setRequestType(PsmRequestTypeType.CRC_QRY_DELETE_QUERY_MASTER);
		
		MasterDeleteRequestType deleteRequestType = new MasterDeleteRequestType();
		deleteRequestType.setQueryMasterId(queryMaster.getQueryMasterId());
		deleteRequestType.setUserId(queryMaster.getUserId());
		
		RequestMessageType requestMessageType = buildRequestMessage(requestHeaderType,deleteRequestType);
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(of.createRequest(requestMessageType), strWriter);
		
		OMElement requestElement = convertStringToOMElement(strWriter.toString()); 
		OMElement responseElement = getServiceClient(setfinderUrl).sendReceive(requestElement);
		
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
		ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
		assertEquals("checking i2b2 message status 'DONE'","DONE",r.getResponseHeader().getResultStatus().getStatus().getType());		
		JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
		MasterResponseType masterResponseType = (MasterResponseType)helper.getObjectByClass(r.getMessageBody().getAny(),MasterResponseType.class);
		StatusType.Condition condition = masterResponseType.getStatus().getCondition().get(0);
		assertEquals("checking crc message status 'DONE'","DONE",condition.getType());
		assertTrue("checking query master list  size > 0 ",masterResponseType.getQueryMaster().size()>0);
		
	}
	
	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(SetfinderQueryTest.class);
	}
	
	
	
	
	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}
	
	
	
	public static RequestMessageType buildRequestMessage(PsmQryHeaderType requestHeaderType, RequestType requestType) {
		//create body type
		BodyType bodyType = new BodyType();
		ObjectFactory of = new ObjectFactory();
		bodyType.getAny().add(of.createPsmheader(requestHeaderType));
		bodyType.getAny().add(of.createRequest(requestType));
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageHeader(generateMessageHeader());
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setRequestHeader(generateRequestHeader());
		return requestMessageType;
	}

}
