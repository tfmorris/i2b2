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
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;

import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.ontology.ws.OntologyResponseMessage;
import edu.harvard.i2b2.ontclient.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontclient.datavo.vdo.GetReturnType;

public class SchemeTool extends ApplicationWindow 
{
	private Log log = LogFactory.getLog(SchemeTool.class.getName());
	private NodeBrowser browser;
	private String findText = null;
	private String schemesKey;
	private String match;
	private Button findButton;
	private List schemes;
	private StatusLineManager slm;
	public static final String OS = System.getProperty("os.name").toLowerCase();

	public SchemeTool(StatusLineManager slm)
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
		
		Composite compositeFindTop = new Composite(compositeFind, SWT.NULL);
		GridLayout gridLayoutTop = new GridLayout(1, false);

		compositeFindTop.setLayout(gridLayoutTop);
		GridData findGridData = new GridData (GridData.FILL_BOTH);
		findGridData.widthHint = 300;
		compositeFindTop.setLayoutData(findGridData);
	    
		   // First set up the Find text combo box    
	    final Combo findCombo = new Combo(compositeFindTop, SWT.DROP_DOWN);
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
;
		 			TreeNode placeholder = new TreeNode(1, "placeholder", "working...", "C-UNDEF");
		 			browser.rootNode.addChild(placeholder);
					browser.refresh();

					browser.getSchemeData(schemesKey, schemes, findText).start();
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
	    
		Composite compositeFindRow2 = new Composite(compositeFindTop, SWT.NULL);
		GridLayout gridLayoutRow2 = new GridLayout(2, false);

		compositeFindRow2.setLayout(gridLayoutRow2);
		GridData findGridDataRow2 = new GridData (GridData.FILL_BOTH);
		findGridData.widthHint = 300;
		compositeFindTop.setLayoutData(findGridDataRow2);
	    
	    // Next set up the category combo box
	    final Combo schemesCombo = new Combo(compositeFindRow2,SWT.READ_ONLY);
	    setSchemes(schemesCombo);    
	    
	    schemesCombo.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent e) {
	    		// Item in list has been selected
	    		if (schemesCombo.getSelectionIndex() == 0)
	    			schemesKey = "any";
	    		else{
	    			ConceptType concept = (ConceptType)schemes.get(schemesCombo.getSelectionIndex()-1);
	    			schemesKey = concept.getKey();
	    		}
	    	}
	    	public void widgetDefaultSelected(SelectionEvent e) {
	    		// this is not an option (text cant be entered)
	    	}
	    });
	    
		
	    // Next include 'Find' Button
	    findButton = new Button(compositeFindRow2, SWT.PUSH);
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
					
	    			browser.getSchemeData(schemesKey, schemes, findText).start();
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
	    browser = new NodeBrowser(compositeFindTop, 1, findButton, slm);
	    return compositeFind;
	}

	private void setSchemes(Combo schemesCombo)
	{
		// set default category for combo box
	
		schemesCombo.add("Any Coding System");
	    schemesCombo.setText("Any Coding System");
	    schemesKey = "any";	
		schemes = getSchemes();

		if(schemes != null) {
			Iterator schemesIterator = schemes.iterator();		
			while(schemesIterator.hasNext())
			{
				ConceptType scheme = (ConceptType) schemesIterator.next();
				String name = scheme.getName();
				schemesCombo.add(name);
			}
		}
	
		return;
	}
	
	public List getSchemes() 
	{
		try {
			GetReturnType request = new GetReturnType();
			request.setType("default");

			OntologyResponseMessage msg = new OntologyResponseMessage();
			StatusType procStatus = null;	
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = OntServiceDriver.getSchemes(request, "FIND");
				procStatus = msg.processResult(response);
//				if  error 
//				TABLE_ACCESS_DENIED and USER_INVALID, DATABASE ERROR
				if (procStatus.getType().equals("ERROR")){					
			 		System.setProperty("errorMessage", procStatus.getValue());
					return null;
				}
				procStatus.setType("DONE");
			}
			ConceptsType allConcepts = msg.doReadConcepts();   	    
			schemes = allConcepts.getConcept();

		} catch (AxisFault e) {
    		log.error(e.getMessage());
    		System.setProperty("errorMessage", "Ontology cell unavailable");	
		} catch (Exception e) {
    		log.error(e.getMessage());
    		System.setProperty("errorMessage", "Remote service unavailable");
		}
		return schemes;
	}

}
