/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.eclipse.plugins.ontology.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.StatusLineManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.graphics.*;

import edu.harvard.i2b2.eclipse.plugins.ontology.views.TreeNode;

public class NodeBrowser extends ApplicationWindow
{
  private Log log = LogFactory.getLog(NodeBrowser.class.getName());	
  private NodeBrowser browser;
  private TreeViewer viewer;
  private TreeData currentData;
  public TreeNode rootNode;       //unfortunately I dont have a way
	                               // to get the rootNode of a tree.....
  private ImageRegistry imageRegistry;
  private StatusLineManager slm;
  
  public NodeBrowser(Composite parent, int inputFlag, StatusLineManager slm)
  {
    super(null);
    this.slm = slm;
    imageRegistry= new ImageRegistry();
    createImageRegistry();
    
    createTreeViewer(parent, SWT.MULTI | SWT.BORDER, inputFlag);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
       
   	this.viewer.addDragSupport(DND.DROP_COPY, types, new NodeDragListener(this.viewer));      
  }

  private void createImageRegistry()
  {
	  ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/leaf.jpg");
	  this.imageRegistry.put("leaf", imageDescriptor);
	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/multi.bmp");
	  this.imageRegistry.put("multi", imageDescriptor);
	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/openFolder.jpg");
	  this.imageRegistry.put("openFolder", imageDescriptor);
	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/openCase.jpg");
	  this.imageRegistry.put("openCase", imageDescriptor);
	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/closedFolder.jpg");
	  this.imageRegistry.put("closedFolder", imageDescriptor);
	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/closedCase.jpg");
	  this.imageRegistry.put("closedCase", imageDescriptor);
//	  imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/xyz.jpg");
//	  this.imageRegistry.put("error", imageDescriptor);
  }
  
