/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

package edu.harvard.i2b2.previousquery;

/***
 * Class: QueryTopPanelModel 
 * 
 * A data model class for the top panel of the Query Tool mode in CRC Navigator.
 * 
 * @author wp066
 *
 */

import java.io.StringWriter;
import java.util.*;

import javax.swing.JLabel;
import javax.xml.bind.JAXBElement;
//import javax.xml.bind.JAXBElement;
//import javax.xml.datatype.DatatypeConfigurationException;
//import javax.xml.datatype.DatatypeConstants;
//import javax.xml.datatype.DatatypeFactory;
//import javax.xml.datatype.XMLGregorianCalendar;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.previousquery.datavo.PreviousQueryJAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.*;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.*;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ItemType.ConstrainByDate;

public class QueryTopPanelModel {
	private ArrayList<QueryConceptTreePanel> panelList;
	private ArrayList<JLabel> andOrLabelList;
	
	private int panelCount = 0;
	
	private int specificity = 0;
	public void specificity(int i) {specificity=i;}
	public int specificity() {return specificity;}
	
	private int lastLabelPosition;
	public void lastLabelPosition(int i) {lastLabelPosition=i;}
	public int lastLabelPosition() {return lastLabelPosition;}
	
	private String queryName = null;
	public String queryName() {return queryName;}
	public void queryName(String str) {queryName = str;}
	
	private QueryTopPanel topPanel = null;
	public void setTopPanel(QueryTopPanel p) {topPanel = p;} 
	
	public QueryTopPanelModel() {
		panelList = new ArrayList<QueryConceptTreePanel>();
		andOrLabelList = new ArrayList<JLabel>();
	}
	
	public QueryConceptTreePanel getTreePanel(int index) {
		return panelList.get(index);
	}
	
	public JLabel getAndOrLabel(int index) {
		return andOrLabelList.get(index);
	}
	
	public int getCurrentPanelCount() {
		return panelCount;
	}
	
	public void addPanel(QueryConceptTreePanel panel, JLabel label, int position) {
		panelList.add(panel);
		if(label != null) {
			andOrLabelList.add(label);
		}
		lastLabelPosition = position;
		
		panelCount++;
	}
	
