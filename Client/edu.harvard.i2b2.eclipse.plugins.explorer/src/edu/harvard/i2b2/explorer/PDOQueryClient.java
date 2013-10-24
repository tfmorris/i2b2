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
package edu.harvard.i2b2.explorer;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.RGB;

import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.ObservationFactType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.PatientDimensionType;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.smlib.Lib;

public class PDOQueryClient {
	private static final Log log = LogFactory.getLog(PDOQueryClient.class);
	
	private static EndpointReference targetEPR; 
	private static String servicename = null;
	
	private static String getPDOServiceName(){
		return UserInfoBean.getInstance().getCellDataUrl("pdo") + "pdorequest";
	}
	
	private static String getPDQServiceName(){
		return UserInfoBean.getInstance().getCellDataUrl("pdo") + "request";
	}
	
	public static OMElement getQueryPayLoad(String str) throws Exception {
		StringReader strReader = new StringReader(str);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		//System.out.println("Line item string " + lineItem.toString());
		return lineItem;
	}
	
	public static String sendPDQQueryRequestREST(String XMLstr) {
		try {
			OMElement payload = getQueryPayLoad(XMLstr);
			Options options = new Options();

			targetEPR = new EndpointReference(getPDQServiceName());
			options.setTo(targetEPR);
				
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);					
			options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
			options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(600000));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(600000));
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			
			OMElement responseElement = sender.sendReceive(payload);
			//System.out.println("Client Side response " + responseElement.toString());
			
			return responseElement.toString();
			
		} 
		catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
		         	JOptionPane.showMessageDialog(null, "Trouble with connection to the remote server, " +
		         			"this is often a network error, please try again", 
		         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});				
			
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String sendPDQQueryRequestSOAP(String XMLstr) {
		try {
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();

			basicAuthentication.setUsername(UserInfoBean.getInstance().getUserName());
			basicAuthentication.setPassword(UserInfoBean.getInstance().getUserPassword());
			
			OMElement payload = getQueryPayLoad(XMLstr);
			Options options = new Options();

			// options.setProperty(HTTPConstants.PROXY, proxyProperties);
			targetEPR = new EndpointReference(getPDQServiceName());
			options.setTo(targetEPR);
				
			options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
					basicAuthentication);	
            
			options.setTimeOutInMilliSeconds(900000);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

			ConfigurationContext configContext =

			ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(null, null);
				
			// Blocking invocation
			ServiceClient sender = new ServiceClient(configContext, null);
			sender.setOptions(options);

			OMElement responseElement = sender.sendReceive(payload);
			//System.out.println("Client Side response " + responseElement.toString());
			
			return responseElement.toString();
			
		} 
		catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			if(axisFault.getMessage().indexOf("No route to host")>=0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		            	JOptionPane.showMessageDialog(null, "Unable to make a connection to the remote server,\n this is often a network error, please try again"
								, "Network Error", JOptionPane.INFORMATION_MESSAGE);
		            }
				});				
			}
			else if(axisFault.getMessage().indexOf("Read timed out")>=0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		            	JOptionPane.showMessageDialog(null, "Unable to obtain a response from the remote server, this is often a network error, please try again"
								, "Network Error", JOptionPane.INFORMATION_MESSAGE);
		            }
				});				
			}
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
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
			options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
			options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(600000));
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(600000));
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);

			OMElement responseElement = sender.sendReceive(payload);
			//System.out.println("Client Side response " + responseElement.toString());
			
			return responseElement.toString();
			
		} 
		catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
		         	JOptionPane.showMessageDialog(null, "Trouble with connection to the remote server, " +
		         			"this is often a network error, please try again", 
		         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});				
			
			return null;
		} 
		catch (java.lang.OutOfMemoryError e){
			e.printStackTrace();
			return "memory error";
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String sendPDOQueryRequestSOAP(String XMLstr) {
		try {
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();

			basicAuthentication.setUsername(UserInfoBean.getInstance().getUserName());
			basicAuthentication.setPassword(UserInfoBean.getInstance().getUserPassword());
			
			OMElement payload = getQueryPayLoad(XMLstr);
			Options options = new Options();

			// options.setProperty(HTTPConstants.PROXY, proxyProperties);
			targetEPR = new EndpointReference(getPDOServiceName());
			options.setTo(targetEPR);
				
			options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
					basicAuthentication);	
            
			options.setTimeOutInMilliSeconds(900000);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

			ConfigurationContext configContext =

			ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(null, null);
				
			// Blocking invocation
			ServiceClient sender = new ServiceClient(configContext, null);
			sender.setOptions(options);

			OMElement responseElement = sender.sendReceive(payload);
			//System.out.println("Client Side response " + responseElement.toString());
			
			return responseElement.toString();
			
		} 
		catch (AxisFault axisFault) {
			axisFault.printStackTrace();
			if(axisFault.getMessage().indexOf("No route to host")>=0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		            	JOptionPane.showMessageDialog(null, "Unable to make a connection to the remote server,\n this is often a network error, please try again"
								, "Network Error", JOptionPane.INFORMATION_MESSAGE);
		            }
				});				
			}
			else if(axisFault.getMessage().indexOf("Read timed out")>=0) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		            	JOptionPane.showMessageDialog(null, "Unable to obtain a response from the remote server, this is often a network error, please try again"
								, "Network Error", JOptionPane.INFORMATION_MESSAGE);
		            }
				});				
			}
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getlldString(ArrayList<TimelineRow> tlrows, String patientRefId, 
			int minPatient, int maxPatient, boolean bDisplayAll, boolean writeFile, 
			boolean displayDemographics, ExplorerC explorer) {
		
		try {
			HashSet<String> conceptPaths = new HashSet<String>();
			HashSet<String> providerPaths = new HashSet<String>();
			HashSet<String> visitPaths = new HashSet<String>();
			
			for(int i=0; i<tlrows.size(); i++) {
				for(int j=0; j<tlrows.get(i).pdoSets.size(); j++) {
					PDOSet pset = tlrows.get(i).pdoSets.get(j);
					String path = pset.fullPath;
					if(pset.tableType.equalsIgnoreCase("provider_dimension")) {
						if(providerPaths.contains(path)) {
							continue;
						}
						providerPaths.add(path);
					}
					else if(pset.tableType.equalsIgnoreCase("Encounters")) {
						if(visitPaths.contains(path)) {
							continue;
						}
						visitPaths.add(path);
					}
					else {
						if(conceptPaths.contains(path)) {
							continue;
						}
						conceptPaths.add(path);
					}
				}				
			}
			
			if(conceptPaths.size()==0 && providerPaths.size()==0 && visitPaths.size()>0) {
				JOptionPane.showMessageDialog(null, "Visit only query is not supproted in this release.");
				return "visitError";
			}
			
			PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
			Integer pid = null;
			if(patientRefId.equalsIgnoreCase("All")) {
				pid = new Integer(-1);
			}
			else {
				pid = Integer.parseInt(patientRefId);
			}
			String xmlStr = pdoFactory.requestXmlMessage(new ArrayList<String>(conceptPaths), new ArrayList<String>(providerPaths), 
					pid, new Integer(minPatient), new Integer(maxPatient), false);
			explorer.lastRequestMessage(xmlStr);
			
			String result = sendPDOQueryRequestREST(xmlStr);
			if(result==null || result.equalsIgnoreCase("memory error")) {
				return result;
			}
			explorer.lastResponseMessage(result);
			
			return TimelineFactory.generateTimelineData(result, tlrows, writeFile, bDisplayAll, displayDemographics);
		}
		/*catch(org.apache.axis2.AxisFault e) {
			e.printStackTrace();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
		         	JOptionPane.showMessageDialog(null, "Trouble with connection to the remote server, " +
		         			"this is often a network error, please try again", 
		         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});				
			
			return null;
		}*/
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void testWritelld(String result, ArrayList<TimelineRow> rows, boolean writeFile) {
		
		TimelineFactory.generateTimelineData(result, rows, writeFile, true, true);		
	}
	
	private static void testWriteTableFile(String result) {
		StringBuilder resultFile = new StringBuilder();
		
		try {
			PDOResponseMessageFactory pdoresponsefactory = new PDOResponseMessageFactory();
			PatientDataType.PatientDimensionSet patientDimensionSet = pdoresponsefactory.getPatientSetFromResponseXML(result);
			if (patientDimensionSet != null) { 
				System.out.println("Total patient: "+patientDimensionSet.getPatientDimension().size());
				for(int i=0; i<patientDimensionSet.getPatientDimension().size();i++) {
					PatientDimensionType patientType = patientDimensionSet.getPatientDimension().get(i);
					System.out.println("PatientNum: " + patientType.getPatientNum()
							+ ","+patientType.getRaceCd()
							+","+patientType.getSexCd()
							+","+patientType.getAgeInYearsNum()
							+","+patientType.getVitalStatusCd());
					resultFile.append(patientType.getPatientNum()
							+","+patientType.getRaceCd()
							+","+patientType.getSexCd()
							+","+patientType.getAgeInYearsNum()
							+","+patientType.getVitalStatusCd()+"\n");
				}
			}
			
			/*List<PatientDataType.ObservationFactSet> factSets = pdoresponsefactory.getFactSetsFromResponseXML(result);
			
			for(int j=0; j<factSets.size(); j++) {
				PatientDataType.ObservationFactSet observationFactSet = factSets.get(j);
				//pdoresponsefactory.getFactSetFromResponseXML(result);
				if (observationFactSet != null) { 
					System.out.println("Total fact: "+observationFactSet.getObservationFact().size()
							+" for "+observationFactSet.getPath()+"-"+observationFactSet.getConceptName());
					for(int i=0; i<observationFactSet.getObservationFact().size(); i++) {
						ObservationFactType obsFactType = observationFactSet.getObservationFact().get(i);
						System.out.println("PatientNum: "+obsFactType.getPatientNum()
								+" concept_cd: " + obsFactType.getConceptCd()
								+" start_date: " + obsFactType.getStartDate().getYear()
								+"_"+obsFactType.getStartDate().getMonth()
								+"_"+obsFactType.getStartDate().getDay()
								+"_"+obsFactType.getNvalNum()
								+"_"+obsFactType.getConceptCd()
								+"_"+obsFactType.getPatientNum());
					}
				}
			}*/
			
			String tableFile = "C:\\tableview\\data\\patienttable.txt";
			File oDelete = new File(tableFile);
			if (oDelete != null) oDelete.delete();
			RandomAccessFile f = new RandomAccessFile(tableFile,"rw");
			TimelineFactory.append(f, "PatientNumber,Race,Sex,Age,Dead\n");
			TimelineFactory.append(f, resultFile.toString());
			f.close();
			
			/*System.out.println("\nTesting lld:");
			for(int i=0; i<patientDimensionSet.getPatientDimension().size();i++) {
				PatientDimensionType patientType = patientDimensionSet.getPatientDimension().get(i);
				Integer pnum = patientType.getPatientNum();
				System.out.println("PatientNum: " + patientType.getPatientNum());
				
				for(int j=0; j<factSets.size(); j++) {
					PatientDataType.ObservationFactSet observationFactSet = factSets.get(j);
					String path = observationFactSet.getPath();
					StringBuilder resultString = new StringBuilder();
					int total = 0;
					XMLGregorianCalendar curStartDate = null;
					for(int k=0; k<observationFactSet.getObservationFact().size(); k++) {
						ObservationFactType obsFactType = observationFactSet.getObservationFact().get(k);
						
						if(pnum.intValue() == obsFactType.getPatientNum().intValue()) {
							if((curStartDate != null) && 
									(obsFactType.getStartDate().compare(curStartDate) == DatatypeConstants.EQUAL)) {
								continue;
							}
							
							resultString.append("PatientNum: "+obsFactType.getPatientNum()
									//+" for "+path
									+" concept_cd: " + obsFactType.getConceptCd()
									+" start_date: " + obsFactType.getStartDate().getYear()
									+"_"+obsFactType.getStartDate().getMonth()
									+"_"+obsFactType.getStartDate().getDay()
									+"_"+obsFactType.getStartDate().getHour()
									+":"+obsFactType.getStartDate().getMinute()
									+"_"+obsFactType.getNvalNum()
									+"_"+obsFactType.getConceptCd()
									+"_"+obsFactType.getPatientNum()
									+"_"+obsFactType.getEndDate()+"\n");
							total++;
							curStartDate = obsFactType.getStartDate();
						}
					}
										
					System.out.println("-- "+path+" has "+total+" events");
					System.out.println(resultString.toString());
				}
			}*/
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void main(String[] args) throws Exception {
		PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
		String conceptPath = new String("\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\CA");
		
		ArrayList<String> paths = new ArrayList<String>();
		//paths.add(conceptPath);
		
		conceptPath = new String("\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\GGT");
		paths.add(conceptPath);
		
		//conceptPath = new String("\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\GGT");
		//paths.add(conceptPath);
		ArrayList<String> ppaths = new ArrayList<String>();
		conceptPath = new String("\\Providers\\BWH");
		//ppaths.add(conceptPath);
		
		String xmlStr = pdoFactory.requestXmlMessage(paths, ppaths, new Integer(1545), new Integer(1), new Integer(20), false);
		String result = sendPDOQueryRequestREST(xmlStr);
		
		//FileWriter fwr = new FileWriter("c:\\testdir\\response.txt");
		//fwr.write(result);
		System.out.println(result);
		
		PDOSet set = new PDOSet();
		set.fullPath = "\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\CA";
		set.hasValueDisplayProperty = true;
		ValueDisplayProperty valdp = new ValueDisplayProperty();
		valdp.left = 0.0;
		valdp.right = 8.4;
		valdp.color = "red";
		valdp.height = "Very Low";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 8.4;
		valdp.right = 8.9;
		valdp.color = "gold";
		valdp.height = "Low";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 8.9;
		valdp.right = 10.0;
		valdp.color = "green";
		valdp.height = "Medium";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 10.0;
		valdp.right = 10.6;
		valdp.color = "gold";
		valdp.height = "Tall";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 10.6;
		valdp.right = Integer.MAX_VALUE;
		valdp.color = "red";
		valdp.height = "Very Tall";
		set.valDisplayProperties.add(valdp);
		set.tableType = "fact";
		
		TimelineRow row = new TimelineRow();
		row.pdoSets.add(set);
		row.displayName = "Calcium (Group:CA)";
		
		ArrayList<TimelineRow> rows = new ArrayList<TimelineRow>();
		rows.add(row);
		
		set = new PDOSet();
		set.fullPath = "\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\GGT";
		set.hasValueDisplayProperty = true;
		valdp = new ValueDisplayProperty();
		valdp.left = 0.0;
		valdp.right = 1.0;
		valdp.color = "red";
		valdp.height = "Very Low";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 1.0;
		valdp.right = 19.0;
		valdp.color = "gold";
		valdp.height = "Low";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 19.0;
		valdp.right = 34.0;
		valdp.color = "green";
		valdp.height = "Medium";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 34.0;
		valdp.right = 82.0;
		valdp.color = "gold";
		valdp.height = "Tall";
		set.valDisplayProperties.add(valdp);
		
		valdp = new ValueDisplayProperty();
		valdp.left = 82.0;
		valdp.right = Integer.MAX_VALUE;
		valdp.color = "red";
		valdp.height = "Very Tall";
		set.valDisplayProperties.add(valdp);
		set.tableType = "fact";
		
		row = new TimelineRow();
		row.pdoSets.add(set);
		row.displayName = "Gamma Glutamyltrans (Group:GGT)";
		
		rows.add(row);
		
		//testWritelld(result, rows, true);
		testWriteTableFile(result);
	}
}
