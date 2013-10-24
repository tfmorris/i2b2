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
package edu.harvard.i2b2.eclipse.plugins.exportData.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.exportData.dataModel.QueryConceptData;
import edu.harvard.i2b2.exportData.ui.DataExporter;

/**
 *  Class: ExplorerView 
 *  
 *  This class defines the Explorer View to the
 *  Eclipse workbench
 *  
 */

public class ExportDataView extends ViewPart implements ICommonMethod {
	public static final String ID = "edu.harvard.i2b2.eclipse.plugins.exportData.views.ExportDataView";
	
	private DataExporter explorer = null;
	public DataExporter explorer() {return explorer;}
	
	//setup context help
	public static final String PREFIX = "edu.harvard.i2b2.eclipse.plugins.exportData";
	public static final String EXPORTDATA_VIEW_CONTEXT_ID = PREFIX + ".exportdata_view_help_context";
	private Composite exportDataComposite;
	
	/**
	 * The constructor.
	 */
	public ExportDataView() {
		
	}

	//public record getRecord() {
	//	return explorer.getRecord();
	//}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		explorer = new DataExporter(parent, false);
		exportDataComposite = parent;
		
		//setup context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, EXPORTDATA_VIEW_CONTEXT_ID);
		addHelpButtonToToolBar();
	}
	
	/**
	 * This is a callback that will allow the i2b2 views to communicate
	 * with each other.
	 */
	public void doSomething(Object data) {
		if(data.getClass().getSimpleName().equalsIgnoreCase("String")) {
			String[] msgs = ((String)data).split("-");
			System.out.println(msgs[0]+" RefId: "+msgs[1]);
			explorer.setPatientSetText("Patient Set: "+msgs[0]+" Patients");
			explorer.setPatientMinNumText("1");
			explorer.patientRefId(msgs[1]); 
			explorer.setPatientSetSize(msgs[0]);
		}
		else {
			ArrayList<QueryConceptData> msgs = (ArrayList<QueryConceptData>) data;
			System.out.println("Explorer View: "+ msgs.get(0));
			explorer.populateTable(msgs);
			explorer.generateTimeLine();
		}
	}

	//add help button
	private void addHelpButtonToToolBar() {
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
		Action helpAction = new Action(){
			public void run() {
				helpSystem.displayHelpResource("/edu.harvard.i2b2.eclipse.plugins.exportData/html/i2b2_ed_index.htm");
		}
		};
		helpAction.setImageDescriptor(ImageDescriptor.createFromFile(ExportDataView.class, "/icons/help.png"));
		getViewSite().getActionBars().getToolBarManager().add(helpAction);
	}
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		//if(cfig!=null && propertyName!=null ) {
    		super.setInitializationData(cfig, propertyName, data);
    	/*}
		else {
			if(data.getClass().getSimpleName().equalsIgnoreCase("String")) {
				String[] msgs = ((String)data).split(":");
				System.out.println(msgs[0]+" RefId: "+msgs[1]);
			}
			else {
				ArrayList<String> msgs = (ArrayList<String>) data;
				System.out.println("Explorer View: "+ msgs.get(0));
				explorer.populateTable(msgs);
				explorer.generateTimeLine();
			}
		}*/
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		exportDataComposite.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		
		System.out.println("image view is closed.");
		/*File dir = new File("temp"+File.separator+"ImageJ");
		if(dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			
			for(int i=0; i<files.length; i++) {
				files[i].delete();
			}
		}*/
	}
}