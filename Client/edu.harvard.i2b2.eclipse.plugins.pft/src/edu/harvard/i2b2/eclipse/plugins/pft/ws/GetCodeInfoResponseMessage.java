/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.pft.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.pftclient.datavo.vdo.GetChildrenType;


/**
 * @author Lori Phillips
 *
 */
public class GetCodeInfoResponseMessage extends OntologyResponseData {
	
	public static final String THIS_CLASS_NAME = GetCodeInfoResponseMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	

    private GetChildrenType childrenType;

	public GetCodeInfoResponseMessage() {}
	
	
	
	
}
	
	