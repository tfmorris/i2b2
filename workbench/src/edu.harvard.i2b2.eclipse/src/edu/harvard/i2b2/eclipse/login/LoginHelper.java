/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     wwg0 02/08/2006 LoginD helper class with business rules for login 
 * calls Selectweb service getClientConfig method 
 * populates UserInfoBean
 * User file webServer/var/www/html/queryToolConfig/[userid]i2b2ClientConfig.xml
 * Sets XML string to System.property ExplorerConfigurationXML,
 * System.property user to userid, 
 * System.property pass to password for TomCat authentication
 * System.property datamartHost, ...Password, ..Service, ..User, and ..Password
 */

package edu.harvard.i2b2.eclipse.login;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.login.ws.GetUserConfigurationRequestMessage;
import edu.harvard.i2b2.pm.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ResponseType;
import edu.harvard.i2b2.pm.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.pm.datavo.pm.GetUserConfigurationType;
//import edu.harvard.i2b2.common.pm.UserInfoBean;

/**
 * @author 
 * 
 */


public class LoginHelper {

	private static Log log = LogFactory.getLog(LoginHelper.class.getName());

	private String msg;

	// class to hold userInfo
	public UserInfoBean userInfoBean;

	// constructor
	public LoginHelper() {

		// class instance to hold user session variables
		userInfoBean = UserInfoBean.getInstance();
	}


	/**
	 * Function to convert pm requestVdo to OMElement
	 * 
	 * @param requestVdo   String requestVdo to send to pm web service
	 * @return An OMElement containing the pm web service requestVdo
	 */
	public static OMElement getPmPayLoad(String requestVdo) throws Exception {
		OMElement method  = null;
		try {

			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMNamespace omNs = fac.createOMNamespace("http://www.i2b2.org/xsd/hive/msg",
			"i2b2");

			method = fac.createOMElement("request", omNs);

			StringReader strReader = new StringReader(requestVdo);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);

			StAXOMBuilder builder = new StAXOMBuilder(reader);
			//method = builder.getDocumentElement();
			OMElement lineItem = builder.getDocumentElement();
			method.addChild(lineItem);
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
			throw new Exception(e);
		}
		return method;
	}

	/**
	 * Function to send getChildren requestVdo to ONT web service
	 * 
	 * @param GetChildrenType  parentNode we wish to get data for
	 * @return A String containing the ONT web service response 
	 */

	public  UserInfoBean getUserInfo(String userid, String password, String projectID, String project, boolean demo) throws Exception{
		//String response = null;
		try {
			GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage(userid, password, project);

			GetUserConfigurationType userConfig = new GetUserConfigurationType();
			userConfig.getProject().add(project);

			String getChildrenRequestString = null;
			if (demo)
			{
				setUserInfo(getPMDemoString());
			}
			else
			{
				getChildrenRequestString = reqMsg.doBuildXML(userConfig);

			OMElement getPm = getPmPayLoad(getChildrenRequestString);


			Options options = new Options();
			options.setTo( new EndpointReference(projectID + "getServices"));

			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setTimeOutInMilliSeconds(50000);

			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);

			OMElement result = sender.sendReceive(getPm);

			if (result != null) {
				String response = result.getFirstElement().toString();
				setUserInfo(response);
				System.setProperty("user", userid);
				System.setProperty("pass", password);
			}
			}
		} catch (AxisFault e) {
			log.error(e.getMessage());
			//setMsg(e.getMessage());
			setMsg("Project Management Cell is unavailable for login");
			//e.printStackTrace();
			//throw new Exception(e);
		} catch (Exception e) {
			log.error(e.getMessage());
			setMsg(e.getMessage());
			//throw new Exception(e);
		}
		return userInfoBean;
	}


	public void setUserInfo(String responseXML) throws Exception {
		JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
				"edu.harvard.i2b2.pm.datavo.pm",
				"edu.harvard.i2b2.pm.datavo.i2b2message"
		});
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
		ResponseMessageType responseMessageType = (ResponseMessageType) jaxbElement.getValue();

		String procStatus = responseMessageType.getResponseHeader().getResultStatus().getStatus().getType();
		String procMessage = responseMessageType.getResponseHeader().getResultStatus().getStatus().getValue();

		if(procStatus.equals("ERROR")){
			setMsg(procMessage);				
		}
		else if(procStatus.equals("WARNING")){
			setMsg(procMessage);
		}	else {

			BodyType bodyType = responseMessageType.getMessageBody();
			JAXBUnWrapHelper helper = new JAXBUnWrapHelper(); 
			ResponseType response = (ResponseType)helper.getObjectByClass(bodyType.getAny(), ResponseType.class);

			userInfoBean.setEnvironment(response.getConfigure().getEnvironment());
			userInfoBean.setUserName(response.getConfigure().getUser().getUserName());
			userInfoBean.setUserFullName(response.getConfigure().getUser().getFullName());
			userInfoBean.setUserPassword(response.getConfigure().getUser().getPassword());
			userInfoBean.setUserDomain(response.getConfigure().getUser().getDomain());
			userInfoBean.setHelpURL(response.getConfigure().getHelpURL());

			//Save Global variables in properties
			for (ParamType param :response.getConfigure().getGlobalData().getParam())
				userInfoBean.setGlobals(param.getName(), param.getValue());

			//Save projects			
			userInfoBean.setProjects( response.getConfigure().getUser().getProject());

			//Save Cell
			userInfoBean.setCellDatas(response.getConfigure().getCellDatas());
		}
	}


	/**
	 * Test code to generate a PM requestPdo String for a sample PM report
	 * called by main below
	 *
	 * @return A String containing the sample PM report
	 */
	public static String getPMDemoString() throws Exception {
		BufferedReader bufRead = new BufferedReader(new FileReader("i2b2workbench.xml"));
		StringBuffer queryStr = new StringBuffer();
		String line = null;

		while ((line = bufRead.readLine()) != null) {
			queryStr.append(line + "\n");
		}
		bufRead.close();


		return queryStr.toString();
	}

	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

}