  private void createTreeViewer(Composite parent, int style, int inputFlag)
  {
	this.browser = this;
	Tree tree = new Tree(parent, style);  
	
	GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.verticalSpan = 50;
    gridData.horizontalSpan = 2;
    gridData.widthHint = 150;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    tree.setLayoutData(gridData);
    
    this.viewer = new TreeViewer(tree);  
    this.viewer.setLabelProvider(new LabelProvider() {
        public String getText(Object element) 
        {
        	// Set the tooltip data
        	//  (cant be done in the lookup thread)
        	//   maps TreeViewer node to Tree item and sets item.data
        	TreeItem item =  (TreeItem) (viewer.testFindItem((TreeNode) element));
        	String tooltip = ((TreeNode)element).getData().getTooltip();
        	if ((tooltip == null) || (tooltip.equals("")))
        	{
        		tooltip = ((TreeNode)element).toString();		
        	}
        	tooltip = " " + tooltip + " ";
        	item.setData("TOOLTIP", tooltip);        
   
        	// if element is Inactive; print label in gray
        	if (((TreeNode)element).getData().getVisualattributes().substring(1,2).equals("I"))
        	{
        		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        		item.setForeground(color);
        	}
        	
//        	 if element is Hidden; print label in red
        	else if (((TreeNode)element).getData().getVisualattributes().substring(1,2).equals("H"))
        	{
        		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        		item.setForeground(color);
        	}
        	
//       	 if element is undefined; print label in red
        	else if (((TreeNode)element).getData().getVisualattributes().equals("C-ERROR"))
        	{
        		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        		item.setForeground(color);
        	}
        	
//       	 if element is synonym; print label in dark blue
        	if (((TreeNode)element).getData().getSynonymCd() != null) {
        		if (((TreeNode)element).getData().getSynonymCd().equals("Y"))
        		{
        			Color color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
        			item.setForeground(color);
        		}
        	}	
        	return ((TreeNode)element).toString();
        }
        public Image getImage(Object element)
        {
        	return imageRegistry.get(((TreeNode)element).getIconKey());
        }
    });
    this.viewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getChildren(Object parentElement) {
        return ((TreeNode)parentElement).getChildren().toArray();
      }
    
      public Object getParent(Object element) {
        return ((TreeNode)element).getParent();
      }
    
      public boolean hasChildren(Object element) {
        return ((TreeNode)element).getChildren().size() > 0;
      }
    
      public Object[] getElements(Object inputElement) {
        return ((TreeNode)inputElement).getChildren().toArray();
      }
    
      public void dispose() {}
    
      public void inputChanged(Viewer viewer, 
                                Object oldInput, 
                                Object newInput) {}
    });

    this.viewer.setInput(populateRootNode());

    String status = System.getProperty("errorMessage");
    if (status != null){
    	TreeNode placeholder = new TreeNode(1, "placeholder",status, "C-ERROR");
    	browser.rootNode.addChild(placeholder);
    	browser.refresh();
    	System.setProperty("errorMessage", "");
    }
    
	this.viewer.addTreeListener(new ITreeViewerListener() {
		public void treeExpanded(TreeExpansionEvent event) {
			final TreeNode node = (TreeNode) event.getElement();
			if (node.getData().getVisualattributes().equals("FA"))
				node.getData().setVisualattributes("FAO");
			else if (node.getData().getVisualattributes().equals("CA"))
				node.getData().setVisualattributes("CAO");
			else if (node.getData().getVisualattributes().equals("FH"))
				node.getData().setVisualattributes("FHO");
			else if (node.getData().getVisualattributes().equals("CH"))
				node.getData().setVisualattributes("CHO");
			viewer.expandToLevel(node, 1);
			viewer.refresh(node);

			// check to see if child is a placeholder ('working...')
			//   if so, make Web Service call to update children of node

			if (node.getChildren().size() == 1) {	
					TreeNode child = (TreeNode)(node.getChildren().get(0));
					if((child.getData().getVisualattributes().equals("LAO")) || (child.getData().getVisualattributes().equals("LHO")) )
					{
						// child is a placeholder, so remove from list 
						//   update list with real children  
						slm.setMessage("Calling WebService");
						slm.update(true);
						node.getXMLData(viewer, browser).start();				
					}
//				}
			}
			else {
				for(int i=0; i<node.getChildren().size(); i++) {
					TreeNode child = (TreeNode)(node.getChildren().get(i));
					if(child.getData().getVisualattributes().equals("FAO"))
					{
						child.getData().setVisualattributes("FA");
					}
					else if (child.getData().getVisualattributes().equals("CAO")) {
						child.getData().setVisualattributes("CA");	
					}
					else if(child.getData().getVisualattributes().equals("FHO"))
					{
						child.getData().setVisualattributes("FH");
					}
					else if (child.getData().getVisualattributes().equals("CHO")) {
						child.getData().setVisualattributes("CH");	
					}
				}
				viewer.refresh(node);
			}
		}
		public void treeCollapsed(TreeExpansionEvent event) {
			final TreeNode node = (TreeNode) event.getElement();
			if (node.getData().getVisualattributes().equals("FAO"))
				node.getData().setVisualattributes("FA");
			else if (node.getData().getVisualattributes().equals("CAO"))
				node.getData().setVisualattributes("CA");
			else if (node.getData().getVisualattributes().equals("FHO"))
				node.getData().setVisualattributes("FH");
			else if (node.getData().getVisualattributes().equals("CHO"))
				node.getData().setVisualattributes("CH");
			viewer.collapseToLevel(node, 1);
			viewer.refresh(node);
		}
	});
	this.viewer.addDoubleClickListener(new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event)
		{	
			TreeNode node = null;
	   	    // if the selection is empty clear the label
 	       if(event.getSelection().isEmpty()) {
 	           setCurrentNode(null);
 	           return;
 	       }
 	       if(event.getSelection() instanceof IStructuredSelection) {
 	           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 	           node = (TreeNode) selection.getFirstElement();
 	           setCurrentNode(node);
 	       }
			
 	       // Case where we are expanding the node
 	       boolean expand = false;
 	       if((node.getData().getVisualattributes()).equals("FA"))
 	       {
 	    	  node.getData().setVisualattributes("FAO");
 	    	  expand = true;
 	       }
 	       else if ((node.getData().getVisualattributes()).equals("CA"))
 	       {
  	    	  node.getData().setVisualattributes("CAO");
  	    	  expand = true;
  	       }
 	       
			else if(node.getData().getVisualattributes().equals("FH"))
			{
				node.getData().setVisualattributes("FHO");
				expand = true;
			}
			else if (node.getData().getVisualattributes().equals("CH")) {
				node.getData().setVisualattributes("CHO");	
				expand = true;
			}
 	       
 	       if(expand == true)
 	       {
 			  viewer.expandToLevel(node, 1);
 			  viewer.refresh(node);
 			  
 				// check to see if this node's child is a placeholder ('working...')
 				//   if so, make Web Service call to update children of node

 				if (node.getChildren().size() == 1)
 				{	
 					TreeNode child = (TreeNode)(node.getChildren().get(0));
 					if((child.getData().getVisualattributes().equals("LAO"))  || (child.getData().getVisualattributes().equals("LHO")))
 					{
 						// child is a placeholder, so remove from list 
 						//   update list with real children  
 						slm.setMessage("Calling WebService");
 						slm.update(true);
 						node.getXMLData(viewer, browser).start();
 					}
 				}
 				else {
 					for(int i=0; i<node.getChildren().size(); i++) {
 						TreeNode child = (TreeNode)(node.getChildren().get(i));
 						if(child.getData().getVisualattributes().equals("FAO"))
 						{
 							child.getData().setVisualattributes("FA");
 						}
 						else if (child.getData().getVisualattributes().equals("CAO")) {
 							child.getData().setVisualattributes("CA");	
 						}
 						else if(child.getData().getVisualattributes().equals("FHO"))
 						{
 							child.getData().setVisualattributes("FH");
 						}
 						else if (child.getData().getVisualattributes().equals("CHO")) {
 							child.getData().setVisualattributes("CH");	
 						}
 					}
 					viewer.refresh(node);
 				}
 				
 	       }
 	       
 	       // Case where we are collapsing the node
 	       else if (node.getData().getVisualattributes().equals("FAO"))
 	       {
 	    	  node.getData().setVisualattributes("FA");
 	    	  viewer.collapseToLevel(node, 1);
 	    	  viewer.refresh(node);
 	       }
 	       else if (node.getData().getVisualattributes().equals("CAO"))
 	       {
 	    	  node.getData().setVisualattributes("CA");
 	    	  viewer.collapseToLevel(node, 1);
 	    	  viewer.refresh(node);
 	       }
 	       else if (node.getData().getVisualattributes().equals("FHO"))
 	       {
 	    	  node.getData().setVisualattributes("FH");
 	    	  viewer.collapseToLevel(node, 1);
 	    	  viewer.refresh(node);
 	       }
 	       else if (node.getData().getVisualattributes().equals("CHO"))
 	       {
 	    	  node.getData().setVisualattributes("CH");
 	    	  viewer.collapseToLevel(node, 1);
 	    	  viewer.refresh(node);
 	       }
		}
	});
	
