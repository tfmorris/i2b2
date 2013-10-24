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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.previousquery.datavo.PreviousQueryJAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.dnd.DndType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.InstanceResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.MasterResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.PsmQryHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.PsmRequestTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryInstanceType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryMasterType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryResultInstanceType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ResultResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.UserRequestType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.UserType;

/*
 * QueryPreviousRunsPanel.java
 *
 * Created on September 19, 2006, 1:55 PM
 */

/**
 *
 * @author  wp066
 */
public class QueryPreviousRunsPanel extends javax.swing.JPanel implements ActionListener,
							TreeExpansionListener {
    
	private static final Log log = LogFactory.getLog(QueryPreviousRunsPanel.class);
	
	private DefaultMutableTreeNode top = null;
    private DefaultTreeModel treeModel = null;
    private QueryC parent = null;
    private ArrayList<QueryMasterData> previousQueries = null;
    private ViewPart parentView = null;
    private String cellStatus = "";
    
    private boolean ascending = false;
    public void ascending(boolean b) {ascending = b;}
    
    private String lastRequestMessage = "";
    public String lastRequestMessage() {return lastRequestMessage;}
    
    private String lastResponseMessage = "";
    public String lastResponseMessage() {return lastResponseMessage;}
	
    /** Creates new form QueryPreviousRunsPanel */
    public QueryPreviousRunsPanel(QueryC parentC){//, ExplorerC explorerC) {
    	parent = parentC;
    	//explorer = explorerC;
    	loadPreviousQueries(false);
    	
        initComponents();
        createPopupMenu();  
    }
    
    public QueryPreviousRunsPanel(ViewPart parent){
    	log.info("Previous Query plugin version 1.0.0");
    	
    	parentView = parent;
    	loadPreviousQueries(false);
    	
        initComponents();
        createPopupMenu();  
        
        if(cellStatus.equalsIgnoreCase("")) {
        	reset(200, false);
        }
    }
    
    public DefaultMutableTreeNode addNode(QueryConceptTreeNodeData node, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryConceptTreeNodeData tmpData = new QueryConceptTreeNodeData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
	
		return childNode;
    }
    
    public DefaultMutableTreeNode addNode(QueryMasterData node, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryMasterData tmpData = new QueryMasterData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
	
		return childNode;
    }
    
    public DefaultMutableTreeNode addNode(QueryInstanceData node, DefaultMutableTreeNode parent) {
		QueryMasterData logicdata = (QueryMasterData) parent.getUserObject();
		logicdata.runs.add(node);
		
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryInstanceData tmpData = new QueryInstanceData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		
		DefaultMutableTreeNode tmpnode = (DefaultMutableTreeNode) parent.getChildAt(0);
		QueryData tmpdata = (QueryData) tmpnode.getUserObject();
		if(tmpdata.name().equalsIgnoreCase("working ......")) {
			treeModel.removeNodeFromParent(tmpnode);
		}
	
		return childNode;
    }
    
    public DefaultMutableTreeNode addNode(QueryResultData node, DefaultMutableTreeNode parent) {
		//QueryInstanceData rundata = (QueryInstanceData) parent.getUserObject();
		
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryInstanceData tmpData = new QueryInstanceData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		
		DefaultMutableTreeNode tmpnode = (DefaultMutableTreeNode) parent.getChildAt(0);
		QueryData tmpdata = (QueryData) tmpnode.getUserObject();
		if(tmpdata.name().equalsIgnoreCase("working ......")) {
			treeModel.removeNodeFromParent(tmpnode);
		}
	
		return childNode;
    }
    
    public DefaultMutableTreeNode addNode(QueryMasterData node) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryMasterData tmpData = new QueryMasterData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		treeModel.insertNodeInto(childNode, top, top.getChildCount());
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		
		return childNode;
    }
    
    public DefaultMutableTreeNode insertNode(QueryMasterData node) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);
		
		QueryMasterData tmpData = new QueryMasterData();
		tmpData.name("working ......");
		tmpData.tooltip("A tmp node");
		tmpData.visualAttribute("LAO");
		DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpData);
		
		if(ascending) {
			treeModel.insertNodeInto(childNode, top, top.getChildCount());
		}
		else {
			treeModel.insertNodeInto(childNode, top, 0);
		}
		
		if(!(node.visualAttribute().startsWith("L") || node.visualAttribute().equalsIgnoreCase("MA"))) {
			treeModel.insertNodeInto(tmpNode, childNode, childNode.getChildCount());
		}
		//Make sure the user can see the lovely new node.
		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		
		previousQueries.add(node);
		
		return childNode;
    }
    
    protected MessageHeaderType getMessageHeader() {
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
	
	protected String generateMessageId() {
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
	
	private String writeContentQueryXML(boolean getAllInGroup) {

		// create header
		PsmQryHeaderType headerType = new PsmQryHeaderType();
		
		UserType userType = new UserType();
		String userId = System.getProperty("user");
		userType.setLogin(userId);
		userType.setValue(userId);
		
		headerType.setUser(userType);
		if(getAllInGroup) {
			headerType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_GROUP_ID);
		}
		else {
			headerType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID);
		}
		
		UserRequestType userRequestType = new UserRequestType();
		//if(getAllInGroup) {
			userRequestType.setGroupId("Asthma");
		//}
		//else {
			userRequestType.setUserId(userId);
		//}
			String maxNum = System.getProperty("QueryToolMaxQueryNumber");
			if(maxNum == null || maxNum.equals("")) {
				userRequestType.setFetchSize(20);
			}
			else {
				userRequestType.setFetchSize(Integer.parseInt(maxNum));
			}
			
		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		BodyType bodyType = new BodyType();
		edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory();
		bodyType.getAny().add(psmOf.createPsmheader(headerType));
		bodyType.getAny().add(psmOf.createRequest(userRequestType));
		MessageHeaderType messageHeader = getMessageHeader();
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setMessageHeader(messageHeader);
		requestMessageType.setRequestHeader(requestHeader);
			
		JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory(); 
			jaxbUtil.marshaller(of.createRequest(requestMessageType), strWriter);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println("Generated content XML request: " + strWriter.toString());
		return strWriter.toString();
	}
	
	public String loadPreviousQueries(boolean getAllInGroup) {
		System.out.println("Loading previous queries for: "+System.getProperty("user"));
		String xmlStr = writeContentQueryXML(getAllInGroup);
		lastRequestMessage = xmlStr;
		//System.out.println(xmlStr);
		
		String responseStr = QueryListNamesClient.sendQueryRequestREST(xmlStr);
		if(responseStr.equalsIgnoreCase("CellDown")) {
			cellStatus = new String("CellDown");
			return "CellDown";
		}
		lastResponseMessage = responseStr;
		
		try {
			JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
			JAXBElement jaxbElement =  jaxbUtil.unMashallFromString(responseStr);
			ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
			BodyType bt = messageType.getMessageBody();
			MasterResponseType masterResponseType = (MasterResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(), edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.MasterResponseType.class);
			previousQueries = new ArrayList<QueryMasterData>();
			for(QueryMasterType queryMasterType:masterResponseType.getQueryMaster()) {
				QueryMasterData tmpData;
				tmpData = new QueryMasterData();				
				XMLGregorianCalendar cldr = queryMasterType.getCreateDate();
				tmpData.name(queryMasterType.getName()+ " ["+addZero(cldr.getMonth())+"-"+addZero(cldr.getDay())+"-"
						+addZero(cldr.getYear())+" ]"+" ["+queryMasterType.getUserId()+"]");
				tmpData.tooltip("A query run by "+queryMasterType.getUserId());//System.getProperty("user"));
				tmpData.visualAttribute("CA");
				tmpData.xmlContent(null);
				tmpData.id(new Integer(queryMasterType.getQueryMasterId()).toString());
				tmpData.userId(queryMasterType.getUserId()); //System.getProperty("user"));
				previousQueries.add(tmpData);				
			}
			return "";
		}
		catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());
        
        QueryMasterData tmpData = new QueryMasterData();
		tmpData.name("Queries by "+System.getProperty("user"));
		tmpData.tooltip("Previous query runs");
		tmpData.visualAttribute("CA");
		top = new DefaultMutableTreeNode(tmpData);
        //top = new DefaultMutableTreeNode("Root Node");
        treeModel = new DefaultTreeModel(top);
        //treeModel.addTreeModelListener(new MyTreeModelListener());

        jTree1 = new JTree(treeModel);
        jTree1.setEditable(false);
        
        //jTree1.getSelectionModel().setSelectionMode
        //        (TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setShowsRootHandles(true);
        //JScrollPane treeView = new JScrollPane(jTree1);
        jTree1.setRootVisible(false);
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setCellRenderer(new MyRenderer());
        ToolTipManager.sharedInstance().registerComponent(jTree1);
        
        if(cellStatus.equalsIgnoreCase("CellDown")) {
        	DefaultMutableTreeNode childNode = new DefaultMutableTreeNode("Data Repository Cell is unavailable");
    		treeModel.insertNodeInto(childNode, top, top.getChildCount());
    		// Make sure the user can see the lovely new node.
    		jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        
        ArrayList<QueryMasterData> queries = previousQueries;
        if(queries != null) {
        	for(int i=0; i<queries.size(); i++) {
        		addNode(queries.get(i));
        	}
        }

        jScrollPane1.setViewportView(jTree1);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
        
        jTree1.setTransferHandler(new NodeCopyTransferHandler());
        jTree1.addTreeExpansionListener(this);
    }
    
    public void reset(int number, boolean byName) {
    	while(top.getChildCount()>0) {
	    	for (int i=0;i<top.getChildCount();i++) {
	    		DefaultMutableTreeNode node = (DefaultMutableTreeNode)top.getChildAt(i);
	            //System.out.println("Remove node: "+
	             //   		((QueryTreeNodeData)node.getUserObject()).tooltip()); 
	            treeModel.removeNodeFromParent(node);
	    	}
    	}
    	
    	ArrayList<QueryMasterData> queries = null;
    	
    	if(byName) {
    		queries = new ArrayList<QueryMasterData>(previousQueries);
    		Collections.sort(queries, new Comparator<QueryMasterData>() {
    			public int compare(QueryMasterData d1, QueryMasterData d2) {
    				return java.text.Collator.getInstance().compare(d1.name(), d2.name());
    			}
    		});
    	}
    	else {
    		queries = previousQueries;
    	}
    	
        if(queries != null) {
        	if(number > queries.size()) {
        		number = queries.size();
        	}
        	
        	if(!ascending) {
	        	for(int i=queries.size()-number; i<queries.size(); i++) {
	        		addNode(queries.get(i));
	        	}
        	}
        	else {
        		for(int i=queries.size()-1; i>=queries.size()-number; i--) {
	        		addNode(queries.get(i));
	        	}
        	}
        }
    }
    	
    class QueryDataTransferable implements Transferable {
    	public QueryDataTransferable(Object data) {
    		super();
    		this.data = data;
    		flavors[0] = DataFlavor.stringFlavor;
    	}
    	
    	public DataFlavor[] getTransferDataFlavors() {
    		return flavors;
    	}
    	
    	public boolean isDataFlavorSupported(DataFlavor flavor) {
    		return true;
    	}
    	
    	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    		return data;
    	}
    	
    	private Object data;
    	private final DataFlavor[] flavors = new DataFlavor[1];
    }
    
    class NodeCopyTransferHandler extends TransferHandler {
    	protected NodeCopyTransferHandler() {
    		super();
    	}
    	
    	protected Transferable createTransferable(JComponent c) {
    		
    		Transferable t = null;
    		DefaultMutableTreeNode node = 
				  (DefaultMutableTreeNode) jTree1.getSelectionPath().getLastPathComponent();
    		String str = null;
    		if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryMasterData")) {
    			StringWriter strWriter = new StringWriter();
				try {
					JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
					
					QueryMasterData ndata = (QueryMasterData) node.getUserObject();
	    			//if(ndata.xmlContent() == null) { 
	    			//setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    			QueryMasterType queryMasterType = new QueryMasterType();
	    			queryMasterType.setName(ndata.name());
	    			queryMasterType.setQueryMasterId(new Integer(ndata.id()).intValue());
	    			queryMasterType.setUserId(ndata.userId());
	    			queryMasterType.setGroupId("Asthma");
					//strWriter = new StringWriter();
					DndType dnd = new DndType();
					edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory();
					dnd.getAny().add(psmOf.createQueryMaster(queryMasterType));
					edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory();
					PreviousQueryJAXBUtil.getJAXBUtil().marshaller(of.createPluginDragDrop(dnd), strWriter);				
				} catch (JAXBUtilException e1) {
					//log.error("Error marshalling Ont drag text");
			//		throw e;
					e1.printStackTrace();
				} 
				
				str = strWriter.toString();
				System.out.println("Node xml set to: "+strWriter.toString());
    		}
    		else if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryInstanceData")) {
    			str = "logicquery";
    		}
    		else if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryResultData")) {
	    		QueryData nodedata = (QueryData) node.getUserObject();
	    		str = nodedata.name()+":"+((QueryResultData)nodedata).patientRefId();
	    		if(str.equalsIgnoreCase("working ......")) {
	    			str = "logicquery";
	    		}
	    		
	    		StringWriter strWriter = new StringWriter();
				try {
					JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
					
					JAXBElement jaxbElement = jaxbUtil.unMashallFromString(nodedata.xmlContent());
					ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
					BodyType bt = messageType.getMessageBody();
					ResultResponseType resultResponseType = (ResultResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(),ResultResponseType.class);
					QueryResultInstanceType queryResultInstanceType = resultResponseType.getQueryResultInstance().get(0);
					//strWriter = new StringWriter();
					DndType dnd = new DndType();
					edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory();
					dnd.getAny().add(psmOf.createQueryResultInstance(queryResultInstanceType));
					edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory();
					PreviousQueryJAXBUtil.getJAXBUtil().marshaller(of.createPluginDragDrop(dnd), strWriter);				
				} catch (JAXBUtilException e) {
					//log.error("Error marshalling Ont drag text");
			//		throw e;
					e.printStackTrace();
				} 

				//log.info("Ont Client dragged "+ strWriter.toString());
				str = strWriter.toString();
				System.out.println("Node xml set to: "+strWriter.toString());
    		}

			t = new QueryDataTransferable(str);
    		return t;
    	}
    	
    	public int getSourceActions(JComponent c) {
    		return TransferHandler.COPY;
    	}
    }
    
    class MyRenderer extends DefaultTreeCellRenderer {

        public MyRenderer() {
           
        }

        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
	 
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("String")) {
            	String nodeInfo = (String)(node.getUserObject());
            	setText(nodeInfo);
		        setToolTipText(nodeInfo);
		        setIcon(null);
		        setForeground(Color.RED);
            }
            else {
            	QueryData nodeInfo = (QueryData)(node.getUserObject());
		        setToolTipText(nodeInfo.tooltip());
		        setIcon(getImageIcon(nodeInfo));
            }
            //else if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryInstanceData")) {
            //	QueryInstanceData nodeInfo = (QueryInstanceData)(node.getUserObject());
		    //    setToolTipText(nodeInfo.tooltip());
		    //    setIcon(getImageIcon(nodeInfo));
            //}
	        //else {
	        //    setToolTipText(null); 
	        //} 
            
            return this;
        }
        
        private ImageIcon getImageIcon(QueryData data)
        {
        	String key = null;
        	if (data.visualAttribute().substring(0,1).equals("F"))
        	{
        		if ((data.visualAttribute().substring(1).equals("A")) ||
        				(data.visualAttribute().substring(1).equals("I")))
        			key = "closedFolder";
        		else if ((data.visualAttribute().substring(1).equals("AO")) ||
        				(data.visualAttribute().substring(1).equals("IO")))	
        			key = "openFolder";
        	}
        	else if (data.visualAttribute().substring(0,1).equals("C"))
        	{
        		if ((data.visualAttribute().substring(1).equals("A")) ||
        				(data.visualAttribute().substring(1).equals("I")))
        			key = "closedCase";
        		else if ((data.visualAttribute().substring(1).equals("AO")) ||
        				(data.visualAttribute().substring(1).equals("IO")))
        			key = "openCase";
        	}
        	else if (data.visualAttribute().substring(0,1).equals("L"))
        	{
        		if(data.name().equalsIgnoreCase("working ......")) {
        			key = "leaf";
        		}
        		else {
        			key = "morepeople";    
        		}
        	}
        	else if (data.visualAttribute().substring(0,1).equals("M"))
        	{
        		key = "leaf";
        	}
        	
        	if(key.equals("multi")) {
        		return createImageIcon(key+".bmp");
        	}
        	else {
        		return createImageIcon(key+".jpg");
        	}
        }   
    }
    
    protected static ImageIcon createImageIcon(String path) {
    	java.net.URL imgURL = QueryTopPanel.class.getResource(path);
    	if (imgURL != null) {
    		return new ImageIcon(imgURL, "");
    	} else {
    			System.err.println("Couldn't find file: " + path);
    			return null;
    	}
    }
    
    private void createPopupMenu() {
        JMenuItem menuItem;
        
        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        
        menuItem = new JMenuItem("Rename ...");
        menuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 
        		java.awt.event.InputEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        popup.add(menuItem);

        /*popup.add(new javax.swing.JSeparator());*/
        
        menuItem = new JMenuItem("Delete");
        menuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 
        		java.awt.event.InputEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
        popup.add(new javax.swing.JSeparator());
        
        menuItem = new JMenuItem("Refresh All");
        menuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 
        		java.awt.event.InputEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        popup.add(menuItem);

        //Add listener to the tree
        MouseListener popupListener = new PreviousRunsTreePopupListener(popup);
        jTree1.addMouseListener(popupListener);
        jTree1.addMouseMotionListener(new PreviousRunsTreeMouseMoveListener());
    }
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("Rename ...")) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getSelectionPath().getLastPathComponent();
			QueryMasterData ndata = (QueryMasterData) node.getUserObject();
			Object inputValue = JOptionPane.showInputDialog(this, 
	    			"Rename this query to: ", "Rename Query Dialog",
	    			JOptionPane.PLAIN_MESSAGE, null,
	    			null, ndata.name().substring(0, ndata.name().lastIndexOf("[")-1));
			
			if(inputValue != null) {
				String newQueryName = (String) inputValue;
				String requestXml = ndata.writeRenameQueryXML(newQueryName);
				lastRequestMessage = requestXml;
				
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				String response = QueryListNamesClient.sendQueryRequestREST(requestXml);
				if(response.equalsIgnoreCase("CellDown")) {
					final JPanel parent = this;
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {					
							JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
			         			"this is often a network error, please try again", 
			         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
						}
					});	
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				lastResponseMessage = response;
				
				if(response != null) {
					JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
			        
			       try {
			    	    JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
			    	    ResponseMessageType messageType = (ResponseMessageType) jaxbElement.getValue();
			    	   StatusType statusType = 
			    		   messageType.getResponseHeader().getResultStatus().getStatus();
			    	   String status = statusType.getType();
			        	
			    	   if(status.equalsIgnoreCase("DONE")) {
				    		ndata.name(newQueryName+" ["+ndata.userId()+"]");
							node.setUserObject(ndata);
							//DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
							
							jTree1.repaint();
			    	   }
			       }
			       catch(Exception ex) {
			    	   ex.printStackTrace();
			       }
				}				
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));				
			}			
		}
		else if(e.getActionCommand().equalsIgnoreCase("Delete")) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getSelectionPath().getLastPathComponent();
			QueryMasterData ndata = (QueryMasterData) node.getUserObject();
			Object selectedValue = JOptionPane.showConfirmDialog(this, 
					"Delete Query \"" + ndata.name()+"\"?","Delete Query Dialog",
					JOptionPane.YES_NO_OPTION);		
			if(selectedValue.equals(JOptionPane.YES_OPTION)) {
				System.out.println("delete "+ndata.name());
				String requestXml = ndata.writeDeleteQueryXML();
				lastRequestMessage = requestXml;
				//System.out.println(requestXml);
				
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				String response = QueryListNamesClient.sendQueryRequestREST(requestXml);
				if(response.equalsIgnoreCase("CellDown")) {
					final JPanel parent = this;
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {					
							JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
			         			"this is often a network error, please try again", 
			         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
						}
					});		
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				lastResponseMessage = response;
				
				if(response != null) {
			        JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
			        
			       try {
			    	   JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
			    	   ResponseMessageType messageType =  (ResponseMessageType)jaxbElement.getValue();
			    	   StatusType statusType = 
			    		   messageType.getResponseHeader().getResultStatus().getStatus();
			    	   String status = statusType.getType();
			        	
			    	   if(status.equalsIgnoreCase("DONE")) {
				    		treeModel.removeNodeFromParent(node);
							
							//jTree1.repaint();
			    	   }
			       }
			       catch(Exception ex) {
			    	   ex.printStackTrace();
			       }
				}				
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));	
			}
		}
		else if(e.getActionCommand().equalsIgnoreCase("Refresh All")) {
			String status = loadPreviousQueries(false);
			if(status.equalsIgnoreCase("")) {
				reset(200, false);
			}
			else if(status.equalsIgnoreCase("CellDown")){
				final JPanel parent = this;
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {					
						JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
		         			"this is often a network error, please try again", 
		         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
					}
				});		
			}
		}
	}
	
	class PreviousRunsTreeMouseMoveListener extends MouseMotionAdapter {

		@Override
		public void mouseDragged(MouseEvent e) {			
			super.mouseDragged(e);
			
			JComponent c = (JComponent)e.getSource(); 
            TransferHandler th = c.getTransferHandler(); 
            th.exportAsDrag(c, e, TransferHandler.COPY); 
		}
	}
	
	class PreviousRunsTreePopupListener extends MouseAdapter {
		
		JPopupMenu popup;

		PreviousRunsTreePopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}
		
	    @Override
		public void mouseClicked(MouseEvent e) {
	    	
	    	if(!(e.isMetaDown() || e.isPopupTrigger()) && (jTree1.getSelectionPath()!= null)
	    			&& e.getClickCount() == 1 && e.getX() > 15) {  
	    		
	    		TreePath path = jTree1.getPathForLocation(e.getX(), e.getY());
	    		DefaultMutableTreeNode clickednode = null;
	    		if(path!=null) {
	    			clickednode = (DefaultMutableTreeNode)path.getLastPathComponent();
	    		}
	    		
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getSelectionPath().getLastPathComponent();
            	if(node != null && node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryMasterData")
            			&& clickednode != null && clickednode.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryMasterData")) {
            		
            		setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    			StringWriter strWriter = new StringWriter();
					try {
						JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
						
						QueryMasterData ndata = (QueryMasterData) node.getUserObject();
		    			//if(ndata.xmlContent() == null) { 
		    			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		    			QueryMasterType queryMasterType = new QueryMasterType();
		    			queryMasterType.setName(ndata.name());
		    			queryMasterType.setQueryMasterId(new Integer(ndata.id()).intValue());
		    			queryMasterType.setUserId(ndata.userId());
		    			queryMasterType.setGroupId("Asthma");
						//strWriter = new StringWriter();
						DndType dnd = new DndType();
						edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory();
						dnd.getAny().add(psmOf.createQueryMaster(queryMasterType));
						edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.dnd.ObjectFactory();
						PreviousQueryJAXBUtil.getJAXBUtil().marshaller(of.createPluginDragDrop(dnd), strWriter);				
					} catch (JAXBUtilException e1) {
						//log.error("Error marshalling Ont drag text");
				//		throw e;
						e1.printStackTrace();
					} 
	    				/*String xmlcontent = null;
	    				String xmlrequest = null;
	    				
	    				xmlrequest = ndata.writeDefinitionQueryXML();
	    				lastRequestMessage = xmlrequest;
	    				
	    				xmlcontent = QueryListNamesClient.sendQueryRequest(xmlrequest);
	    				lastResponseMessage = xmlcontent;
	    				
	    				if(xmlcontent == null) {
	    					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    					return;
	    				}
	    				else {
	    					System.out.println("Query content response: "+xmlcontent);
	    					ndata.xmlContent(xmlcontent);
	    				}*/
	    				
	    			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    			//}
	    			
	    			//if(parent != null) {
	    			//	parent.queryPanel().getTopPanel().reset();
		    		//	parent.queryPanel().dataModel().redrawPanelFromXml(ndata.xmlContent());
	    			//}
	    			
	    			IWorkbenchPage page = parentView.getViewSite().getPage();
	    	    	ViewPart queryview = (ViewPart) page.findView("edu.harvard.i2b2.eclipse.plugins.query.views.QueryView");
	    	    	//if(queryview == null) {
	    	    	//	try {
	    	    	//		queryview = (ViewPart) page.showView("edu.harvard.i2b2.eclipse.plugins.query.views.QueryView");
	    	    	//	}
	    	    	//	catch(Exception ex) {
	    	    	//		ex.printStackTrace();
	    	    	//	}
	    	    	//}
	    	    	System.out.println("Sending Node xml to: "+queryview.getTitle()+"\n"+strWriter.toString());
	    	    	//System.out.println("First view title: "+queryview.getTitle());
	    	    	//queryview.setInitializationData(null, null, ndata.xmlContent());
	    	    	
	    	    	((ICommonMethod) queryview).doSomething(strWriter.toString());//ndata.xmlContent());
            	}
	    	}
     
	    	maybeShowPopup(e);
		}

		public void mousePressed(MouseEvent e) {
	    	//JComponent c = (JComponent)e.getSource(); 
            //TransferHandler th = c.getTransferHandler(); 
            //th.exportAsDrag(c, e, TransferHandler.COPY); 		
	    }

	    public void mouseReleased(MouseEvent e) {
	    	maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	    	if (e.isPopupTrigger()) {
	    		TreePath path = jTree1.getPathForLocation(e.getX(), e.getY());
	    		if(path!=null) {
	    			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
	    			//DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
	    			if(!node.isLeaf()) {
	    				popup.show(e.getComponent(), e.getX(), e.getY());
	    				jTree1.setSelectionPath(path);
	    			}
	    		}
	    	}
	    }
	}
	
	public void treeCollapsed(TreeExpansionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		QueryData data = (QueryData) node.getUserObject();

		System.out.println("Node collapsed: "+data.name());	
		
		if (data.visualAttribute().equals("FAO")){
			data.visualAttribute("FA");
		}
		else if (data.visualAttribute().equals("CAO")) {
			data.visualAttribute("CA");
		}		
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		QueryData data = (QueryData) node.getUserObject();
		jTree1.scrollPathToVisible(new TreePath(node));
		
		System.out.println("Node expanded: "+data.name());	
		
		if (data.visualAttribute().equals("FA")) {
			data.visualAttribute("FAO");
		}
		else if (data.visualAttribute().equals("CA")) {
			data.visualAttribute("CAO");
		}
	
		// check to see if child is a placeholder ('working...')
		//   if so, make Web Service call to update children of node
		if (node.getChildCount() == 1) {	
			final DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) node.getChildAt(0);
			if(((QueryData)node1.getUserObject()).visualAttribute().equals("LAO") && 
					((QueryData)node1.getUserObject()).name().equals("working ......"))
			{		
				final DefaultMutableTreeNode anode = node;
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {	      			
						populateChildNodes(anode);
					}
		      	});	  
			}
		}
		else {
			for(int i=0; i<node.getChildCount(); i++) {
				DefaultMutableTreeNode anode = (DefaultMutableTreeNode) node.getChildAt(0);
				QueryData adata = (QueryData) anode.getUserObject();
				if(adata.visualAttribute().equals("FAO"))
				{
					adata.visualAttribute("FA");
				}
				else if (adata.visualAttribute().equals("CAO")) {
					adata.visualAttribute("CA");	
				}
			}
		}		
	}
	
	private String addZero(int number) {
		String result = new Integer(number).toString();
		if(number < 10 && number >=0) {
			result = "0"+result;
		}
		return result;
	}
	
	private void populateChildNodes(DefaultMutableTreeNode node) {
		if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryMasterData")) {	
			QueryMasterData data = (QueryMasterData) node.getUserObject();
			try {
				String xmlRequest = data.writeContentQueryXML();
				lastRequestMessage = xmlRequest;
				//System.out.println(xmlRequest);
				String xmlResponse = QueryListNamesClient.sendQueryRequestREST(xmlRequest);
				if(xmlResponse.equalsIgnoreCase("CellDown")) {
					final JPanel parent = this;
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {					
							JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
			         			"this is often a network error, please try again", 
			         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
						}
					});	
					return;
				}
				lastResponseMessage = xmlResponse;
				
				try {
					JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
					JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlResponse);
					ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
					
					BodyType bt = messageType.getMessageBody();
					InstanceResponseType instanceResponseType = (InstanceResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(), InstanceResponseType.class);
					
					for(QueryInstanceType queryInstanceType:instanceResponseType.getQueryInstance()) {
						//change later for working with new xml schema
						//RunQuery runQuery = queryInstanceType.getResult().get(i).getRunQuery().get(0);
						
						QueryInstanceData runData = new QueryInstanceData();
						
						runData.visualAttribute("FA");
						runData.tooltip("The results of the query run");
						runData.id(new Integer(queryInstanceType.getQueryInstanceId()).toString());
						//runData.patientRefId(new Integer(queryInstanceType.getRefId()).toString());
						//runData.patientCount(new Long(queryInstanceType.getCount()).toString());
						XMLGregorianCalendar cldr = queryInstanceType.getStartDate();
						runData.name("Results of "+ "["+addZero(cldr.getMonth())+"-"+addZero(cldr.getDay())+"-"
							+addZero(cldr.getYear())+" "+addZero(cldr.getHour())+":"
							+addZero(cldr.getMinute())
							+":"+addZero(cldr.getSecond())+"]");
						
						data.runs.add(runData);
						addNode(runData, node);			
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				
		    	jTree1.scrollPathToVisible(new TreePath(node.getPath()));
	    	}
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
		}
		else if(node.getUserObject().getClass().getSimpleName().equalsIgnoreCase("QueryInstanceData")) {
			QueryInstanceData data = (QueryInstanceData) node.getUserObject();
			
			try {
				String xmlRequest = data.writeContentQueryXML();
				lastRequestMessage = xmlRequest;
				//System.out.println(xmlRequest);
				
				String xmlResponse = QueryListNamesClient.sendQueryRequestREST(xmlRequest);
				if(xmlResponse.equalsIgnoreCase("CellDown")) {
					final JPanel parent = this;
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {					
							JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
			         			"this is often a network error, please try again", 
			         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
						}
					});	
					return;
				}
				lastResponseMessage = xmlResponse;
				
				JAXBUtil jaxbUtil = PreviousQueryJAXBUtil.getJAXBUtil();
				
				JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlResponse);
				ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
				BodyType bt = messageType.getMessageBody();
				ResultResponseType resultResponseType = (ResultResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(),ResultResponseType.class);
					
				for(QueryResultInstanceType queryResultInstanceType:resultResponseType.getQueryResultInstance()) {
					String status = queryResultInstanceType.getQueryStatusType().getName();
					
					QueryResultData resultData = new QueryResultData();
					resultData.visualAttribute("LAO");
					resultData.tooltip("A patient set of the query run");
					//resultData.queryId(data.queryId());
					resultData.patientRefId(new Integer(queryResultInstanceType.getResultInstanceId()).toString());//data.patientRefId());
					resultData.patientCount(new Integer(queryResultInstanceType.getSetSize()).toString());//data.patientCount());
					if(status.equalsIgnoreCase("FINISHED")) {
						resultData.name("Patient Set - "+resultData.patientCount()+" Patients");
					}
					else {
						resultData.name("Patient Set - "+status);
					}
					resultData.xmlContent(xmlResponse);
							
					addNode(resultData, node);		
				}
					
				jTree1.scrollPathToVisible(new TreePath(node.getPath()));
			}	
			catch(Exception e) {
				e.printStackTrace();
			}
		}
				
		//implement for other type of nodes later!!!
	}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
