/*
 * Copyright (c) 2006-2009 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.eclipse.plugins.ontology.views.find;

import java.util.*;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.NodeBrowser;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.TreeData;

import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntologyResponseMessage;
import edu.harvard.i2b2.ontclient.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetReturnType;

public class TreeNode
{
	private Log log = LogFactory.getLog(TreeNode.class.getName());
	private TreeData data;
    private List children = new ArrayList();
    private TreeNode parent;
    private int result;
    
    public TreeNode(int level, String fullName, String name, String visualAttributes)
    {
  	  this.data = new TreeData(level, fullName, name, visualAttributes);
    }
       
    public TreeNode(TreeData data)
    {
    	this.data = data;
    }
    
    public Object getParent()
    {
      return parent;
    }

    public TreeNode addChild(TreeNode child)
    {
      children.add(child);
      child.parent = this;
      return this;
    }

    public List getChildren()
    {
      return children;
    }
    
    public TreeData getData()
    {
    	return this.data;
    }
    

    @Override
	public String toString()
    {
      return this.data.getName();
    }

    public String getIconKey()
    {
    	String key = null;
    	if (data.getVisualattributes().substring(0,1).equals("F"))
    	{
    		if ((data.getVisualattributes().substring(1).equals("A")) ||
    				(data.getVisualattributes().substring(1).equals("I"))  ||
    				(data.getVisualattributes().substring(1).equals("H")))
    			key = "closedFolder";
    		else if ((data.getVisualattributes().substring(1).equals("AO")) ||
    				(data.getVisualattributes().substring(1).equals("IO"))  ||
    				(data.getVisualattributes().substring(1).equals("HO")))
    			key = "openFolder";
    	}
    	else if (data.getVisualattributes().substring(0,1).equals("C"))
    	{
    		if ((data.getVisualattributes().substring(1).equals("A")) ||
    				(data.getVisualattributes().substring(1).equals("I"))  ||
    				(data.getVisualattributes().substring(1).equals("H")))
    			key = "closedCase";
    		else if ((data.getVisualattributes().substring(1).equals("AO")) ||
    				(data.getVisualattributes().substring(1).equals("IO"))  ||
    				(data.getVisualattributes().substring(1).equals("HO")))
    			key = "openCase";
    	}
    	else if (data.getVisualattributes().substring(0,1).equals("L"))
    	{
    		key = "leaf";
    	}
    	else if (data.getVisualattributes().substring(0,1).equals("M"))
    	{
    		key = "multi";
    	}
    	else if (data.getVisualattributes().equals("C-UNDEF"))
    	{
    		key = "undefined";
    	}
    	return key;
    }
    
	public Thread getXMLData(TreeViewer viewer, NodeBrowser browser) {
		final TreeNode theNode = this;
		final TreeViewer theViewer = viewer;
		final Display theDisplay = Display.getCurrent();
		return new Thread() {
			@Override
			public void run(){
				try {
					theNode.updateChildren(theDisplay, theViewer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.setProperty("statusMessage", e.getMessage());					
				}
				theDisplay.syncExec(new Runnable() {
					public void run() {
					//	theViewer.expandToLevel(theNode, 1);
						theViewer.refresh(theNode);
					//	theBrowser.refresh();
					}
				});
			}
		};
	}
	   public void updateChildren(final Display theDisplay, final TreeViewer theViewer) 
	    {
	    	try {
				GetChildrenType parentType = new GetChildrenType();
				parentType.setMax(Integer.parseInt(System.getProperty("OntFindMax")));
				parentType.setHiddens(Boolean.parseBoolean(System.getProperty("OntFindHiddens")));
				parentType.setSynonyms(Boolean.parseBoolean(System.getProperty("OntFindSynonyms")));
				
				parentType.setParent(this.getData().getKey());

	    	    OntologyResponseMessage msg = new OntologyResponseMessage();
				StatusType procStatus = null;	
				while(procStatus == null || !procStatus.getType().equals("DONE")){
					String response = OntServiceDriver.getChildren(parentType, "FIND");
					procStatus = msg.processResult(response);
					if(procStatus.getValue().equals("MAX_EXCEEDED")) {
						theDisplay.syncExec(new Runnable() {
						public void run() {
			    	    	MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), 
					    	 			SWT.ICON_QUESTION | SWT.YES | SWT.NO);
							mBox.setText("Please Note ...");
							mBox.setMessage("The node has exceeded maximum number of children\n"
											+ "Populating the node will be slow\n"
											+"Do you want to continue?");
							result = mBox.open();
						}
	    	    	});
						if(result == SWT.NO) {
							TreeNode node = (TreeNode) this.getChildren().get(0);
							node.getData().setName("Over maximum number of child nodes");
							procStatus.setType("DONE");
						}
						else {
							parentType.setMax(null);
							response = OntServiceDriver.getChildren(parentType, "FIND");
							procStatus = msg.processResult(response);
						}
					}
//	/				else  -- other error codes
					// TABLE_ACCESS_DENIED and USER_INVALID, DATABASE ERROR
					else if (procStatus.getType().equals("ERROR")){
						System.setProperty("statusMessage",  procStatus.getValue());				
						theDisplay.syncExec(new Runnable() {
							public void run() {
								MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
								mBox.setText("Please Note ...");
								mBox.setMessage("Server reports: " +  System.getProperty("statusMessage"));
								int result = mBox.open();
							}
						});
						return;
					}
				}
				ConceptsType allConcepts = msg.doReadConcepts();   	    
				List concepts = allConcepts.getConcept();
				getChildren().clear();
				getNodesFromXMLString(concepts);
	    	} catch (AxisFault e) {
	    		theDisplay.syncExec(new Runnable() {
	    			public void run() {
	    				MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
	    				mBox.setText("Please Note ...");
	    				mBox.setMessage("Unable to make a connection to the remote server\n" +  
	    				"This is often a network error, please try again");
	    				int result = mBox.open();
	    			}
	    		});
			}catch (Exception e) {
				theDisplay.syncExec(new Runnable() {
					public void run() {
						// e.getMessage() == Incoming message input stream is null  -- for the case of connection down.
						MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
						mBox.setText("Please Note ...");
						mBox.setMessage("Error message delivered from the remote server\n" +  
						"You may wish to retry your last action");
						int result = mBox.open();
					}
				});		
			}
	    	
	    	
	    }
	   private void getNodesFromXMLString(List concepts){	    	
	    	Iterator it = concepts.iterator();

	    	while(it.hasNext()){
	    		TreeData child = new TreeData((ConceptType) it.next()); 		 

	    		TreeNode childNode = new TreeNode(child);
	    		// if the child is a folder/directory set it up with a leaf placeholder
	    		if((child.getVisualattributes().equals("FA")) || (child.getVisualattributes().equals("CA")))
	    		{
	    			TreeNode placeholder = new TreeNode(child.getLevel() + 1, "working...", "working...", "LAO");
	    			childNode.addChild(placeholder);
	    		}
	    		else if	((child.getVisualattributes().equals("FH")) || (child.getVisualattributes().equals("CH")))
	    		{
	    			TreeNode placeholder = new TreeNode(child.getLevel() + 1, "working...", "working...", "LHO");
	    			childNode.addChild(placeholder);
	    		}
	    		this.addChild(childNode);

	    	}	    	
	    }
     
	    public void updateCategories(final Display theDisplay, final TreeViewer theViewer) 
	    {
	    	try {
				GetReturnType request = new GetReturnType();
				request.setType("core");
				
	    	    OntologyResponseMessage msg = new OntologyResponseMessage();
				StatusType procStatus = null;	
				while(procStatus == null || !procStatus.getType().equals("DONE")){
					String response = OntServiceDriver.getCategories(request, "FIND");
					procStatus = msg.processResult(response);
					
//					if  other error codes
//					TABLE_ACCESS_DENIED and USER_INVALID
					if (procStatus.getType().equals("ERROR")){
						System.setProperty("statusMessage",  procStatus.getValue());				
						theDisplay.syncExec(new Runnable() {
							public void run() {
								MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
								mBox.setText("Please Note ...");
								mBox.setMessage("Server reports: " +  System.getProperty("statusMessage"));
								int result = mBox.open();
							}
						});
						return;
					}	
					
					procStatus.setType("DONE");
				}
				ConceptsType allConcepts = msg.doReadConcepts();   	    
				List concepts = allConcepts.getConcept();
				getNodesFromXMLString(concepts);	
	    	} catch (AxisFault e) {
	    		theDisplay.syncExec(new Runnable() {
	    			public void run() {
	    				MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
	    				mBox.setText("Please Note ...");
	    				mBox.setMessage("Unable to make a connection to the remote server\n" +  
	    				"This is often a network error, please try again");
	    				int result = mBox.open();
	    			}
	    		});
			} catch (Exception e) {
				theDisplay.syncExec(new Runnable() {
					public void run() {
						// e.getMessage() == Incoming message input stream is null  -- for the case of connection down.
						MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
						mBox.setText("Please Note ...");
						mBox.setMessage("Error message delivered from the remote server\n" +  
						"You may wish to retry your last action");
						int result = mBox.open();
					}
				});	
			}
		
	    }	   
	    
	    
////	 Below are all the old Select service based ontology functions......    
	 	   
/*   public void updateChildren() //throws Exception
    {
    	// method to send web services message to obtain children 
    	// for a given node 
    	
        try {
    	    // Make a service
    	    SelectService service = new SelectServiceLocator();
    	    // Use service to get stub that implement SDI
    	    
            java.net.URL endpoint = new java.net.URL(this.data.getWebserviceName());
            //* call is going out here
	    	System.out.println(this.data.getWebserviceName().toString());
    	    Select port = service.getSelect(endpoint);
    	    // Form the query
    	    org.w3c.dom.Document queryDoc = formQuery();
    	    
    	    // Make the call
    	    org.w3c.dom.Document queryResultDoc  = port.getDataMartRecords(queryDoc);

//    	    System.out.println(this.data.getWebserviceName() + " Service returned");
    	    if (queryResultDoc == null)
    	    {
    	    	//System.err.println("queryResultDoc is null");
        		System.setProperty("statusMessage", "Web service call failed");
    	    	return;
    	    }
    	    getNodesFromXML(queryResultDoc);
    	}catch(Exception e)
    	{
    		// throw service exception
    		//System.out.println("in updateChildren " + e.getMessage());
    		//e.printStackTrace();
//    		throw (new javax.xml.rpc.ServiceException(e));
    		if(e.getMessage().contains("Not+Found"))
    		{
    			System.setProperty("statusMessage", "WebService " + this.data.getWebserviceName() + " not found");
    		}
    		else
    			System.setProperty("statusMessage", e.getMessage());
    	}
    }
    private org.w3c.dom.Document formQuery(){
    	org.w3c.dom.Document domDoc = null;
    	try {
    	    org.jdom.Element selectElement = new org.jdom.Element("selectParameters");
    	    org.jdom.Document jqueryDoc = new org.jdom.Document(selectElement);
    	    org.jdom.Element dbElement = new org.jdom.Element("i2b2Mart");
 			dbElement.setText(this.data.getLookupDB());
 //   	    dbElement.setText("METADATA_DEV");
// 			System.out.println(this.data.getLookupDB()); 			
 			selectElement.addContent(dbElement);
    	    org.jdom.Element table = new org.jdom.Element("table");
    	    table.setText(this.data.getLookupTable());
    	    table.setAttribute("abbr", "l");
    	    table.setAttribute("numCols", "16");
    	    table.setAttribute("withBlob", "false");
    	    selectElement.addContent(table);
    	    org.jdom.Element where = new org.jdom.Element("where");
    	    int nextLevel = this.data.getLevel() + 1;
    	    String whereClause = "";
    	    if(nextLevel == 1)
    	    {
    	    	whereClause = "l.c_hlevel = 1";
    	    }
    	    else
    	    {
    	    	whereClause = "l.c_hlevel = " + Integer.toString(nextLevel) + " and l.c_fullname like '%" + this.data.getFullName() + "%'";
    	    }
    	    where.setText(whereClause);
    	    selectElement.addContent(where);

    	    org.jdom.Element orderBy = new org.jdom.Element("orderBy");
    	    orderBy.setText("c_name");
    	    selectElement.addContent(orderBy);

//    	  System.out.println((new XMLOutputter()).outputString(jqueryDoc));

    	    org.jdom.output.DOMOutputter convertor = new org.jdom.output.DOMOutputter();
    	    domDoc = convertor.output(jqueryDoc);
    	    
    	}catch (Exception e){
    	    System.err.println(e.getMessage());
    		System.setProperty("statusMessage", e.getMessage());
    	}

    	return domDoc;	
    }

    private void getNodesFromXML(org.w3c.dom.Document resultDoc){
    	try {
    	    org.jdom.input.DOMBuilder builder = new org.jdom.input.DOMBuilder();
    	    org.jdom.Document jresultDoc = builder.build(resultDoc);
    	    org.jdom.Namespace ns = jresultDoc.getRootElement().getNamespace();
//System.out.println((new XMLOutputter()).outputString(jresultDoc));   	
    	    Iterator iterator = jresultDoc.getRootElement().getChildren("patientData", ns).iterator();
    	    while (iterator.hasNext())
    	    {
    	    	org.jdom.Element patientData = (org.jdom.Element) iterator.next();
       	    	org.jdom.Element lookup = (org.jdom.Element) patientData.getChild(this.data.getLookupTable().toLowerCase(), ns).clone();
       	    	XMLOutputter fmt = new XMLOutputter();
    	    	String XMLContents = fmt.outputString(lookup);      	    
    	    	TreeData childData = new TreeData(XMLContents, this.data.getLookupTable(), this.data.getLookupDB(), this.data.getWebserviceName());
    	    	if(!(childData.getVisualattributes().substring(1,2).equals("H")))
    	    	{	
    	    		TreeNode child = new TreeNode(childData);
    	    	// if the child is a folder/directory set it up with a leaf placeholder
    	    		if((childData.getVisualattributes().equals("FA")) || (childData.getVisualattributes().equals("CA")))
    	    		{
    	    			TreeNode placeholder = new TreeNode(childData.getLevel() + 1, "working...", "working...", "LAO", childData.getLookupTable(), childData.getLookupDB(), childData.getWebserviceName());
    	    			child.addChild(placeholder);
    	    		}
    	    		this.addChild(child);
    	    	}
    	    }
    	    org.jdom.Element result = (org.jdom.Element) jresultDoc.getRootElement().getChild("result");
			String resultString = result.getChildTextTrim("resultString", ns);
			System.setProperty("statusMessage", resultString);
    	}catch (Exception e) {
    		System.setProperty("statusMessage", e.getMessage());
    	   // System.err.println(e.getMessage());
    	}
    }
*/
} 