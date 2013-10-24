/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Wensong Pan
 *     Janice Donahoe (documentation for on-line help)
 *     
 */
package edu.harvard.i2b2.eclipse.plugins.explorer.views;

import java.util.ArrayList;

//import org.eclipse.swt.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.explorer.ui.ExplorerC;
import edu.harvard.i2b2.timeline.lifelines.record;

/**
 *  Class: ExplorerView 
 *  
 *  This class defines the Explorer View to the
 *  Eclipse workbench
 *  
 *  @author Wensong Pan
 */


public class ExplorerView extends ViewPart implements ICommonMethod {
	public static final String ID = "edu.harvard.i2b2.eclipse.plugins.explorer.views.ExplorerView";
    private static final Log log = LogFactory.getLog(ExplorerView.class);
	
	//setup context help
	public static final String PREFIX = "edu.harvard.i2b2.eclipse.plugins.explorer";
	public static final String TIMELINE_VIEW_CONTEXT_ID = PREFIX + ".timeline_view_help_context";
	private Composite timelineComposite;
	
	private ExplorerC explorer = null;
	public ExplorerC explorer() {return explorer;}
	
	/**
	 * The constructor.
	 */
	public ExplorerView() {
		
	}

	public record getRecord() {
		return explorer.getRecord();
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		explorer = new ExplorerC(parent, false);
		timelineComposite = parent;
		
		//setup context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, TIMELINE_VIEW_CONTEXT_ID);
		addHelpButtonToToolBar();

	}
	
	/**
	 * This is a callback that will allow the i2b2 views to communicate
	 * with each other.
	 */
	public void doSomething(Object data) {
		if(data.getClass().getSimpleName().equalsIgnoreCase("String")) {
			String[] msgs = ((String)data).split("-");
			log.debug(msgs[0]+" RefId: "+msgs[1]);
			explorer.setPatientSetText("Patient Set: "+msgs[0]+" Patients");
			explorer.setPatientMinNumText("1");
			explorer.patientRefId(msgs[1]); 
			explorer.setPatientSetSize(msgs[0]);
		}
		else {
			ArrayList<String> msgs = (ArrayList<String>) data;
			log.debug("Explorer View: "+ msgs.get(0));
			explorer.populateTable(msgs);
			explorer.generateTimeLine();
		}
	}

	//add help button
	private void addHelpButtonToToolBar() {
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
		Action helpAction = new Action(){
			public void run() {
				helpSystem.displayHelpResource("/edu.harvard.i2b2.eclipse.plugins.explorer/html/i2b2_timeline_index.htm");
		}
		};
		helpAction.setImageDescriptor(ImageDescriptor.createFromFile(ExplorerView.class, "/icons/help.png"));
		getViewSite().getActionBars().getToolBarManager().add(helpAction);
	}
	
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
    		super.setInitializationData(cfig, propertyName, data);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		timelineComposite.setFocus();
	}
}