/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

package edu.harvard.i2b2.query;

//import edu.harvard.i2b2.select.soap.Select;
//import edu.harvard.i2b2.select.soap.SelectService;
//import edu.harvard.i2b2.select.soap.SelectServiceLocator;

/**
 *  
 *  Class: QuerySelectServiceClient.
 *  
 */

/**
 * @author wp066
 *
 */
public class QuerySelectServiceClient {
		
	public static org.w3c.dom.Document getXMLResults(QueryConceptTreeNodeData data, int mode) {
		/*
		// send web services message to obtain children 
    	// for a given node 
        try {
    	    // Make a service
    	    SelectService service = new SelectServiceLocator();
    	    // Use service to get stub that implement SDI
    	    
            java.net.URL endpoint = new java.net.URL(System.getProperty("selectservice"));
            //* call is going out here
	    	System.out.println(endpoint.toString());
    	    Select port = service.getSelect(endpoint);
    	    // Form the query
    	    org.w3c.dom.Document queryDoc = sendQuery(data, mode);
    	    
    	    // Make the call
    	    org.w3c.dom.Document queryResultDoc  = port.getDataMartRecords(queryDoc);

    	    // System.out.println(this.data.getWebserviceName() + " Service returned");
    	    if (queryResultDoc == null)
    	    {
    	    	System.out.println("Web service call failed");
        		//System.setProperty("statusMessage", "Web service call failed");
    	    	return null;
    	    }
    	    
    	    int nodecount = queryResultDoc.getElementsByTagName("patientData").getLength();
    	    System.out.println("total node count: "+nodecount);
    	    
    	    return queryResultDoc;
    	}
        catch(Exception e) {
        	e.printStackTrace();
        	System.err.println("Get Nodes: " + e.getMessage());
        	return null;
        }
        */
		return null;
	}
	
	private static org.w3c.dom.Document sendQuery(QueryConceptTreeNodeData data, int mode) {
		org.w3c.dom.Document domDoc = null;
    	try {
//    	    org.jdom.Element selectElement = new org.jdom.Element("selectParameters");
//    	    org.jdom.Document jqueryDoc = new org.jdom.Document(selectElement);
//    	    org.jdom.Element dbElement = new org.jdom.Element("i2b2Mart");
// 			dbElement.setText(data.lookupdb());
// 						
// 			selectElement.addContent(dbElement);
//    	    org.jdom.Element table = new org.jdom.Element("table");
//    	    table.setText(data.lookuptable());
//    	    table.setAttribute("abbr", "l");
//    	    table.setAttribute("numCols", "16");
//    	    table.setAttribute("withBlob", "false");
//    	    selectElement.addContent(table);
//    	    org.jdom.Element where = new org.jdom.Element("where");
//    	    int nextLevel = new Integer(data.hlevel()).intValue();
//    	    if(mode == 0) {
//    	    	nextLevel = new Integer(data.hlevel()).intValue() + 1;
//    	    }
//    	    String whereClause = "";
//    	    if(nextLevel == 1)
//    	    {
//    	    	whereClause = "l.c_hlevel = 1";
//    	    }
//    	    else if (System.getProperty("applicationName").equals("BIRN"))
//    	    {
//    	    	whereClause = "l.c_hlevel = " + Integer.toString(nextLevel) 
//    	    		+ " and l.c_fullname like '" + data.fullname().replace("\\", "\\\\") 
//    	    		+ "\\\\%' ESCAPE '|'";
//    	    }
//    	    else
//    	    {	
//    	    	whereClause = "l.c_hlevel = " + Integer.toString(nextLevel) 
//	    		+ " and l.c_fullname like '%" + data.fullname() + "%'";
//    	    }
//    	    where.setText(whereClause);
//    	    selectElement.addContent(where);
//
//    	    org.jdom.Element orderBy = new org.jdom.Element("orderBy");
//    	    orderBy.setText("c_name");
//    	    selectElement.addContent(orderBy);
//
//    	    //System.out.println((new XMLOutputter()).outputString(jqueryDoc));
//
//    	    org.jdom.output.DOMOutputter convertor = new org.jdom.output.DOMOutputter();
//    	    domDoc = convertor.output(jqueryDoc);    	    
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	    System.err.println("formQuery: " + e.getMessage());
    	}

    	return domDoc;	
	}
}
