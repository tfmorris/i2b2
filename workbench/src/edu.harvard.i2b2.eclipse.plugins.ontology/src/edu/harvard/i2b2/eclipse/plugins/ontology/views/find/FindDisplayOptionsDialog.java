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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FindDisplayOptionsDialog extends Dialog {
    
	private Text maximum = null;
	private Button showHiddens = null;
	private Button showSynonyms = null;
	
    /**
     * Creates new form FindDisplayOptionsDialog
     */
    public FindDisplayOptionsDialog(Shell parentShell) {
    	super(parentShell);
    	
    }
   
    @Override
	protected Control createDialogArea(Composite parent){
    	Composite comp = (Composite)super.createDialogArea(parent);
    	comp.getShell().setText("Find Terms Options");
    	
       	GridLayout layout = (GridLayout)comp.getLayout();
    	layout.numColumns = 2;
    	
    	Label maxLabel = new Label(comp, SWT.RIGHT);
    	maxLabel.setText("Maximum number of children to display: ");
    	maximum = new Text(comp, SWT.SINGLE);
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	maximum.setLayoutData(data);
		maximum.setText(System.getProperty("OntFindMax"));
		
		Composite compOptions = new Composite(comp, SWT.NULL);
		GridLayout gridLayoutOptions = new GridLayout(1, false);
		compOptions.setLayout(gridLayoutOptions);
		
    	showHiddens = new Button(compOptions, SWT.CHECK);
    	showHiddens.setText("Show hiddens");
		showHiddens.setSelection(Boolean.parseBoolean(
				System.getProperty("OntFindHiddens")));
    	
    	showSynonyms = new Button(compOptions, SWT.CHECK);
    	showSynonyms.setText("Show synonyms");
    	showSynonyms.setSelection(Boolean.parseBoolean(
				System.getProperty("OntFindSynonyms")));

    	return parent;
    }
    
    @Override
	protected void createButtonsForButtonBar(Composite parent){
    	super.createButtonsForButtonBar(parent);
    	createButton(parent, 2, "Reset to Defaults", false);
    }
    
    @Override
	protected void buttonPressed(int buttonId){
    	// reset
    	if(buttonId == 2){
    		maximum.setText("200");
    		showHiddens.setSelection(false);
    		showSynonyms.setSelection(false);
    	}	
    	// OK
    	else if(buttonId == 0){
    		String message = "";
    		try{
    			if(Integer.parseInt(maximum.getText())< 2)
    				message = "Maximum children size should be greater than 1 \n";
    		}catch(java.lang.NumberFormatException e){
    			message = message + "Maximum children size is invalid \n";
    		}
    		
    		if(!message.equals("")){
    			MessageBox mBox = new MessageBox(Display.getCurrent().getActiveShell(), 
						SWT.ICON_ERROR);
				mBox.setText("Please Note ...");
				mBox.setMessage(message);
				mBox.open();
    			return;
    		}
    		System.setProperty("OntFindMax", maximum.getText());
    		System.setProperty("OntFindHiddens", String.valueOf(showHiddens.getSelection()));
    		System.setProperty("OntFindSynonyms", String.valueOf(showSynonyms.getSelection()));
        	close();
    	}
    	//Cancel
    	else if(buttonId ==1) {
    		close();
    	}
    }

 }


