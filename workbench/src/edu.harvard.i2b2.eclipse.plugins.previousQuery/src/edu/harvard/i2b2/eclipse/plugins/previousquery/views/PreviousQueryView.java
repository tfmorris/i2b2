/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

package edu.harvard.i2b2.eclipse.plugins.previousquery.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JRootPane;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;

import org.eclipse.ui.part.ViewPart;

import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.previousquery.QueryMasterData;
import edu.harvard.i2b2.previousquery.QueryPreviousRunsPanel;


/**
 *  Class: PreviousQueryView 
 *  
 *  This class defines the Previous Query View to the
 *  Eclipse workbench
 *  
 *  @author Wensong Pan
 */

public class PreviousQueryView extends ViewPart implements ICommonMethod {
	
	public static final String ID = "edu.harvard.i2b2.eclipse.plugins.previousquery.views.PreviousQueryView";
	public static final String THIS_CLASS_NAME = PreviousQueryView.class.getName();
	
	private java.awt.Container oAwtContainer;
	
	private QueryPreviousRunsPanel runTreePanel;
	public QueryPreviousRunsPanel runTreePanel() {return runTreePanel;}
	
	/**
	 * The constructor
	 */
    public PreviousQueryView() {
    	
    }
    
    @Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
    	//if(cfig!=null && propertyName!=null) {
    		super.setInitializationData(cfig, propertyName, data);
    	//}
		
    	/*String msg = (String)data;
		System.out.println(msg);*/
	}
    
    /**
	 * This is a callback that will allow the i2b2 views to communicate
	 * with each other.
	 */
    public void doSomething(Object obj) {
    	String msg = (String) obj;
    	String[] msgs = msg.split("#i2b2seperater#");
    	
    	QueryMasterData nameNode = new QueryMasterData();
    	nameNode.visualAttribute("CA");
    	nameNode.userId(UserInfoBean.getInstance().getUserName());
    	nameNode.tooltip("A query run by "+nameNode.userId());
    	nameNode.id(msgs[1]);
    	nameNode.name(msgs[0]+" ["+UserInfoBean.getInstance().getUserName()+"]");
		
    	addNode(nameNode);
	}

	public void addNode(QueryMasterData node) {
    	runTreePanel.insertNode(node);
    }
	
	/**
	 * This is a callback that will allow us
	 * to create the tabbed viewers and initialize them.
	 */
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		
	    /* Create and setting up frame */
	    Frame runFrame = SWT_AWT.new_Frame(composite);
	    Panel runPanel = new Panel(new BorderLayout());
	    try {
	    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch(Exception e) {
	    	System.out.println("Error setting native LAF: " + e);
	    }
	    	    
	    runFrame.add(runPanel);
	    JRootPane runRoot = new JRootPane();
	    runPanel.add(runRoot);
	    oAwtContainer = runRoot.getContentPane();
	    
	    runTreePanel = new QueryPreviousRunsPanel(this);
	    oAwtContainer.add(runTreePanel);
	}
	
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
}
	
	

