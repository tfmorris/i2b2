/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     
 */

package edu.harvard.i2b2.navigator;

	import java.util.*;
	import org.eclipse.jface.viewers.TreeViewer;
	import org.eclipse.swt.widgets.Display;
	import org.jdom.output.*;
//	import edu.harvard.i2b2.select.soap.SelectServiceLocator;
//	import edu.harvard.i2b2.select.soap.SelectService;
//import edu.harvard.i2b2.select.soap.Select;

	public class PrimativeResults {	
	    
		private String msWebServiceName = "";
		
		public PrimativeResults() {
	    	super();
	    }

	    public PrimativeResults(String sWebServiceName) {
	    	super();
	    	msWebServiceName = sWebServiceName;
	    }
		/*
		 *  web service to send query XML in SOAP message and return XML result
		 *  set. 
		 */
		
	    public  org.w3c.dom.Document sendSOAPtoServers() //throws Exception
	    {
	    	/*
    	    org.w3c.dom.Document oQueryResultDoc = null;
	        try {
	        	// Create the Service Locator
	    	    SelectService service = new SelectServiceLocator();
	    	    // Use service to get stub that implement SDI	    	    
	            java.net.URL endpoint = new java.net.URL(msWebServiceName);
	    	    Select port = service.getSelect(endpoint);
	    	    // Form the query XML DOM
	    	    org.w3c.dom.Document oQueryDoc = formQueryDom();
	    	    // Make the SOAP call and get the XML back in a W3C Dom Document
	    	    oQueryResultDoc  = port.getDataMartRecords(oQueryDoc);
	    	    //System.out.println(this.data.getWebserviceName() + " Service returned");
	    	    if (oQueryResultDoc == null)
	    	    {
	    	    	System.err.println("queryResultDoc is null");
	        		System.setProperty("statusMessage", "Web service call failed");
	    	    }
	    	}
	        catch(Exception e) {
	    		// throw service exception
	    		//System.out.println("in updateChildren " + e.getMessage());
	    		//e.printStackTrace();
//	    		throw (new javax.xml.rpc.ServiceException(e));
	    		if(e.getMessage().contains("Not+Found"))
	    		{
	    			System.setProperty("statusMessage", "WebService " + msWebServiceName + " not found");
	    		}
	    		else
	    			System.setProperty("statusMessage", e.getMessage());
	    	}
    	    return oQueryResultDoc;
    	    */
	    	return null;
	    }
	    
	    /*
	     *  makes the qery parameters into a W3C XML DOM to send in the SOAP message
	     */
	    
	    private org.w3c.dom.Document formQueryDom(){
	    	org.w3c.dom.Document domDoc = null;
	    	try {
//	 			System.out.println(this.data.getLookupDB());
	    		
	    		String sDatabaseSchemaName = "metadata";
                String sTableName = "Diagnoses";
	    	    String sAbbr = "l";
	    	    String sNumCols = "16";
	    	    String sWithBlob = "false";
	    	    String sWhereClause = "l.c_hlevel = 1";
	    	    String sOrderbyClause = "c_name";
	    	    /*
	    		String sDatabaseSchemaName = "asthma";
	    		String sTableName = "observation_fact";
	    	    String sAbbr = "o";
	    	    String sNumCols = "20";  // not sure if this will work
	    	    String sWithBlob = "false";
	    	    //String sWhereClause = "o.concept_cd='pftconcept'";   // work needed here
	    	    String sWhereClause = "o.patient_num=1";   // work needed here
	    		String sOrderbyClause = "patient_num";
	    		*/
	    		// top level tag (selectParameters)
	    	    org.jdom.Element selectElement = new org.jdom.Element("selectParameters");
	    	    org.jdom.Document jqueryDoc = new org.jdom.Document(selectElement);
	    	    // tag for the schema name
	    	    org.jdom.Element dbElement = new org.jdom.Element("i2b2Mart");
	 			dbElement.setText(sDatabaseSchemaName); 			
	 			selectElement.addContent(dbElement);
	 			// tag for the table name
	    	    org.jdom.Element table = new org.jdom.Element("table");
	    	    table.setText(sTableName);
	    	    table.setAttribute("abbr", sAbbr);
	    	    table.setAttribute("numCols", sNumCols);
	    	    table.setAttribute("withBlob", sWithBlob);
	    	    selectElement.addContent(table);
	    	    // tag for the where clause
	    	    org.jdom.Element where = new org.jdom.Element("where");
	    	    where.setText(sWhereClause);
	    	    selectElement.addContent(where);
	    	    // tag for the order by clause
	    	    org.jdom.Element orderBy = new org.jdom.Element("orderBy");
	    	    orderBy.setText(sOrderbyClause);
	    	    selectElement.addContent(orderBy);

	    	  System.out.println((new XMLOutputter()).outputString(jqueryDoc));

	    	    org.jdom.output.DOMOutputter convertor = new org.jdom.output.DOMOutputter();
	    	    domDoc = convertor.output(jqueryDoc);
	    	    
	    	}catch (Exception e){
	    	    System.err.println(e.getMessage());
	    		System.setProperty("statusMessage", e.getMessage());
	    	}

	    	return domDoc;	
	    }

	    /*
	     * Get the straight XML from a W3C DOM document
	     */
	    
	    public String getXMLStringFromW3C(org.w3c.dom.Document oW3CDomDocument) {
	    	String sXml = "";
	    	try {
	    	    org.jdom.input.DOMBuilder oBuilder = new org.jdom.input.DOMBuilder();
	    	    org.jdom.Document oJDomDocument = oBuilder.build(oW3CDomDocument);
	    		sXml = new XMLOutputter().outputString(oJDomDocument);
	    	}
	    	catch (Exception e) {
	    		System.err.println("Error in getXMLStringFromW3C");
	    	    System.err.println(e.getMessage());
	    	}
	    	return sXml;
	    }
	    
	    /*
	     * Get the straight XML from a JDOM DOM document
	     */
	    
	    public String getXMLStringFromJDom(org.jdom.Document oJDomDocument) {
	    	String sXml = "";
	    	try {
	    		sXml = new XMLOutputter().outputString(oJDomDocument);
	    	}
	    	catch (Exception e) {
	    		System.err.println("Error in getXMLStringFromJDOM");
	    	    System.err.println(e.getMessage());
	    	}
	    	return sXml;
	    }
	    
	    private void getVisualizerText(org.w3c.dom.Document resultDoc){
	    	try {
	    	    org.jdom.input.DOMBuilder builder = new org.jdom.input.DOMBuilder();
	    	    org.jdom.Document jresultDoc = builder.build(resultDoc);
	    	    org.jdom.Namespace ns = jresultDoc.getRootElement().getNamespace();
	System.out.println((new XMLOutputter()).outputString(jresultDoc));   	
/*	    	    Iterator iterator = jresultDoc.getRootElement().getChildren("patientData", ns).iterator();
	    	    while (iterator.hasNext())
	    	    {
	    	    	org.jdom.Element patientData = (org.jdom.Element) iterator.next();
	       	    	org.jdom.Element lookup = (org.jdom.Element) patientData.getChild(this.data.getLookupTable().toLowerCase(), ns).clone();
	       	    	XMLOutputter fmt = new XMLOutputter();
	    	    	String XMLContents = fmt.outputString(lookup);      	    
	    	    	TreeData childData = new TreeData(XMLContents, this.data.getLookupTable(), this.data.getLookupDB(), this.data.getWebserviceName());
	    	    	if(!(childData.getVisualAttributes().substring(1,2).equals("H")))
	    	    	{	
	    	    		TreeNode child = new TreeNode(childData);
	    	    	// if the child is a folder/directory set it up with a leaf placeholder
	    	    		if((childData.getVisualAttributes().equals("FA")) || (childData.getVisualAttributes().equals("CA")))
	    	    		{
	    	    			TreeNode placeholder = new TreeNode(childData.getLevel() + 1, "working...", "working...", "LAO", childData.getLookupTable(), childData.getLookupDB(), childData.getWebserviceName());
	    	    			child.addChild(placeholder);
	    	    		}
	    	    		this.addChild(child);
	    	    	}
	    	    }
	    	    org.jdom.Element result = (org.jdom.Element) jresultDoc.getRootElement().getChild("result");
				String resultString = result.getChildTextTrim("resultString", ns);
				System.setProperty("statusMessage", resultString);*/
	    	}catch (Exception e) {
	    		System.setProperty("statusMessage", e.getMessage());
	    	   // System.err.println(e.getMessage());
	    	}
	    }

	    private void getNodesFromXML(org.w3c.dom.Document resultDoc){
	    	try {
	    	    org.jdom.input.DOMBuilder builder = new org.jdom.input.DOMBuilder();
	    	    org.jdom.Document jresultDoc = builder.build(resultDoc);
	    	    org.jdom.Namespace ns = jresultDoc.getRootElement().getNamespace();
	System.out.println((new XMLOutputter()).outputString(jresultDoc));   	
/*	    	    Iterator iterator = jresultDoc.getRootElement().getChildren("patientData", ns).iterator();
	    	    while (iterator.hasNext())
	    	    {
	    	    	org.jdom.Element patientData = (org.jdom.Element) iterator.next();
	       	    	org.jdom.Element lookup = (org.jdom.Element) patientData.getChild(this.data.getLookupTable().toLowerCase(), ns).clone();
	       	    	XMLOutputter fmt = new XMLOutputter();
	    	    	String XMLContents = fmt.outputString(lookup);      	    
	    	    	TreeData childData = new TreeData(XMLContents, this.data.getLookupTable(), this.data.getLookupDB(), this.data.getWebserviceName());
	    	    	if(!(childData.getVisualAttributes().substring(1,2).equals("H")))
	    	    	{	
	    	    		TreeNode child = new TreeNode(childData);
	    	    	// if the child is a folder/directory set it up with a leaf placeholder
	    	    		if((childData.getVisualAttributes().equals("FA")) || (childData.getVisualAttributes().equals("CA")))
	    	    		{
	    	    			TreeNode placeholder = new TreeNode(childData.getLevel() + 1, "working...", "working...", "LAO", childData.getLookupTable(), childData.getLookupDB(), childData.getWebserviceName());
	    	    			child.addChild(placeholder);
	    	    		}
	    	    		this.addChild(child);
	    	    	}
	    	    }
	    	    org.jdom.Element result = (org.jdom.Element) jresultDoc.getRootElement().getChild("result");
				String resultString = result.getChildTextTrim("resultString", ns);
				System.setProperty("statusMessage", resultString);*/
	    	}catch (Exception e) {
	    		System.setProperty("statusMessage", e.getMessage());
	    	   // System.err.println(e.getMessage());
	    	}
	    }
	    
	public static void main(String[] args) {
	    String ssService = "http://{server}:9091/i2b2/services/Select";
	    // String ssService = "http://{server}:8080/i2b2/services/Select";
		PrimativeResults oResult = new PrimativeResults(ssService);
//		System.out.println(oResult.getXMLStringFromW3C(oResult.sendSOAPtoServer()));
	}
	
	/*
	 
<?xml version="1.0" encoding="UTF-8"?>
<selectData>

<patientData>

<observation_fact>

<encounter_num>93</encounter_num>
<concept_cd>07810</concept_cd>
<patient_num>2</patient_num>
<provider_id>505</provider_id>
<start_date>13.04.2001 00:00:00</start_date>
<principal_concept>2</principal_concept>
<valtype_cd />
<tval_char />
<nval_num />
<valueflag_cd />
<quantity_num />
<confidence_num />
<units_cd />
<end_date />
<location_cd />
<update_date />
<download_date />
<import_date>14.09.2004 16:19:00</import_date>
<sourcesystem_cd>RPDRASTHMA</sourcesystem_cd>

</observation_fact>

</patientData>

<patientData>

<observation_fact>

<encounter_num>121</encounter_num>
<concept_cd>1100</concept_cd>
<patient_num>2</patient_num>
<provider_id>10786</provider_id>
<start_date>27.01.2004 00:00:00</start_date>
<principal_concept>2</principal_concept>
<valtype_cd />
<tval_char />
<nval_num />
<valueflag_cd />
<quantity_num />
<confidence_num />
<units_cd />
<end_date />
<location_cd />
<update_date />
<download_date />
<import_date>14.09.2004 16:19:00</import_date>
<sourcesystem_cd>RPDRASTHMA</sourcesystem_cd>

</observation_fact>

</patientData>

<result>

<resultCode>0</resultCode>
<resultString>155 records returned</resultString>

</result>

</selectData>

	 */
}
