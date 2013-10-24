/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
 package edu.harvard.i2b2.eclipse.plugins.workplace.util;

import java.io.IOException;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.wkplclient.datavo.wdo.XmlValueType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmlUtil {

    //to make this class singleton
    private static XmlUtil thisInstance;
	private static Log log = LogFactory.getLog(XmlUtil.class.getName());	
    
    static {
            thisInstance = new XmlUtil();
    }
    
    public static XmlUtil getInstance() {
        return thisInstance;
    }
    
    public static XmlValueType stringToXml(String c_xml) 
    {    	
    	if(c_xml == null)	
    		return null;
    	else {
    		// convert 
    		SAXBuilder parser = new SAXBuilder();
    		java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
    		Element rootElement = null;
    		try {
    			org.jdom.Document metadataDoc = parser.build(xmlStringReader);
    			org.jdom.output.DOMOutputter out = new DOMOutputter(); 
    			Document doc = out.output(metadataDoc);
    			rootElement = doc.getDocumentElement();
    		} catch (JDOMException e) {
    			log.error(e.getMessage());
    			return null;
    		} catch (IOException e1) {
    			log.error(e1.getMessage());
    			return null;
    		}
    		if (rootElement != null) {		
    			XmlValueType xml = new XmlValueType();
    			xml.getAny().add(rootElement);	
        		return xml;
    		}
    	}
    	return null;
    }
        
    
    public static String getName(XmlValueType xml)
    {    	
    	String name = null;
    	Element rootElement = xml.getAny().get(0);
    	NodeList nameElements = rootElement.getElementsByTagName("name");
    	// Group templates dont have tag 'name'
    	if (nameElements.getLength() == 0){
    		nameElements = rootElement.getElementsByTagNameNS("*", "panel");
    		if (nameElements.getLength() == 0){
    			nameElements = rootElement.getElementsByTagName("query_name");
    			if (nameElements.getLength() == 0){
    	    		// if we get to here and no name has been found then its a PDO.
    				// return generically -- change to obs or event etc one level up.
    				return "PDO";
    			}
    			else {
    				name = nameElements.item(0).getTextContent();
    			}
    		}
    		else {
    			name = nameElements.item(0).getAttributes().getNamedItem("name").getNodeValue();
    		}
    		if(name != null)
    			return name;
    		// Default to ABC if we cant find a name at all.
    		else
    			return "ABC"+MessageUtil.getInstance().getTimestamp();
    	}
    	// append result_instance_id to PATIENTSET <name> to create unique name
    	//   same for PATIENT_COUNT_XML
    	else if ((nameElements.item(0).getTextContent().equals("PATIENTSET")) ||
    			(nameElements.item(0).getTextContent().equals("PATIENT_COUNT_XML")) ){
    		NodeList resultElements = rootElement.getElementsByTagName("result_instance_id");
    		if(resultElements.getLength() > 0){
    			String resultInstanceId = resultElements.item(0).getTextContent();
    			return nameElements.item(0).getTextContent()+ "_" + resultInstanceId;
    		}
    	}

    	return nameElements.item(0).getTextContent();
    }
    
    
    public static String getPatientId(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList nameElements = rootElement.getElementsByTagName("patient_id");
    	if (nameElements.getLength() != 0){
    		return nameElements.item(0).getTextContent();
    	}
    	else
    		return MessageUtil.getInstance().getTimestamp();
    	
    }
    public static String getIndex(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList indexElements = rootElement.getElementsByTagName("index");
    	if (indexElements.getLength() == 0)
    		return null;

    	return indexElements.item(0).getTextContent();
    }
    
    public static Boolean hasConceptTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList conceptElements = rootElement.getElementsByTagNameNS("*", "concepts");
    	if (conceptElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    
    public static Boolean hasFolderTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList folderElements = rootElement.getElementsByTagName("folder");
    	if (folderElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    
    public static Boolean hasPatientSetTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	// <name>PATIENTSET</name>
    	NodeList psElements = rootElement.getElementsByTagName("name");
    	if (psElements.getLength() == 0)
    		return false;
    	else {
    		Boolean result = false;
    		for(int i = 0 ; i< psElements.getLength(); i++) {
    			String resultTypeName = psElements.item(i).getTextContent();
    	//		log.info(resultTypeName);
    			if(resultTypeName.equals("PATIENTSET")){	
    				result = true;
    			}
    		}
    		return result;
    	}
    }
    
    public static Boolean hasPatientCountTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	// <name>PATIENTSET</name>
    	NodeList pcElements = rootElement.getElementsByTagName("name");
    	if (pcElements.getLength() == 0)
    		return false;
    	else {
    		Boolean result = false;
    		for(int i = 0 ; i< pcElements.getLength(); i++) {
    			String resultTypeName = pcElements.item(i).getTextContent();
    	//		log.info(resultTypeName);
    			if(resultTypeName.equals("PATIENT_COUNT_XML")){	
    				result = true;
    			}
    		}
    		return result;
    	}
    }
    
    public static Boolean hasPrevQueryTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	// <query_master_id> indicates PrevQuery 
    	NodeList pqElements = rootElement.getElementsByTagNameNS("*","query_master");
    	if (pqElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    
    public static Boolean hasGroupTemplateTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	// <query_master_id> indicates PrevQuery 
    	NodeList gtElements = rootElement.getElementsByTagNameNS("*", "panel");
    	if (gtElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    
    public static Boolean hasQueryDefinitionTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList gtElements = rootElement.getElementsByTagNameNS("*", "query_definition");
    	if (gtElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    
    public static Boolean hasObservationTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList gtElements = rootElement.getElementsByTagNameNS("*", "observation_set");
    	if (gtElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    public static Boolean hasPatientDataTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList gtElements = rootElement.getElementsByTagNameNS("*", "patient_data");
    	if (gtElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
    public static Boolean hasPatientTag(XmlValueType xml)
    {    	
    	Element rootElement = xml.getAny().get(0);
    	NodeList gtElements = rootElement.getElementsByTagNameNS("*", "patient_set");
    	if (gtElements.getLength() == 0)
    		return false;
    	else
    		return true;
    }
}
