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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.graphics.*;

import edu.harvard.i2b2.eclipse.plugins.ontology.util.StringUtil;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.TreeData;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.TreeNode;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntologyResponseMessage;
import edu.harvard.i2b2.ontclient.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontclient.datavo.vdo.MatchStrType;
import edu.harvard.i2b2.ontclient.datavo.vdo.VocabRequestType;

public class NodeBrowser extends ApplicationWindow
{
  private Log log = LogFactory.getLog(NodeBrowser.class.getName());
  public boolean stopRunning = false;
  private Button findButton;
  private TreeViewer viewer;
  private int result;
  private TreeData currentData;
  public TreeNode rootNode;       
  private ImageRegistry imageRegistry;
  private StatusLineManager slm;
  private NodeBrowser browser;
  
  public NodeBrowser(Composite parent, int inputFlag, Button button, StatusLineManager slm)
  {
    super(null);
    this.slm = slm;
    this.browser = this;
    findButton = button;
    imageRegistry= new ImageRegistry();
    createImageRegistry();
    
    createTreeViewer(parent, SWT.MULTI | SWT.BORDER, inputFlag);
    Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
     
   	this.viewer.addDragSupport(DND.DROP_COPY|DND.DROP_MOVE, types, new NodeDragListener(this.viewer));      
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
// imageDescriptor = ImageDescriptor.createFromFile(getClass(), "icons/xyz.jpg");
// this.imageRegistry.put("error", imageDescriptor);
  }
  