//	 Implement a "fake" tooltip
	final Listener labelListener = new Listener () {
		public void handleEvent (Event event) {
			Label label = (Label)event.widget;
			Shell shell = label.getShell();
			switch (event.type) {
				case SWT.MouseDown:
					Event e = new Event ();
					e.item = (TreeItem) label.getData ("_TREEITEM");
					//cdh@20060314 have to fix this for multi select in treeview
					
					// Assuming table is single select, set the selection as if
					// the mouse down event went through to the table
					(viewer.getTree()).setSelection(new TreeItem[] {(TreeItem) e.item}); 
					(viewer.getTree()).notifyListeners(SWT.Selection, e);
					//table.setSelection (new TableItem [] {(TableItem) e.item});
					//table.notifyListeners (SWT.Selection, e);
					// fall through
				case SWT.MouseExit:
					shell.dispose ();
					break;
			}
		}
	};

	Listener viewerListener = new Listener() {
		Shell tip = null;
		Label label = null;

		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Dispose:
				case SWT.KeyDown:
				case SWT.MouseMove: 
				case SWT.MouseExit: {
					if (tip == null)
						break;
					tip.dispose();
					tip = null;
					label = null;
					break;
				}
		        case SWT.MouseHover: {
		        	TreeItem item = (viewer.getTree()).getItem(new Point(event.x, event.y));
		        	if (item != null) {
		            if (tip != null && !tip.isDisposed())
		              tip.dispose();
		            tip = new Shell(Display.getCurrent().getActiveShell(), SWT.ON_TOP | SWT.TOOL);            
		            tip.setLayout(new FillLayout());
		            label = new Label(tip, SWT.NONE);
		            label.setForeground(Display.getCurrent()
		                .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		            label.setBackground(Display.getCurrent()
		                .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		            label.setData("_TREEITEM", item);
		            label.setText((String)item.getData("TOOLTIP"));
//		            label.setText("Tooltip test");
		            label.addListener(SWT.MouseExit, labelListener);
		            label.addListener(SWT.MouseDown, labelListener);
		            Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		            Rectangle rect = item.getBounds(0);
		            Point pt = viewer.getTree().toDisplay(event.x, event.y);
		            tip.setBounds(pt.x + 10, pt.y + 20, size.x, size.y);
		            tip.setVisible(true);
		          }
		        }
		        }
		      }
		    };
		    viewer.getTree().addListener(SWT.Dispose, viewerListener);
		    viewer.getTree().addListener(SWT.KeyDown, viewerListener);
		    viewer.getTree().addListener(SWT.MouseMove, viewerListener);
		    viewer.getTree().addListener(SWT.MouseHover, viewerListener);	
		    viewer.getTree().addListener(SWT.MouseExit, viewerListener);	
  }

  public void setCurrentNode(TreeNode node)
  {
	  this.currentData = node.getData();
  }
  
  public void addNodes(TreeData data)
  {
	  this.currentData = data;
	  TreeNode child = new TreeNode(data);
	  this.viewer.setInput(child);
  }

