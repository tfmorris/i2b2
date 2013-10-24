/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *      Wensong Pan
 * 		Lori Phillips
 */
package net.nbirn.srbclient.plugin.views;

import javax.swing.JFrame;

import net.nbirn.srbclient.utils.MessageUtil;
import net.nbirn.srbclient.utils.Messages;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

//import edu.harvard.i2b2.eclipse.plugins.workplace.views.WorkplaceDisplayXmlMessageDialog;

/**
 * @author wp066
 *
 */
public class ViewRequestMessageToolbarActionDelegate implements IViewActionDelegate {

	private FRView workView;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		workView = (FRView) view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String request = MessageUtil.getInstance().getRequest();
		JFrame frame = new FRDisplayXmlMessageDialog(request);
        frame.setTitle(Messages.getString("ViewRequestMessageToolbarActionDelegate.XMLRequest")); //$NON-NLS-1$
        frame.setVisible(true);     
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}
