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
package edu.harvard.i2b2.explorer.dataModel;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import java.util.Iterator;
//import java.util.List;

//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreePath;

//import org.jdom.input.SAXBuilder;
//import org.jdom.output.Format;
//import org.jdom.output.XMLOutputter;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crcxmljaxb.datavo.dnd.DndType;
//import edu.harvard.i2b2.eclipse.plugins.ontology.ws.GetChildrenResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.explorer.ontologyMessaging.GetTermInfoResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.explorer.ontologyMessaging.OntServiceDriver;
import edu.harvard.i2b2.explorer.datavo.ExplorerJAXBUtil;
import edu.harvard.i2b2.explorer.ui.ExplorerC;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.StatusType;
//import edu.harvard.i2b2.crcxmljaxb.datavo.vdo.ConceptType;
import edu.harvard.i2b2.crcxmljaxb.datavo.vdo.ConceptsType;
//import edu.harvard.i2b2.crcxmljaxb.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.crcxmljaxb.datavo.vdo.GetTermInfoType;

public class QueryConceptData {
    private static final Log log = LogFactory.getLog(QueryConceptData.class);

	private boolean inverted = false;
	public void inverted(boolean b) {inverted = b;}
	public boolean inverted() {return inverted;}
	
	private String hlevel;
	public void hlevel(String str) {hlevel = new String(str);}
	public String hlevel() {return hlevel;}
	
	private String fullname;
	public void fullname(String str) {fullname = new String(str);}
	public String fullname() {return fullname;}
	
	private String name = "";
	public void name(String str) {name = new String(str);}
	public String name() {return name;}
	
	private String visualAttribute;
	public void visualAttribute(String str) {visualAttribute = new String(str);}
	public String visualAttribute() {return visualAttribute;}
	
	private String factTableColumn;
	public void factTableColumn(String str) {factTableColumn = new String(str);}
	public String factTableColumn() {return factTableColumn;}
	
	private String tableName;
	public void tableName(String str) {tableName = new String(str);}
	public String tableName() {return tableName;}
	
	private String columnName;
	public void columnName(String str) {columnName = new String(str);}
	public String columnName() {return columnName;}
	
	private String columnDataType;
	public void columnDataType(String str) {columnDataType = new String(str);}
	public String columnDataType() {return columnDataType;}
	
	private String operator;
	public void operator(String str) {operator = new String(str);}
	public String operator() {return operator;}
	
	private String dimcode;
	public void dimcode(String str) {dimcode = new String(str);}
	public String dimcode() {return dimcode;}
	
	private String tooltip;
	public void tooltip(String str) {if(str!=null) {tooltip = new String(str);}}
	public String tooltip() {return tooltip;}
	
	private String lookupdb;
	public void lookupdb(String str) {lookupdb = new String(str);}
	public String lookupdb() {return lookupdb;}
	
	private String lookuptable;
	public void lookuptable(String str) {lookuptable = new String(str);}
	public String lookuptable() {return lookuptable;}
	
	private String selectservice;
	public void selectservice(String str) {selectservice = new String(str);}
	public String selectservice() {return selectservice;}
	
	private int startYear = -1;
	public void startYear(int i) {startYear = i;}
	public int startYear() {return startYear;}
		
	private int startMonth = -1;
	public void startMonth(int i) {startMonth = i;}
	public int startMonth() {return startMonth;}
		
	private int startDay = -1;
	public void startDay(int i) {startDay = i;}
	public int startDay() {return startDay;}
	
	private long startTime = -1;
	public void startTime(long l) {startTime = l;}
	public long startTime() {return startTime;}
			
	private int endYear = -1;
	public void endYear(int i) {endYear = i;}
	public int endYear() {return endYear;}
		
	private int endMonth = -1;
	public void endMonth(int i) {endMonth = i;}
	public int endMonth() {return endMonth;}
		