	private String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for(int i=0; i<20; i++) {
			int num = getValidAcsiiValue();
			//System.out.println("Generated number: " + num + " char: "+(char)num);
			strWriter.append((char)num);
		}
		return strWriter.toString();
	}
	
	private int getValidAcsiiValue() {
		int number = 48;
		while(true) {
			number = 48+(int) Math.round(Math.random() * 74);
			if((number > 47 && number < 58) || (number > 64 && number < 91) 
				|| (number > 96 && number < 123)) {
					break;
			}
		}
		return number;	
	}
	
	private MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		messageHeader.setAcceptAcknowledgementType(new String("messageId"));
		
		MessageControlIdType mcIdType = new MessageControlIdType();
		//mcIdType.setInstanceNum(1);
		mcIdType.setMessageNum(generateMessageId());
		//mcIdType.setSessionId("1");
		messageHeader.setMessageControlId(mcIdType);
		
		MessageTypeType messageTypeType = new MessageTypeType();
		messageTypeType.setEventType("EQQ");
		messageTypeType.setMessageCode("Q04");
		messageHeader.setMessageType(messageTypeType);
		
		ApplicationType sendAppType = new ApplicationType();
		sendAppType.setApplicationName("i2b2_QueryTool");
		sendAppType.setApplicationVersion("0.2"); 
		messageHeader.setSendingApplication(sendAppType);
		
		ApplicationType receiveAppType = new ApplicationType();
		receiveAppType.setApplicationName("i2b2_DataRepositoryCell");
		receiveAppType.setApplicationVersion("0.2"); 
		messageHeader.setReceivingApplication(receiveAppType);
		
		FacilityType facType = new FacilityType();
		facType.setFacilityName("PHS");
		messageHeader.setSendingFacility(facType);
		messageHeader.setReceivingFacility(facType);
		
		SecurityType secType = new SecurityType();
		secType.setDomain(System.getProperty("projectName"));
		secType.setUsername(System.getProperty("user"));
		secType.setPassword(System.getProperty("pass"));
		messageHeader.setSecurity(secType);
		
		ProcessingIdType procIdType = new ProcessingIdType();
		procIdType.setProcessingId("P");
		procIdType.setProcessingMode("I");
		messageHeader.setProcessingId(procIdType);
		
		return messageHeader;
	}
	
	public String writeContentQueryXML(String queryName, String userId) {
		DTOFactory dtoFactory = new DTOFactory();
		
		//QueryType queryType = new QueryType();
		
		// create header
		PsmQryHeaderType headerType = new PsmQryHeaderType();
		UserType userType = new UserType();
		userType.setLogin(userId);
		userType.setValue(userId);
		headerType.setUser(userType);
		headerType.setRequestType(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION);
		
	//	QuerySetType querySetType = new QuerySetType();
		//querySetType.getQuery().add(queryType);
		
		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		BodyType bodyType = new BodyType();
		edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory(); 
		bodyType.getAny().add(psmOf.createPsmheader(headerType));

		MessageHeaderType messageHeader = getMessageHeader();
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setMessageHeader(messageHeader);
		requestMessageType.setRequestHeader(requestHeader);
			//		dtoFactory.getRequestMessageType(messageHeader, requestHeader, bodyType);
		
		JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory(); 
			jaxbUtil.marshaller(of.createRequest(requestMessageType), strWriter);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Generated content XML request: " + strWriter.toString());
		return strWriter.toString();
	}
	
	public String wirteQueryXML() {		
		DTOFactory dtoFactory = new DTOFactory();
		//TimeZone pdt = TimeZone.getTimeZone("EST");
		//DatatypeFactory dataTypeFactory = null;
		//try {
		//	dataTypeFactory = DatatypeFactory.newInstance();
			//XMLGregorianCalendar xmlCalendar = dataTypeFactory.newXMLGregorianCalendarDate(2006, 9, 21, -5*60);
			//TimeZone pdt = xmlCalendar.getgetTimezone();
		//}
		//catch (DatatypeConfigurationException e) {
		//	e.printStackTrace();
		//}
		
		//RequestType requestType = new RequestType();
		//requestType.setSpecificityScale(specificity());
		QueryDefinitionType queryDefinitionType = new QueryDefinitionType();
		QueryDefinitionRequestType queryDefinitionRequestType = new QueryDefinitionRequestType();
		for(int i=0; i<panelCount; i++) {
			QueryConceptTreePanel panel = getTreePanel(i);
			ArrayList<QueryConceptTreeNodeData> nodelist = panel.data().getItems();
			if((nodelist != null) && (nodelist.size() > 0)) {
				System.out.println("Panel: "+panel.getGroupName()+" Excluded: "+((panel.data().exclude())?"yes":"no"));
				PanelType panelType = new PanelType();
				panelType.setInvert((panel.data().exclude())? 1:0);
				panelType.setTotalItemOccurrences(panel.getOccurrenceTimes());
				panelType.setPanelNumber(i+1);
				
				if(panel.data().startTime() != -1) {
					panelType.setPanelDateFrom(//dataTypeFactory.newXMLGregorianCalendarDate(
						dtoFactory.getXMLGregorianCalendarDate(panel.data().startYear(), panel.data().startMonth(), panel.data().startDay()));
				}
				
				if(panel.data().endTime() != -1) {
					panelType.setPanelDateTo(//dataTypeFactory.newXMLGregorianCalendarDate(
					    dtoFactory.getXMLGregorianCalendarDate(panel.data().endYear(), panel.data().endMonth(), panel.data().endDay()));
				}
				
				for(int j=0; j<nodelist.size(); j++) {
					QueryConceptTreeNodeData node = nodelist.get(j);
					System.out.println("\tItem: "+node.fullname());
					
					// create item
					ItemType itemType = new ItemType();
					// itemType.setConstrainByDate(.setDateFrom(dtoFactory.getXMLGregorianCalendarDate(2006,
					// 10, 4)));
					itemType.setItemKey(node.fullname());
					itemType.setItemName(node.name());
					itemType.setItemTable(node.lookuptable());
					itemType.setTooltip(node.tooltip());
					itemType.setHlevel(Integer.parseInt(node.hlevel()));
					itemType.setClazz("ENC");
					ConstrainByDate cDate = new ConstrainByDate();
					if(node.startTime() != -1) {
						cDate.setDateFrom(dtoFactory.getXMLGregorianCalendarDate(node.startYear(), 
								node.startMonth(), node.startDay()));				
					}
					if(node.endTime() != -1) {
						cDate.setDateTo(dtoFactory.getXMLGregorianCalendarDate(node.endYear(), 
								node.endMonth(), node.endDay()));				
					}
					itemType.setConstrainByDate(cDate);
					panelType.getItem().add(itemType);
				}
				queryDefinitionType.getPanel().add(panelType);
			}
		}
		
		// create infotype
		InfoType infoType = new InfoType();
		infoType.setValue("INFO");
		infoType.setUrl("http://www.ibm.com");

		// create header
		PsmQryHeaderType headerType = new PsmQryHeaderType();
		UserType userType = new UserType();
		userType.setLogin(System.getProperty("user"));
		userType.setValue(System.getProperty("user"));
		headerType.setUser(userType);
		headerType.setRequestType(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION);
		if(queryName == null) {
			queryName = getTreePanel(0).data().getItems().get(0).name()+"_"+generateMessageId().substring(0, 4);
		}
		
		queryDefinitionType.setQueryName(queryName);
		queryDefinitionRequestType.setQueryDefinition(queryDefinitionType);
		
		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		
		BodyType bodyType = new BodyType();
		edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory(); 
		bodyType.getAny().add(psmOf.createPsmheader(headerType));
		bodyType.getAny().add(psmOf.createRequest(queryDefinitionRequestType));
			//new QueryToolDTOFactory().buildBodyType(headerType, queryDefinitionRequestType, null);
		MessageHeaderType messageHeader = getMessageHeader();
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setMessageHeader(messageHeader);
		requestMessageType.setRequestHeader(requestHeader);
			//dtoFactory.getRequestMessageType(messageHeader, requestHeader, bodyType);
		
		JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory(); 
			jaxbUtil.marshaller(of.createRequest(requestMessageType), strWriter);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return strWriter.toString();
	}
	
	public String getTmpQueryName() {
		return getTreePanel(0).data().getItems().get(0).name()+"_"+generateMessageId().substring(0, 4);
	}
	
	public int getQueryResultCount(String fileURL) {
		return 0;
	}
	
	public boolean hasEmptyPanels() {
		for(int i=0; i<panelList.size(); i++) {
			QueryConceptTreePanel panel = panelList.get(i);
			if((panel.getItems() == null) || (panel.getItems().size() == 0)) {
				return true;
			}
		}
		return false;
	}
	
	public void clearConceptTrees() {
		for(int i=0; i<panelList.size(); i++) {
			QueryConceptTreePanel panel = panelList.get(i);
			panel.reset();
		}
	}
	
	public void removeAdditionalPanels() {
		if(getCurrentPanelCount() > 3) {
			for(int i=3; i<getCurrentPanelCount(); i++) {
				QueryConceptTreePanel panel = panelList.get(3);
				JLabel label = andOrLabelList.get(2);
				panel.setVisible(false);
				label.setVisible(false);
				panelList.remove(3);
				andOrLabelList.remove(2);
			}
			panelCount = 3;
		}
	}
	
	public boolean isAllPanelEmpty() {
		for(int i=0; i<panelList.size(); i++) {
			QueryConceptTreePanel panel = panelList.get(i);
			if((panel.getItems() != null) && (panel.getItems().size() > 0)) {
				return false;
			}
		}
		return true;
	}
	
	public void redrawPanelFromXml(String xmlstr) {
		try {
			JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlstr);
			ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
			BodyType bt = messageType.getMessageBody();
			MasterResponseType masterResponseType = 
				(MasterResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(),MasterResponseType.class);
			RequestXmlType requestXmlType = masterResponseType.getQueryMaster().get(0).getRequestXml();
			String strRequest = (String) requestXmlType.getContent().get(0);
			
			jaxbElement = jaxbUtil.unMashallFromString(strRequest);
			RequestMessageType requestMessageType = (RequestMessageType)jaxbElement.getValue();
			bt = requestMessageType.getMessageBody();
			QueryDefinitionRequestType queryDefinitionRequestType = 
				(QueryDefinitionRequestType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(),QueryDefinitionRequestType.class);
			QueryDefinitionType queryDefinitionType = queryDefinitionRequestType.getQueryDefinition();
			
			int numOfPanels = queryDefinitionType.getPanel().size();
			if(numOfPanels > 3) {
				for(int i=0; i<(numOfPanels-3); i++) {
					topPanel.addPanel();
				}
			}
					
			for(int i=0; i<numOfPanels; i++) {
				PanelType panelType = queryDefinitionType.getPanel().get(i);
				final QueryConceptTreePanel panel = getTreePanel(i);
				QueryConceptTreeData panelData = new QueryConceptTreeData();
				panelData.setOccurrenceTimes(panelType.getTotalItemOccurrences());
				if(panelType.getInvert() == 0) {
					panelData.exclude(false);
				}
				else if(panelType.getInvert() == 1) {
					panelData.exclude(true);
				}
					
				for(int j=0; j<panelType.getItem().size(); j++) {
					ItemType itemType = panelType.getItem().get(j);
					QueryConceptTreeNodeData nodedata = new QueryConceptTreeNodeData();
							
					nodedata.name(itemType.getItemName());
					nodedata.visualAttribute("FA");
				    nodedata.tooltip(itemType.getTooltip());
				    nodedata.fullname(itemType.getItemKey());
				    nodedata.lookuptable(itemType.getItemTable());
				    nodedata.hlevel(new Integer(itemType.getHlevel()).toString());
				    nodedata.lookupdb("metadata");
				    nodedata.selectservice(System.getProperty("selectservice"));
				    //get the xml content from select service then set it as node data
				    nodedata.setXmlContent();
					    
				    panelData.getItems().add(nodedata);
				}
					
				final QueryConceptTreeData fpanelData = panelData;
				final String name = queryDefinitionType.getQueryName();
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						topPanel.setQueryName(name);
						panel.redraw(fpanelData);
					}
				});					
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
