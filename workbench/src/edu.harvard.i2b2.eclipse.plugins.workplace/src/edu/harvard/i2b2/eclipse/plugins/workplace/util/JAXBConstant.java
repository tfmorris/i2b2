/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */

package edu.harvard.i2b2.eclipse.plugins.workplace.util;


/**
 * Define JAXB constants here.
 * For dynamic configuration, move these values to property file
 * and read from it.
 *
 * @author rkuttan
 */
public class JAXBConstant {
    public static final String[] DEFAULT_PACKAGE_NAME = new String[] {
            "edu.harvard.i2b2.wkplclient.datavo.i2b2message",
            "edu.harvard.i2b2.wkplclient.datavo.wdo",
            "edu.harvard.i2b2.wkplclient.datavo.vdo",
            "edu.harvard.i2b2.wkplclient.datavo.dnd"
        };
    
    public static final String[] DND_PACKAGE_NAME = new String[] {
        "edu.harvard.i2b2.wkplclient.datavo.dnd",
        "edu.harvard.i2b2.wkplclient.datavo.wdo"
    };
}
