/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 * 		Mike Mendis
 */
package edu.harvard.i2b2.eclipse.plugins.ontology.views.find;

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.*;

import edu.harvard.i2b2.eclipse.plugins.ontology.util.StringUtil;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.TreeNode;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntologyResponseMessage;
import edu.harvard.i2b2.ontclient.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetReturnType;


public class FindTool extends ApplicationWindow 
{
	private Log log = LogFactory.getLog(FindTool.class.getName());
	private NodeBrowser browser;
	private String findText = null;
	private String categoryKey;
	private String match;
	private Button findButton;
	private List categories;
	private StatusLineManager slm;
	public static final String OS = System.getProperty("os.name").toLowerCase();

	
	public FindTool(StatusLineManager slm)
	{
		super(null);
		this.slm = slm;
	}
	
	public Control getFindTabControl(TabFolder tabFolder)
	{		
		// Find Composite
		Composite compositeFind = new Composite(tabFolder, SWT.NULL);
		GridLayout gridLayout = new GridLayout(2, false);
		compositeFind.setLayout(gridLayout);
		
		//		 Set up the match combo box
	    final Combo matchCombo = new Combo(compositeFind,SWT.READ_ONLY);

	    matchCombo.add("Starting with");
	    matchCombo.add("Ending with");
	    matchCombo.add("Containing");
	    matchCombo.add("Exact");
	    
	    // set default category
	    matchCombo.setText("Containing");
	    match = "Containing";
	    
	    matchCombo.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent e) {
	    		// Item in list has been selected
	    		match = matchCombo.getItem(matchCombo.getSelectionIndex());
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		// this is not an option (text cant be entered)
	    	}
	    });
	    
		   // First set up the Find text combo box    
	    final Combo findCombo = new Combo(compositeFind, SWT.DROP_DOWN);
		GridData findComboData = new GridData (GridData.FILL_HORIZONTAL);
		findComboData.widthHint = 200;
		findComboData.horizontalSpan = 1;
		findCombo.setLayoutData(findComboData);
	    findCombo.addModifyListener(new ModifyListener() {
	    	public void modifyText(ModifyEvent e) {	    
	    		// Text Item has been entered
	    		// Does not require 'return' to be entered
	    		findText = findCombo.getText();
	    	}
	    });
	    
	    findCombo.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent e) {
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		findText = findCombo.getText();
	    		if(findCombo.indexOf(findText) < 0) {
	    			findCombo.add(findText);
	    		}
	    		if(findButton.getText().equals("Find"))
	    		{
	    			slm.setMessage("Performing search");
	    			slm.update(true);
	    			browser.flush();
	    			System.setProperty("statusMessage", "Calling WebService");
	    			
		 			TreeNode placeholder = new TreeNode(1, "placeholder", "working...", "C-UNDEF");
		 			browser.rootNode.addChild(placeholder);
					browser.refresh();

					browser.getFindData(categoryKey,categories, findText, match).start();
	    			findButton.setText("Cancel");

	    		}
	    		else
	    		{
	    			System.setProperty("statusMessage", "Canceling WebService call");
	    			browser.refresh();
	    			browser.stopRunning = true;
	    			findButton.setText("Find");
	    		}
	    	}
	    });
	    
	    // Next include 'Find' Button
	    findButton = new Button(compositeFind, SWT.PUSH);
	    findButton.setText("Find");
		GridData findButtonData = new GridData ();
		if (OS.startsWith("mac"))	
			findButtonData.widthHint = 80;
		else
			findButtonData.widthHint = 60;
	    findButton.setLayoutData(findButtonData);
	    findButton.addMouseListener(new MouseAdapter() {
	    	public void mouseDown(MouseEvent e) {
	    		// Add item to findCombo drop down list if not already there
	    		if(findText == null)
	    		{
	    			return;
	    		}
	    		if(findCombo.indexOf(findText) < 0) {
	    			findCombo.add(findText);
	    		}
	    		if(findButton.getText().equals("Find"))
	    		{	    			
	    			browser.flush();
	    			System.setProperty("statusMessage", "Calling WebService");
		 			TreeNode placeholder = new TreeNode(1, "placeholder", "working...", "C-UNDEF");
					browser.rootNode.addChild(placeholder);
					browser.refresh();
					
	    			browser.getFindData(categoryKey,categories,findText, match).start();
	    			findButton.setText("Cancel");
	    		}
	    		else
	    		{
	    			System.setProperty("statusMessage", "Canceling WebService call");
	    			browser.refresh();
	    			browser.stopRunning = true;
	    			findButton.setText("Find");
	    		}
	    	}
	    });	    
	    // Next set up the category combo box
	    final Combo categoryCombo = new Combo(compositeFind,SWT.READ_ONLY);
	    setCategories(categoryCombo);    
	    
	    categoryCombo.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent e) {
	    		// Item in list has been selected
	    		if (categoryCombo.getSelectionIndex() == 0)
	    			categoryKey = "any";
	    		else{
	    			ConceptType concept = (ConceptType)categories.get(categoryCombo.getSelectionIndex()-1);
	    			categoryKey = StringUtil.getInstance().getTableCd(concept.getKey());
	    		}
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		// this is not an option (text cant be entered)
	    	}
	    });
	    
	    browser = new NodeBrowser(compositeFind, 1, findButton, slm);
	    return compositeFind;
	}
	  

	private void setCategories(Combo categoryCombo)
	{
		// set default category for combo box
		categoryCombo.add("Any Category");
	    categoryCombo.setText("Any Category");
	    categoryKey = "any";
		
		categories = getCategories();
		
		if(categories != null) {
			Iterator categoriesIterator = categories.iterator();		
			while(categoriesIterator.hasNext())
			{
				ConceptType category = (ConceptType) categoriesIterator.next();
				String name = category.getName();
				categoryCombo.add(name);
			}
		}
		return;
	}
	
    public List getCategories() 
    {
    	List concepts = null;
    	try {
			GetReturnType request = new GetReturnType();
			request.setType("core");
			
    	    OntologyResponseMessage msg = new OntologyResponseMessage();
			StatusType procStatus = null;	
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = OntServiceDriver.getCategories(request, "FIND");			
				procStatus = msg.processResult(response);
//				if  other error codes
//				TABLE_ACCESS_DENIED and USER_INVALID, DATABASE ERROR
				if (procStatus.getType().equals("ERROR")){
		    		System.setProperty("errorMessage", procStatus.getValue());
					return concepts;
				}
				procStatus.setType("DONE");
			}
			ConceptsType allConcepts = msg.doReadConcepts();   
			if(allConcepts != null)
				concepts = allConcepts.getConcept();
			
    	} catch (AxisFault e) {
    		log.error(e.getMessage());
    		System.setProperty("errorMessage", "Ontology cell unavailable");
    	} catch (Exception e) {
    		log.error(e.getMessage());
    		System.setProperty("errorMessage", "Remote server unavailable");
    	}

		return concepts;
    }	   
	
}