// used in old select service version (pass XMLContents)
//  public void addNodes(String data)
//  {
//	  TreeData td = new TreeData(data);
//	  TreeNode child = new TreeNode(td);
//	  this.viewer.setInput(child);
//  }
  
  public TreeData getSelectedNode()
  {
	  return this.currentData;
  }
  
  public void refresh()
  {
	  this.viewer.refresh(this.rootNode);
	  this.slm.setMessage(System.getProperty("statusMessage"));
	  this.slm.update(true);
  }
  
  public void flush()
  {
	  this.rootNode.getChildren().clear();
  }
  
  private TreeNode populateRootNode()
  {	  	  
	  TreeNode root = new TreeNode(0,"Standard Query Items",
			  "Standard Query Items", "CA");
  
	  //make call to getCategories to get list of root nodes
		  root.getCategories(this.viewer, this.browser );

	  this.rootNode = root;
	  return root;
  
  }

  
// Old select service version
//  private TreeNode getRootNode(int inputFlag)
//  {	  	  
//	  TreeNode root = new TreeNode(0,"Standard Query Items",
//			  "Standard Query Items", "CA", "UNDEF", "UNDEF", "UNDEF");
//
//	// Read in configuration data from HTTP request of xml file  
//	Document config = getConfig();  
//	if (config == null)
//	{
//		this.rootNode = root;	
//		this.refresh();
//		return root;
//	}
//	  
//	if (inputFlag == 1)
//	{
//		List tables = config.getRootElement().getChildren("table");
//		Iterator tableIterator = tables.iterator();
//		
//		while(tableIterator.hasNext())
//		{
//			Element table = (org.jdom.Element) tableIterator.next();
//			String name = table.getChild("name").getText();
//			String tableName = table.getChild("tableName").getText();
//			String status = table.getChild("status").getText();
//			String description = table.getChild("description").getText();
//			String lookupDB = table.getChild("lookupDB").getText();
//			String webserviceName = table.getChild("webserviceName").getText();
//			
//			if(System.getProperty("selectservice") == null && webserviceName != null) {
//				System.setProperty("selectservice", webserviceName);
//			}
//			
//			root.addChild(new TreeNode(0, name, name, "CA", tableName, lookupDB, webserviceName)
//				 .addChild(new TreeNode(1, "working...", "working...", "LAO", tableName, lookupDB, webserviceName)));			
//		}		
//	}
	
	  // original hardcoded list of tree elements