  private void createTreeViewer(Composite parent, int style, int inputFlag)
  {
	Tree tree = new Tree(parent, style);  

  //  GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
	GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.verticalSpan = 25;
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
        	
        	// if element is inactive; display label in gray
        	if( ((TreeNode)element).getData().getVisualattributes().substring(1,2).equals("I") )
        	{
        		Color gray = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        		item.setForeground(gray);
        	}
//       	 if element is Hidden; print label in red
        	else if (((TreeNode)element).getData().getVisualattributes().substring(1,2).equals("H"))
        	{
        		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        		item.setForeground(color);
        	}
        	
//      	 if element is error; print label in red
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
   this.viewer.setInput(getRootNode());
   
   String status = System.getProperty("errorMessage");
   if (status != null){
	   TreeNode placeholder = new TreeNode(1, "placeholder", status, "C-ERROR");
	   browser.rootNode.addChild(placeholder);
	   browser.refresh();
	   System.setProperty("errorMessage", "");
   }

    this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
    	   public void selectionChanged(SelectionChangedEvent event) {
    	       // if the selection is empty clear the label
    	       if(event.getSelection().isEmpty()) {
    	           return;
    	       }
    	       if(event.getSelection() instanceof IStructuredSelection) {
    	           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
    	           TreeNode node = ((TreeNode) selection.getFirstElement());
    	           setCurrentNode(node);
    	       }    	
    	       Clipboard clipboard = new Clipboard(Display.getCurrent());
    	       TextTransfer transfer = TextTransfer.getInstance();
    	       
    	       String query = "This is a categorical item that cannot be queried";
    	       if (!(getSelectedNode().getVisualattributes().substring(0,1).equals("C")))
    	       {
    	    	   String dimCode = getSelectedNode().getDimcode();
    	    	   if(getSelectedNode().getOperator().equals("LIKE"))
    	    	   {
    	    		   dimCode = "'" + dimCode + "\\%'";
    	    	   }
    	    	   else if(getSelectedNode().getOperator().equals("IN"))
    	    	   {
    	    		   dimCode = "(" + dimCode + ")";
    	    	   }
    	    	   else if(getSelectedNode().getOperator().equals("="))
    	    	   {
    	    		   if(getSelectedNode().getColumndatatype().equals("T"))
    	    		   {
    	    			   dimCode = "'" + dimCode + "'";
    	    		   }
    	    	   }
    	    	   query = "select " + getSelectedNode().getFacttablecolumn() + " from " + getSelectedNode().getTablename() + 
				   " where " + getSelectedNode().getColumnname() + " " + getSelectedNode().getOperator()
				   + " " + dimCode;    	           
    	       }
    	       clipboard.setContents(
    	    		   new String[] {query},
    	    		   new Transfer[] {transfer}); 
    	       clipboard.dispose();
    	   }
    });
    
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
			//viewer.refresh(node);
			//viewer.expandToLevel(node, 1);

			// check to see if child is a placeholder ('working...')
			//   if so, make Web Service call to update children of node

			if (node.getChildren().size() == 1 && 
			!(((TreeNode)node.getChildren().get(0)).getData().getName().
					equalsIgnoreCase("Over maximum number of child nodes"))) 	
			{	
				TreeNode child = (TreeNode)(node.getChildren().get(0));
				if((child.getData().getVisualattributes().equals("LAO")) || (child.getData().getVisualattributes().equals("LHO")))
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
				//viewer.refresh(node);
			}
			viewer.refresh();
			viewer.expandToLevel(node, 1);
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
 					if((child.getData().getVisualattributes().equals("LAO")) || (child.getData().getVisualattributes().equals("LHO")))
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
	
	
	Listener viewerListener = new Listener() {
		Shell tip = null;
		Label label = null;

		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Dispose:
				case SWT.KeyDown:
				case SWT.MouseExit:
				case SWT.MouseMove: {
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
		      //      label.setData("_TREEITEM", item);
		            label.setText((String)item.getData("TOOLTIP"));
//		            label.setText("Tooltip test");
		           // label.addListener(SWT.MouseExit, labelListener);
		           // label.addListener(SWT.MouseDown, labelListener);
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
    
  public TreeData getSelectedNode()
  {
	  return this.currentData;
  }
  
  public void refresh()
  {		  
	  findButton.setText("Find");	  
	  if(this.stopRunning == false)
	  {
		  this.slm.setMessage(System.getProperty("statusMessage"));
		  this.slm.update(true);
		  this.viewer.refresh(this.rootNode);
	  }
	  else
	  {  
		  this.stopRunning = false;
		  this.flush();
		  this.viewer.refresh(this.rootNode);
		  
	  }
  }
  
  public void flush()
  {
	  this.rootNode.getChildren().clear();
  }
  

  
  private TreeNode getRootNode()
    {	  
      TreeNode root = new TreeNode(0,"Find Items",
									"Find Items", "CA");
	  this.rootNode = root;
      return root;
    }
  
	public Thread getFindData(final String categoryKey, final List categories, String phrase, String match) {
		final Display theDisplay = Display.getCurrent();
		final NodeBrowser theBrowser = this;
		final TreeViewer theViewer = this.viewer;
		final String lookupPhrase = phrase;
		final String lookupOperator = match;
		final List lookupCategories = categories;
		return new Thread() {
			public void run() {						
				theBrowser.rootNode.getChildren().clear();
				theBrowser.findNodes(categoryKey, lookupCategories, lookupPhrase, lookupOperator, theDisplay, theViewer);
							
				theDisplay.syncExec(new Runnable() {
					public void run() {
				 		if (theBrowser.rootNode.getChildren().size() == 0)
						{	
				 			TreeNode placeholder = new TreeNode(1, "placeholder", "There were no matches", "C-UNDEF");
							theBrowser.rootNode.addChild(placeholder);
					  	 }	
				 		theBrowser.refresh();
					}
				});
			}
		};
	}
	private void findNodes(String categoryKey, List categories, String phrase, String operator, Display display, TreeViewer viewer) {
		VocabRequestType vocabData = new VocabRequestType();

		MatchStrType match = new MatchStrType();
		match.setValue(phrase);
		if(operator.equals("Containing"))
			operator = "contains";
		else if(operator.equals("Starting with"))
			operator = "left";
		else if(operator.equals("Ending with"))
			operator = "right";	
		else if(operator.equals("Exact"))
			operator = "exact";	
		match.setStrategy(operator);
		vocabData.setMatchStr(match);
		vocabData.setType("core");
		vocabData.setBlob(true);
		vocabData.setMax(Integer.parseInt(System.getProperty("OntFindMax")));

		if(categoryKey.equals("any"))
		{
			if(categories != null ) {
				Iterator categoriesIterator = categories.iterator();		
				while(categoriesIterator.hasNext()&& this.stopRunning == false)
				{
					vocabData.setCategory(StringUtil.getTableCd(((ConceptType)categoriesIterator.next()).getKey()));
					List concepts = getNodes(vocabData, display, viewer);
					getNodesFromXMLString(concepts);
				}
//				System.setProperty("statusMessage", count + " records returned");
			}
		}

		else
		{
			vocabData.setCategory(categoryKey);
			List concepts = getNodes(vocabData, display, viewer);
			getNodesFromXMLString(concepts);
		}		
	}
	
    private void getNodesFromXMLString(List concepts){
    	if(concepts != null){
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
    			this.rootNode.addChild(childNode);
    		}
    	}
    }
	
    private List getNodes(VocabRequestType vocabData, Display theDisplay, final TreeViewer theViewer) {

    	OntologyResponseMessage msg = new OntologyResponseMessage();
    	StatusType procStatus = null;	
    	try {
    		while(procStatus == null || !procStatus.getType().equals("DONE")){
    			String response = OntServiceDriver.getNameInfo(vocabData, "FIND");			
    			procStatus = msg.processResult(response);;
    			if(procStatus.getValue().equals("MAX_EXCEEDED")) {
    				log.info("MAX_EXCEEDED");
    				theDisplay.syncExec(new Runnable() {
    					public void run() {
    						MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), 
    								SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    						mBox.setText("Please Note ...");
    						mBox.setMessage("This query has exceeded maximum number of nodes allowed\n"
    								+ "Populating the query results will be slow\n"
    								+"Do you want to continue?");
    						result = mBox.open();
    					}
    				});
    				if(result == SWT.NO) {
    					procStatus.setType("DONE");
    				}
    				else {
    					vocabData.setMax(null);
    					response = OntServiceDriver.getNameInfo(vocabData, "FIND");
    					procStatus = msg.processResult(response);
    				}
    			}
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
					this.stopRunning = true;
					return null;
				}		
    			else
    				procStatus.setType("DONE");
    		}
    		
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
    		this.stopRunning = true;
    		return null;
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
			this.stopRunning = true;
			return null;
    	}
    	ConceptsType allConcepts = msg.doReadConcepts();   	 
    	if(allConcepts == null)
    		return null;
    	List concepts = allConcepts.getConcept();
    	return concepts;
    }
	
	public Thread getSchemeData(final String schemeKey, List schemes, String phrase, String match) {
		final Display theDisplay = Display.getCurrent();
		final NodeBrowser theBrowser = this;
		final TreeViewer theViewer = this.viewer;
		final String lookupPhrase = phrase;
		final String lookupKey = schemeKey;
		final String lookupStrategy = match;
		final List lookupSchemes = schemes;
		
		return new Thread() {
			public void run() {						
				theBrowser.rootNode.getChildren().clear();			
				theBrowser.findSchemeNodes(lookupKey, lookupSchemes, lookupPhrase, lookupStrategy, theDisplay, theViewer);							
				theDisplay.syncExec(new Runnable() {
					public void run() {

						if (theBrowser.rootNode.getChildren().size() == 0)
						{	
				 			TreeNode placeholder = new TreeNode(1, "placeholder", "There were no matches", "C-UNDEF");
				 			theBrowser.rootNode.addChild(placeholder);							
					  	 }	
				 		theBrowser.refresh();
					}
				});
			}
		};
	}
	private void findSchemeNodes(String key, List schemes, String phrase, String operator, Display display, TreeViewer viewer) {
		VocabRequestType vocabData = new VocabRequestType();

		MatchStrType match = new MatchStrType();
		match.setValue(phrase);
		
		if(operator.equals("Containing"))
			operator = "contains";
		else if(operator.equals("Starting with"))
			operator = "left";
		else if(operator.equals("Ending with"))
			operator = "right";	
		else if(operator.equals("Exact"))
			operator = "exact";	
		match.setStrategy(operator);
		
		vocabData.setType("core");
	//	vocabData.setBlob(false);
		vocabData.setBlob(true);
		vocabData.setMax(Integer.parseInt(System.getProperty("OntFindMax")));
		vocabData.setHiddens(Boolean.parseBoolean(System.getProperty("OntFindHiddens")));
		vocabData.setSynonyms(Boolean.parseBoolean(System.getProperty("OntFindSynonyms")));
		
		if(key.equals("any"))
		{
			if(schemes != null ) {
				Iterator schemesIterator = schemes.iterator();		
				while(schemesIterator.hasNext()&& this.stopRunning == false)
				{
					vocabData.setCategory(null);
					match.setValue(((ConceptType)schemesIterator.next()).getKey()+phrase);
					vocabData.setMatchStr(match);
					List concepts = getSchemeNodes(vocabData, display, viewer);
					if((concepts != null) && (concepts.isEmpty()==false)) {
						getNodesFromXMLString(concepts);
				//		break;   // cycle through all schemes; dont stop at first hit
					}
				}
			}
//			System.setProperty("statusMessage", count + " records returned");
		}

		else
		{
			match.setValue(key+phrase);
			vocabData.setMatchStr(match);
			vocabData.setCategory(null);
			List concepts = getSchemeNodes(vocabData, display, viewer);
			if(concepts != null) {
				getNodesFromXMLString(concepts);
			}
		}		
	}
	
	private List getSchemeNodes(VocabRequestType vocabData, Display theDisplay, final TreeViewer theViewer) {

		OntologyResponseMessage msg = new OntologyResponseMessage();
		StatusType procStatus = null;	
		try {
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = OntServiceDriver.getCodeInfo(vocabData, "FIND");			
				procStatus = msg.processResult(response);
				if(procStatus.getValue().equals("MAX_EXCEEDED")) {
					log.debug("MAX_EXCEEDED");
					theDisplay.syncExec(new Runnable() {
						public void run() {
							MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), 
									SWT.ICON_QUESTION | SWT.YES | SWT.NO);
							mBox.setText("Please Note ...");
							mBox.setMessage("This query has exceeded maximum number of nodes allowed\n"
									+ "Populating the query results will be slow\n"
									+"Do you want to continue?");
							result = mBox.open();
						}
					});
					if(result == SWT.NO) {
						procStatus.setType("DONE");
					}
					else {
						vocabData.setMax(null);
						response = OntServiceDriver.getCodeInfo(vocabData, "FIND");
						procStatus = msg.processResult(response);
					}
				}
				// other error cases  USER INVALID, TABLE ACCESS DENIED, DATABASE ERROR
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
					this.stopRunning = true;
					return null;
				}		
    			else
    				procStatus.setType("DONE");
			}

		} catch (AxisFault e) {
			theDisplay.syncExec(new Runnable() {
				public void run() {
					// e.getMessage() == Incoming message input stream is null  -- for the case of connection down.
					MessageBox mBox = new MessageBox(theViewer.getTree().getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox.setMessage("Unable to make a connection to the remote server\n" +  
					"This is often a network error, please try again");
					int result = mBox.open();
				}
			});
			this.stopRunning = true;
			return null;
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
			this.stopRunning = true;
			return null;
		}
		ConceptsType allConcepts = msg.doReadConcepts();   	    
		List concepts = allConcepts.getConcept();
		return concepts;
	}
	
}
	
  // Old select service base methods