	private int endDay = -1;
	public void endDay(int i) {endDay = i;}
	public int endDay() {return endDay;}
	
	private long endTime = -1;
	public void endTime(long l) {endTime = l;}
	public long endTime() {return endTime;}
	
	private boolean includePrincipleVisit = true;
	public void includePrincipleVisit(boolean b) {includePrincipleVisit = b;}
	public boolean includePrincipleVisit() {return includePrincipleVisit;}
	
	private boolean includeSecondaryVisit = true;
	public void includeSecondaryVisit(boolean b) {includeSecondaryVisit = b;}
	public boolean includeSecondaryVisit() {return includeSecondaryVisit;}
	
	private boolean includeAdmissionVisit = true;
	public void includeAdmissionVisit(boolean b) {includeAdmissionVisit = b;}
	public boolean includeAdmissionVisit() {return includeAdmissionVisit;}
	
	private String xmlContent="";
	public void xmlContent(String str) {xmlContent = str;}
	public String xmlContent() {return xmlContent;}
	
	private boolean hasValue = false;

	public void hasValue(boolean b) {
		hasValue = b;
	}

	public boolean hasValue() {
		return hasValue;
	}

	private ValuePropertyData valuePropertyData;

	public ValuePropertyData valuePropertyData() {
		return valuePropertyData;
	}
	
	private String valueName = "";

	public void valueName(String str) {
		valueName = new String(str);
	}

	public String valueName() {
		return valueName;
	}

	public QueryConceptData() {
		valuePropertyData = new ValuePropertyData();
	}
	
	public String setXmlContent() {
		if(!xmlContent.equals("")) {
			return "";
		}
		//calling getTermInfo to get the xml content
		try {
			GetTermInfoType termInfoType = new GetTermInfoType();

			termInfoType.setMax(null);//Integer.parseInt(System.getProperty("OntMax")));
			termInfoType.setHiddens(Boolean.parseBoolean("true"));
			termInfoType.setSynonyms(Boolean.parseBoolean("false"));

    //		log.info("sent : " + parentType.getMax() + System.getProperty("OntMax") + System.getProperty("OntHiddens")
    //				+ System.getProperty("OntSynonyms") );

	//		parentType.setMax(150);
			termInfoType.setBlob(true);
//			parentType.setType("all");
		
			termInfoType.setSelf(fullname());

	//		Long time = System.currentTimeMillis();
	//		log.info("making web service call " + time);
			GetTermInfoResponseMessage msg = new GetTermInfoResponseMessage();
			StatusType procStatus = null;	
			//while(procStatus == null || !procStatus.getType().equals("DONE")){
			String response = OntServiceDriver.getTermInfo(termInfoType, "");
			log.debug("Ontology service getTermInfo response: "+response);
			
			procStatus = msg.processResult(response);
			// log.info(procStatus.getType());
			// log.info(procStatus.getValue());
			if(!procStatus.getType().equals("DONE")) {
				return "error";
			}
			ConceptsType allConcepts = msg.doReadConcepts();   	    
			//List<ConceptType> concepts = allConcepts.getConcept();
			StringWriter strWriter = new StringWriter();
			try {
				//strWriter = new StringWriter();
				DndType dnd = new DndType();
				edu.harvard.i2b2.crcxmljaxb.datavo.vdo.ObjectFactory vdoOf = new edu.harvard.i2b2.crcxmljaxb.datavo.vdo.ObjectFactory();
				dnd.getAny().add( vdoOf.createConcepts(allConcepts));

				edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory();
				ExplorerJAXBUtil.getJAXBUtil().marshaller(of.createPluginDragDrop(dnd), strWriter);
				
				
			} catch (JAXBUtilException e) {
				//log.error("Error marshalling Ont drag text");
				return "error";
			} 

			//log.info("Ont Client dragged "+ strWriter.toString());
			log.debug("Node xml set to: "+strWriter.toString());
			xmlContent(strWriter.toString());
			return "";	
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
}