/**	  if (inputFlag == 1)
	  {
		  root.addChild(new TreeNode(0,"Demographics", "Demographics", "CA", "DEMOGRAPHICS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "DEMOGRAPHICS")));   
			  	
		  root.addChild(new TreeNode(0,"Diagnoses", "Diagnoses", "CA", "DIAGNOSES")
				  .addChild(new TreeNode(1, "working....",
	            								"working....", "LAO", "DIAGNOSES")));    
			  
		  root.addChild(new TreeNode(0,"Encounters", "Encounters", "CA", "ENCOUNTERS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "ENCOUNTERS")));    
		  
		  
		  root.addChild(new TreeNode(0,"Laboratory Tests", "Laboratory Tests", "CA", "LABTESTS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO","LABTESTS")));   
			  
		  root.addChild(new TreeNode(0,"Medications", "Medications", "CA", "MEDICATIONS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "MEDICATIONS")));    
		  
		  root.addChild(new TreeNode(0,"Microbiology", "Microbiology", "CA", "MICROBIOLOGY")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "MICROBIOLOGY")));   
			  
		  root.addChild(new TreeNode(0,"Procedures", "Procedures", "CA", "PROCEDURES")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "PROCEDURES")));    
			  		  
		  root.addChild(new TreeNode(0,"Providers", "Providers", "CA", "PROVIDERS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "PROVIDERS")));
			  
		  root.addChild(new TreeNode(0,"Transfusion Services", "Transfusion Services", "CA", "TRANSFUSIONS")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "TRANSFUSIONS")));   
			  
		  root.addChild(new TreeNode(0,"i2b2", "i2b2", "CA", "i2b2")
				  .addChild(new TreeNode(1, "working....",
						  "working....", "LAO", "i2b2"))); 
		  
	  }

	  this.rootNode = root;
      return root;
  }
  **/
  
  // lcp Old select service version
  
  // snm - Acquires configuration data via properties variable
  // Returns as JDOM document
//  private Document getConfig(){
//		String responseBody = "";
//		
//		try{
//			responseBody = System.getProperty("ExplorerConfigurationXML");
//		}catch(Exception e){
//	//		e.printStackTrace();
//	//		System.out.println(e.getMessage());
//			System.setProperty("statusMessage", e.getMessage());
//		}finally{
//
//		}
//	//	System.out.println(responseBody);
//		Document responseDoc = null;
//		try {
//			SAXBuilder parser = new SAXBuilder();
//			responseDoc = parser.build(new java.io.StringReader(responseBody));
//		} catch (JDOMException e) {
//		//	System.out.println(e.getMessage());
//			System.setProperty("statusMessage", e.getMessage());
//			//e.printStackTrace();
//		} catch (IOException e) {
//		//	System.out.println(e.getMessage());
//			System.setProperty("statusMessage", e.getMessage());
//			//e.printStackTrace();
//		}		
//		return(responseDoc);
//	}
  // snm - old routine Acquires configuration data via Http call
  // Returns as JDOM document
/* private Document getConfigEx(){
		String responseBody = "";
		HttpClient client = new HttpClient();
//		client.getHttpConnectionManager().getParams().setConnectionTimeout 
//(30000);
		//GetMethod get = new GetMethod("http://phsi2b2appdev.mgh.harvard.edu/queryToolConfig/contents.xml");
		GetMethod get = new GetMethod("http://phsi2b2appprod1.mgh.harvard.edu/queryToolConfig/contents.xml");
//		get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new  
//									DefaultHttpMethodRetryHandler(3, false));
		
		try{
			int resultCode = client.executeMethod(get);
			responseBody = get.getResponseBodyAsString();
		}catch(Exception e){
	//		e.printStackTrace();
	//		System.out.println(e.getMessage());
			System.setProperty("statusMessage", e.getMessage());
		}finally{
			get.releaseConnection();
		}
	//	System.out.println(responseBody);
		Document responseDoc = null;
		if(responseBody.contains("Not Found"))
		{
			System.setProperty("statusMessage", "Query tool config file contents.xml cannot be found");
			return responseDoc;
		}
		try {
			SAXBuilder parser = new SAXBuilder();
			responseDoc = parser.build(new java.io.StringReader(responseBody));
		} catch (JDOMException e) {
		//	System.out.println(e.getMessage());
			System.setProperty("statusMessage", e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
		//	System.out.println(e.getMessage());
			System.setProperty("statusMessage", e.getMessage());
			//e.printStackTrace();
		}		
		return(responseDoc);
	}*/
} 