/*	public Thread getFindData(final String lookup, final Document categories, String phrase, String match) {
		final Display theDisplay = Display.getCurrent();
		final NodeBrowser theBrowser = this;
		final String lookupPhrase = phrase;
		final String lookupOperator = match;
		final Document lookupCategories = categories;
		return new Thread() {
			public void run() {		
				String lookupTable = lookup;
				List tables = categories.getRootElement().getChildren("table");
				Iterator tableIterator = tables.iterator();		
				while(tableIterator.hasNext())
				{
					Element table = (org.jdom.Element) tableIterator.next();
					String name = table.getChild("name").getText();
					if (name.equals(lookup))
					{
						lookupTable = table.getChild("tableName").getText();
						break;
					}
//					String tableName = table.getChild("tableName").getText();
//					String status = table.getChild("status").getText();
//					String description = table.getChild("description").getText();
//					String lookupDB = table.getChild("lookupDB").getText();
//					String webserviceName = table.getChild("webserviceName").getText();					
				}
				
				theBrowser.rootNode.getChildren().clear();
				theBrowser.parseFind(lookupTable, lookupCategories, lookupPhrase, lookupOperator);
				
	/**			if(lookupTable.equals("Any Category"))
				{
					theBrowser.getFind("Demographics", lookupName, lookupOperator);			    		
					theBrowser.getFind("Diagnoses", lookupName, lookupOperator);
					theBrowser.getFind("Encounters",lookupName, lookupOperator);		    		
					theBrowser.getFind("LabTests", lookupName, lookupOperator);
					theBrowser.getFind("Medications", lookupName, lookupOperator);			    		
					theBrowser.getFind("Microbiology", lookupName, lookupOperator);
					theBrowser.getFind("Procedures", lookupName, lookupOperator);		    		
					theBrowser.getFind("Providers", lookupName, lookupOperator);
					theBrowser.getFind("Transfusions", lookupName, lookupOperator);		    		
					theBrowser.getFind("I2B2", lookupName, lookupOperator);
				}
				else
				{
					theBrowser.getFind(lookupTable, lookupName, lookupOperator);
				}
	*/			
