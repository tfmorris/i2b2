/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package net.nbirn.srbclient.plugin.views;

import net.nbirn.srbclient.utils.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.*;


/**
 *
 * @author  lcp5
 */
public class FRDisplayOptionsDialog extends Dialog {
    
    /**
     * Creates new form WorkplaceOptionsDialog
     */
    public FRDisplayOptionsDialog(Shell parentShell) {
    	super(parentShell);
    	
    }
      	
    @Override
	protected Control createDialogArea(Composite parent){
    	Composite comp = (Composite)super.createDialogArea(parent);
    	comp.getShell().setText(Messages.getString("FRDisplayOptionsDialog.Options")); //$NON-NLS-1$
   
    	return parent;
    }
    
    @Override
	protected void createButtonsForButtonBar(Composite parent){
    	super.createButtonsForButtonBar(parent);
    }
    
    @Override
	protected void buttonPressed(int buttonId){
    	// reset
    }

 }
