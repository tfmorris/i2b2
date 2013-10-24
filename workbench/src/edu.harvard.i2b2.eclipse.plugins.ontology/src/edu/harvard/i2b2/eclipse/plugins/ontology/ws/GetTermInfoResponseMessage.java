/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */

package edu.harvard.i2b2.eclipse.plugins.ontology.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.ontclient.datavo.vdo.GetTermInfoType;

public class GetTermInfoResponseMessage extends OntologyResponseData{
	
	public static final String THIS_CLASS_NAME = GetTermInfoResponseMessage.class.getName();
    private Log log = LogFactory.getLog(THIS_CLASS_NAME);	

    private GetTermInfoType termInfoType;

	public GetTermInfoResponseMessage() {}
	
	
	
	
}