/*				theDisplay.syncExec(new Runnable() {
					public void run() {
				 		if (theBrowser.rootNode.getChildren().size() == 0)
						{	
				 			TreeNode placeholder = new TreeNode(1, "placeholder", "There were no matches", "C-UNDEF", "UNDEF","UNDEF", "UNDEF");
							theBrowser.rootNode.addChild(placeholder);
					  	 }	
				 		theBrowser.refresh();
					}
				});
			}
		};
	}
  
	private void parseFind(String lookup, Document categories, String phrase, String operator)
	{
		int count = 0;
		if(categories == null)
		{
			slm.setMessage(System.getProperty("statusMessage"));
			slm.update(true);
			return;
		}
		if(lookup.equals("Any Category"))
		{
			List tables = categories.getRootElement().getChildren("table");
			Iterator tableIterator = tables.iterator();		
			while(tableIterator.hasNext())
			{
				Element table = (org.jdom.Element) tableIterator.next();
				String tableName = table.getChild("tableName").getText();
//				String name = table.getChild("name").getText();
//				String status = table.getChild("status").getText();
//				String description = table.getChild("description").getText();
				String lookupDB = table.getChild("lookupDB").getText();
				String webserviceName = table.getChild("webserviceName").getText();
				count = count + getFind(tableName, lookupDB, webserviceName, phrase, operator);
			}
			System.setProperty("statusMessage", count + " records returned");
		}

		else
		{
			List tables = categories.getRootElement().getChildren("table");
			Iterator tableIterator = tables.iterator();		
			while(tableIterator.hasNext())
			{
				Element table = (org.jdom.Element) tableIterator.next();
				String tableName = table.getChild("tableName").getText();
//				String name = table.getChild("name").getText();
//				String status = table.getChild("status").getText();
//				String description = table.getChild("description").getText();
				String lookupDB = table.getChild("lookupDB").getText();
				String webserviceName = table.getChild("webserviceName").getText();
				if(tableName.toUpperCase().equals(lookup.toUpperCase()))
				{
					count = getFind(tableName, lookupDB, webserviceName, phrase, operator);
					System.setProperty("statusMessage", count + " records returned");
				}
			}
		}		
	}
	
  private int getFind(String lookup, String lookupDB, String webserviceName, String phrase, String operator)
  {  
  	// method to send web services message to obtain 
	//  find query result data
      try {
  	    // Make a service
  	    SelectService service = new SelectServiceLocator();
  	    // Use service to get stub that implement SDI
        java.net.URL endpoint = new java.net.URL(webserviceName);
	    Select port = service.getSelect(endpoint);
  	    
  	    // Form the query
  	    org.w3c.dom.Document queryDoc = formFindQuery(lookup, lookupDB, phrase, operator);
  	    
  	    //System.out.println("Calling Web service");
  	    // Make the call
  	    org.w3c.dom.Document queryResultDoc  = port.getDataMartRecords(queryDoc);

  	    if (queryResultDoc == null)
  	    {
    		System.setProperty("statusMessage", "WebService query is faulty");
  	    	return 0;
  	    }
  	    return getNodesFromXML(queryResultDoc, lookup, lookupDB, webserviceName);
  	}catch(Exception e)
  	{    		
  		if(e.getMessage().contains("Not+Found"))
  		{
  			System.setProperty("statusMessage", "WebService " + webserviceName + " not found");
  		}
  		else
  		{
  			System.setProperty("statusMessage",e.getMessage());
  		}
  		return 0;
  	}
  }
  private org.w3c.dom.Document formFindQuery(String lookup, String lookupDB, String name, String operator){
  	org.w3c.dom.Document domDoc = null;
  	try {
  	    org.jdom.Element selectElement = new org.jdom.Element("selectParameters");
  	    org.jdom.Document jqueryDoc = new org.jdom.Document(selectElement);
  	    org.jdom.Element dbElement = new org.jdom.Element("i2b2Mart");
  	    dbElement.setText(lookupDB);	    
  	    selectElement.addContent(dbElement);

  	    org.jdom.Element table = new org.jdom.Element("table");
  	    table.setText(lookup);
  	    table.setAttribute("abbr", "l");
  	    table.setAttribute("numCols", "16");
  	    table.setAttribute("withBlob", "false");
  	    selectElement.addContent(table);
  	    org.jdom.Element where = new org.jdom.Element("where");
  	    String whereClause = "UPPER(l.c_name) ";
  	    if (operator.equals("Containing"))
  	    	whereClause = whereClause + "LIKE '%" + name.toUpperCase() + "%'";
  	    else if (operator.equals("Starting with"))
  	    	whereClause = whereClause + "LIKE '" + name.toUpperCase() + "%'";
  	    else if (operator.equals("Ending with"))
  	    	whereClause = whereClause + "LIKE '%" + name.toUpperCase() + "'";
  	    else if (operator.equals("Exact"))
  	    	whereClause = whereClause + "= '" + name.toUpperCase() + "'";

  	    where.setText(whereClause);
  	    selectElement.addContent(where);

  	    org.jdom.Element orderBy = new org.jdom.Element("orderBy");
  	    orderBy.setText("l.c_name");
  	    selectElement.addContent(orderBy);
 // 	System.out.println((new XMLOutputter()).outputString(jqueryDoc));
  	    org.jdom.output.DOMOutputter convertor = new org.jdom.output.DOMOutputter();
  	    domDoc = convertor.output(jqueryDoc);
  	}catch (Exception e){
  		System.setProperty("statusMessage", e.getMessage());
  	}
  	return domDoc;	
  }

  private int getNodesFromXML(org.w3c.dom.Document resultDoc, String lookupTable,
  								String lookupDB, String webserviceName)
  {
  	try {
  	    org.jdom.input.DOMBuilder builder = new org.jdom.input.DOMBuilder();
  	    org.jdom.Document jresultDoc = builder.build(resultDoc);
  	    org.jdom.Namespace ns = jresultDoc.getRootElement().getNamespace();
//System.out.println((new XMLOutputter()).outputString(jresultDoc));   	
  	    Iterator iterator = jresultDoc.getRootElement().getChildren("patientData", ns).iterator();
  	    while (iterator.hasNext())
  	    {
  	    	org.jdom.Element patientData = (org.jdom.Element) iterator.next();
     	    	org.jdom.Element lookup = (org.jdom.Element) patientData.getChild(lookupTable.toLowerCase(), ns).clone();
     	    	
//     	    	modification of c_metadataxml tag to make it part of the xml document
       	    	org.jdom.Element metaDataXml = (org.jdom.Element) lookup.getChild("c_metadataxml");
       	    	String c_xml = metaDataXml.getText();
       	    	if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
       	    	{
       	    	 SAXBuilder parser = new SAXBuilder();
       			 String xmlContent = c_xml;
       		     java.io.StringReader xmlStringReader = new java.io.StringReader(xmlContent);
       		     org.jdom.Document tableDoc = parser.build(xmlStringReader);
       		     org.jdom.Element rootElement = (org.jdom.Element) tableDoc.getRootElement().clone();
       		     metaDataXml.setText("");
       		     metaDataXml.getChildren().add(rootElement);
       	    	}
       	    	//
       	    	
     	    	XMLOutputter fmt = new XMLOutputter();
  	    	String XMLContents = fmt.outputString(lookup);
  	    		//this.setXMLContents(XMLContents);      	    
  	    	TreeData childData = new TreeData(XMLContents, lookupTable, lookupDB, webserviceName);
  	    	if(!(childData.getVisualattributes().substring(1,2).equals("H")))
  	    	{	
  	    		TreeNode child = new TreeNode(childData);
  	    	// if the child is a folder/directory set it up with a leaf placeholder
  	    		if((childData.getVisualattributes().equals("FA")) || (childData.getVisualattributes().equals("CA")))
  	    		{
  	    			TreeNode placeholder = new TreeNode(childData.getLevel() + 1, "working...", "working...", "LAO", childData.getLookupTable(), "UNDEF", "UNDEF");
  	    			child.addChild(placeholder);
  	    		}
  	    		this.rootNode.addChild(child);
  	    	}
  	    }
	    org.jdom.Element result = (org.jdom.Element) jresultDoc.getRootElement().getChild("result");
		String resultString = result.getChildTextTrim("resultString", ns);
		System.setProperty("statusMessage", resultString);
		int index = resultString.indexOf("records");
		return Integer.parseInt(resultString.substring(0,index-1).trim());
  	}catch (Exception e) {
  		System.setProperty("statusMessage", e.getMessage());
  		return 0;
  	}
  }
  */







