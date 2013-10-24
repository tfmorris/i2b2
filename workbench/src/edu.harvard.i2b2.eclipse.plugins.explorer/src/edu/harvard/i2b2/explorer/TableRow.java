/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 *     Christopher D. Herrick 
 */
package edu.harvard.i2b2.explorer;

import org.eclipse.swt.graphics.RGB;

public class TableRow {
	public int rowId;
	public int rowNumber;
	public String conceptName;
	public String valueType;
	public String valueText;
	public String height;
	public RGB color;
	public String conceptXml;
		
	//public int timelineRowNumber;
	//public String fullConceptPath;
		
	public TableRow() {}
}
