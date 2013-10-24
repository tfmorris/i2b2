/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Wensong Pan
 *     
 */
package edu.harvard.i2b2.exportData.ui;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JOptionPane;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.harvard.i2b2.eclipse.UserInfoBean; 
import edu.harvard.i2b2.exportData.data.PDOItem;
import edu.harvard.i2b2.exportData.data.PDORequestMessageFactory;
import edu.harvard.i2b2.exportData.dataModel.TimelineRow;
												
												// edu.harvard.i2b2.smlib.Lib;
import edu.harvard.i2b2.exportData.utils.MessageUtil;

public class PDOQueryClient {
	private static final Log log = LogFactory.getLog(PDOQueryClient.class);

	private static EndpointReference targetEPR;

	private static String getPDOServiceName() {
		return UserInfoBean.getInstance().getCellDataUrl("CRC") + "pdorequest";
	}

	private static String getPDQServiceName() {
		return UserInfoBean.getInstance().getCellDataUrl("CRC") + "request";
	}

	public static OMElement getQueryPayLoad(String str) throws Exception {
		StringReader strReader = new StringReader(str);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		// System.out.println("Line item string " + lineItem.toString());
		return lineItem;
	}

	public static String sendPDQQueryRequestREST(String XMLstr) {
		try {
			OMElement payload = getQueryPayLoad(XMLstr);
			Options options = new Options();

			targetEPR = new EndpointReference(getPDQServiceName());
			options.setTo(targetEPR);

			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(600000));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(
					600000));

			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);

			OMElement responseElement = sender.sendReceive(payload);
			// System.out.println("Client Side response " +
			// responseElement.toString());

			MessageUtil.getInstance().setRequest("URL: " + getPDQServiceName() + "\n" + XMLstr);
	        MessageUtil.getInstance().setResponse("URL: " + getPDQServiceName() + "\n" + responseElement.toString());

			return responseElement.toString();

		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									null,
									"Trouble with connection to the remote server, "
											+ "this is often a network error, please try again",
									"Network Error",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String sendPDQQueryRequestSOAP(String XMLstr) {
		try {
			ServiceClient sender = DataExporterQueryServiceClient
					.getServiceClient();
			OperationClient operationClient = sender
					.createClient(ServiceClient.ANON_OUT_IN_OP);

			// creating message context
			MessageContext outMsgCtx = new MessageContext();
			// assigning message context's option object into instance variable
			Options opts = outMsgCtx.getOptions();
			// setting properties into option

			targetEPR = new EndpointReference(UserInfoBean.getInstance()
					.getCellDataUrl("CRC"));
			log.debug(targetEPR);
			opts.setTo(targetEPR);
			opts.setAction("http://rpdr.partners.org/GetPreviousQueryList");
			opts.setTimeOutInMilliSeconds(180000);

			// log.debug(requestString);

			SOAPEnvelope envelope = null;
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			envelope = fac.getDefaultEnvelope();
			OMNamespace omNs = fac.createOMNamespace(
					"http://rpdr.partners.org/", "rpdr");

			// creating the SOAP payload
			OMElement method = fac
					.createOMElement("GetPreviousQueryList", omNs);
			OMElement value = fac.createOMElement("RequestXmlString", omNs);
			value.setText(XMLstr);
			method.addChild(value);
			envelope.getBody().addChild(method);

			outMsgCtx.setEnvelope(envelope);

			operationClient.addMessageContext(outMsgCtx);
			operationClient.execute(true);

			MessageContext inMsgtCtx = operationClient.getMessageContext("In");
			SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();

			OMElement soapResponse = responseEnv.getBody().getFirstElement();
			System.out.println("Sresponse: " + soapResponse.toString());
			OMElement soapResult = soapResponse.getFirstElement();
			// System.out.println("Sresult: "+ soapResult.toString());

			String i2b2Response = soapResult.getText();
			log.debug(i2b2Response);
			return i2b2Response;
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		} catch (AxisFault e) {
			e.printStackTrace();
			log.error("CellDown");
			return "CellDown";
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		}
	}

	public static String sendPDOQueryRequestREST(String XMLstr) {
		try {
			OMElement payload = getQueryPayLoad(XMLstr);
			Options options = new Options();

			targetEPR = new EndpointReference(getPDOServiceName());
			options.setTo(targetEPR);

			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setProperty(Constants.Configuration.ENABLE_REST,
					Constants.VALUE_TRUE);
			options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(600000));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(
					600000));

			ServiceClient sender = DataExporterQueryServiceClient
					.getServiceClient();
			sender.setOptions(options);

			OMElement responseElement = sender.sendReceive(payload);
			// System.out.println("Client Side response " +
			// responseElement.toString());
			MessageUtil.getInstance().setRequest("URL: " + getPDQServiceName() + "\n" + XMLstr);
	        MessageUtil.getInstance().setResponse("URL: " + getPDQServiceName() + "\n" + responseElement.toString());

			return responseElement.toString();

		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									null,
									"Trouble with connection to the remote server, "
											+ "this is often a network error, please try again",
									"Network Error",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});

			return null;
		} catch (java.lang.OutOfMemoryError e) {
			e.printStackTrace();
			return "memory error";
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String sendPDOQueryRequestSOAP(String requestString) {

		try {
			ServiceClient sender = DataExporterQueryServiceClient
					.getServiceClient();
			OperationClient operationClient = sender
					.createClient(ServiceClient.ANON_OUT_IN_OP);

			// creating message context
			MessageContext outMsgCtx = new MessageContext();
			// assigning message context's option object into instance variable
			Options opts = outMsgCtx.getOptions();
			// setting properties into option

			targetEPR = new EndpointReference(UserInfoBean.getInstance()
					.getCellDataUrl("IM"));

			log.debug(targetEPR);
			opts.setTo(targetEPR);
			opts.setAction("http://rpdr.partners.org/GetPatientDataObject");
			opts.setTimeOutInMilliSeconds(20000);

			log.debug(requestString);

			SOAPEnvelope envelope = null;
			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			envelope = fac.getDefaultEnvelope();
			OMNamespace omNs = fac.createOMNamespace(
					"http://rpdr.partners.org/", "rpdr");

			// creating the SOAP payload
			OMElement method = fac
					.createOMElement("GetPatientDataObject", omNs);
			OMElement value = fac.createOMElement("RequestXmlString", omNs);
			value.setText(requestString);
			method.addChild(value);
			envelope.getBody().addChild(method);

			outMsgCtx.setEnvelope(envelope);

			operationClient.addMessageContext(outMsgCtx);
			operationClient.execute(true);

			MessageContext inMsgtCtx = operationClient.getMessageContext("In");
			SOAPEnvelope responseEnv = inMsgtCtx.getEnvelope();

			OMElement soapResponse = responseEnv.getBody().getFirstElement();
			// System.out.println("Sresponse: "+ soapResponse.toString());
			OMElement soapResult = soapResponse.getFirstElement();
			// System.out.println("Sresult: "+ soapResult.toString());

			String i2b2Response = soapResult.getText();
			log.debug(i2b2Response);
			return i2b2Response;

		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			if (axisFault.getMessage().indexOf("No route to host") >= 0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						JOptionPane
								.showMessageDialog(
										null,
										"Unable to make a connection to the remote server,\n this is often a network error, please try again",
										"Network Error",
										JOptionPane.INFORMATION_MESSAGE);
					}
				});
			} else if (axisFault.getMessage().indexOf("Read timed out") >= 0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						JOptionPane
								.showMessageDialog(
										null,
										"Unable to obtain a response from the remote server, this is often a network error, please try again",
										"Network Error",
										JOptionPane.INFORMATION_MESSAGE);
					}
				});
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getlldString(ArrayList<TimelineRow> tlrows,
			String patientRefId, int minPatient, int maxPatient,
			boolean bDisplayAll, boolean writeFile,
			boolean displayDemographics, DataExporter explorer) {

		try {
			HashSet<String> conceptPaths = new HashSet<String>();
			HashSet<String> providerPaths = new HashSet<String>();
			//HashSet<String> visitPaths = new HashSet<String>();
			ArrayList<PDOItem> items = new ArrayList<PDOItem>();
			
			for(int i=0; i<tlrows.size(); i++) {
				for(int j=0; j<tlrows.get(i).pdoItems.size(); j++) {
					PDOItem pdoItem = tlrows.get(i).pdoItems.get(j);
					String path = pdoItem.fullPath;
					
					if(conceptPaths.contains(path)) {
						continue;
					}
					conceptPaths.add(path);
					//for(int k=0; k<pdoItem.valDisplayProperties.size(); k++) {
					    items.add(pdoItem);					
					//}
				}				
			}
			
			PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
			String pid = null;
			if(patientRefId.equalsIgnoreCase("All")) {
				pid = "-1";
			}
			else {
				pid = patientRefId;
			}
			String xmlStr = pdoFactory.requestXmlMessage(items, new ArrayList<String>(providerPaths), pid,
					new Integer(minPatient), new Integer(maxPatient), false);
			explorer.lastRequestMessage(xmlStr);

			String result = null;// sendPDOQueryRequestREST(xmlStr);
			if (System.getProperty("webServiceMethod").equals("SOAP")) {
				result = PDOQueryClient.sendPDOQueryRequestSOAP(xmlStr);
			} else {
				result = PDOQueryClient.sendPDOQueryRequestREST(xmlStr);
			}

			if (result == null || result.equalsIgnoreCase("memory error")) {
				return result;
			}
			explorer.lastResponseMessage(result);
			return null;
			// return TimelineFactory.generateTimelineData(result, tlrows,
			// writeFile, bDisplayAll, displayDemographics);
		} catch (org.apache.axis2.AxisFault e) {
			e.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									null,
									"Trouble with connection to the remote server, "
											+ "this is often a network error, please try again",
									"Network Error",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getPDOResponseString(ArrayList<TimelineRow> tlrows,
			ArrayList<String> ids, DataExporter explorer) {

		try {
			HashSet<String> conceptPaths = new HashSet<String>();
			// HashSet<String> providerPaths = new HashSet<String>();
			// HashSet<String> visitPaths = new HashSet<String>();
			ArrayList<PDOItem> items = new ArrayList<PDOItem>();

			for (int i = 0; i < tlrows.size(); i++) {
				for (int j = 0; j < tlrows.get(i).pdoItems.size(); j++) {
					PDOItem item = tlrows.get(i).pdoItems.get(j);
					String path = item.fullPath;

					if (conceptPaths.contains(path)) {
						continue;
					}
					conceptPaths.add(path);
					items.add(item);
				}
			}

			PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
			String xmlStr = pdoFactory.requestXmlMessage(items, ids, false);
			explorer.lastRequestMessage(xmlStr);

			String result = null;// sendPDOQueryRequestREST(xmlStr);
			if (System.getProperty("webServiceMethod").equals("SOAP")) {
				result = PDOQueryClient.sendPDOQueryRequestSOAP(xmlStr);
			} else {
				result = PDOQueryClient.sendPDOQueryRequestREST(xmlStr);
			}

			if (result == null || result.equalsIgnoreCase("memory error")) {
				return result;
			}
			explorer.lastResponseMessage(result);

			return result;
			// return TimelineFactory.generateTimelineData(result, tlrows,
			// writeFile, bDisplayAll, displayDemographics);
		} catch (org.apache.axis2.AxisFault e) {
			e.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									null,
									"Trouble with connection to the remote server, "
											+ "this is often a network error, please try again",
									"Network Error",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {

	}
}
