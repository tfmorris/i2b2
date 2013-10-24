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

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import edu.harvard.i2b2.eclipse.UserInfoBean;


/**
 * The Ontology View class provides the Ontology UI View to the
 *  Eclipse framework  --- This has been ported from the CRC Navigator project.
 * @author Lori Phillips   
 */

public class OntologyView extends ViewPart {

	public static final String ID = "edu.harvard.i2b2.eclipse.plugins.ontology.views.ontologyView";
	public static final String THIS_CLASS_NAME = OntologyView.class.getName();
	
	private Composite compositeQueryTree;
	
	public boolean bWantStatusLine = false;
	private StatusLineManager slm = new StatusLineManager();	


	/**
	 * The constructor.
	 */
	public OntologyView() {
		System.setProperty("OntMax", UserInfoBean.getInstance().getCellDataParam("ont", "OntMax"));
		System.setProperty("OntHiddens", UserInfoBean.getInstance().getCellDataParam("ont","OntHiddens"));
		System.setProperty("OntSynonyms",  UserInfoBean.getInstance().getCellDataParam("ont","OntSynonyms"));	
		System.setProperty("user", UserInfoBean.getInstance().getUserName());
		System.setProperty("pass", UserInfoBean.getInstance().getUserPassword());
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */

	public void createPartControl(Composite parent) {
		// Drag "from" tree
		compositeQueryTree = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 1;
		gridLayout.verticalSpacing = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		compositeQueryTree.setLayout(gridLayout);

		GridLayout gridLayoutTree = new GridLayout(1, false);
		gridLayoutTree.numColumns = 1;
		gridLayoutTree.marginHeight = 0;
		GridData fromTreeGridData = new GridData (GridData.FILL_BOTH);
		fromTreeGridData.widthHint = 300;
		compositeQueryTree.setLayoutData(fromTreeGridData);

		TreeComposite dragTree = new TreeComposite(compositeQueryTree, 1, slm);
		dragTree.setLayoutData(new GridData (GridData.FILL_BOTH));
		dragTree.setLayout(gridLayout);
	}


	
	/**
	 * Passing the focus request 
	 */
	public void setFocus() {
		compositeQueryTree.setFocus();
	}
}
