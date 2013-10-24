/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Wensong Pan
 *     
 */
package edu.harvard.i2b2.eclipse.plugins.explorer.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.IConfigurationElement;

import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.explorer.ExplorerC;
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
	public void createPartControl(Composite parent) {
		explorer = new ExplorerC(parent, false);
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
			ArrayList<String> msgs = (ArrayList<String>) data;
			System.out.println("Explorer View: "+ msgs.get(0));
			explorer.populateTable(msgs);
			explorer.generateTimeLine();
		}
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
	public void setFocus() {
	}
}