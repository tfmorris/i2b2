/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 * 		Janice Donahoe (documentation for on-line help)
 */


package edu.harvard.i2b2.eclipse.plugins.ontology.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
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

	//setup context help
	public static final String PREFIX = "edu.harvard.i2b2.eclipse.plugins.ontology";
	public static final String ONTOLOGY_VIEW_CONTEXT_ID = PREFIX + ".navigate_terms_view_help_context";
	
	private Composite compositeQueryTree;
	private Log log = LogFactory.getLog(THIS_CLASS_NAME);
	public boolean bWantStatusLine = false;
	private StatusLineManager slm = new StatusLineManager();	


	/**
	 * The constructor.
	 */
	public OntologyView() {
		if (UserInfoBean.getInstance().getCellDataParam("ont", "OntMax") != null)
			System.setProperty("OntMax", UserInfoBean.getInstance().getCellDataParam("ont", "OntMax"));
		else 
			System.setProperty("OntMax","200");
		if (UserInfoBean.getInstance().getCellDataParam("ont", "OntHiddens") != null)	
			System.setProperty("OntHiddens", UserInfoBean.getInstance().getCellDataParam("ont","OntHiddens"));
		else
			System.setProperty("OntHiddens","false");
		if (UserInfoBean.getInstance().getCellDataParam("ont", "OntSynonyms") != null)
			System.setProperty("OntSynonyms",  UserInfoBean.getInstance().getCellDataParam("ont","OntSynonyms"));	
		else
			System.setProperty("OntSynonyms","false");
		//System.setProperty("user", UserInfoBean.getInstance().getUserName());
		//System.setProperty("pass", UserInfoBean.getInstance().getUserPassword());
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */

	@Override
	public void createPartControl(Composite parent) {
		log.info("Ontology plugin version 1.3.0");
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
		
		//setup context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ONTOLOGY_VIEW_CONTEXT_ID);
		addHelpButtonToToolBar();
	}

	//add help button
	private void addHelpButtonToToolBar() {
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
		Action helpAction = new Action(){
			public void run() {
				helpSystem.displayHelpResource("/edu.harvard.i2b2.eclipse.plugins.ontology/html/i2b2_navigate_terms_index.htm");
		}
		};
		helpAction.setImageDescriptor(ImageDescriptor.createFromFile(OntologyView.class, "/icons/help.png"));
		getViewSite().getActionBars().getToolBarManager().add(helpAction);
	}
	
	/**
	 * Passing the focus request 
	 */
	@Override
	public void setFocus() {
		compositeQueryTree.setFocus();
	}
}
