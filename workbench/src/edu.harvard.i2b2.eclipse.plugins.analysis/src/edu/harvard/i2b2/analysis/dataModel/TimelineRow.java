/*
 * Copyright (c) 2006-2010 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */
/**
 * 
 */
package edu.harvard.i2b2.analysis.dataModel;

import java.util.ArrayList;


public class TimelineRow {
	
	public String displayName = "";
	
	public ArrayList<PDOItem> pdoItems;

	public TimelineRow() {
		pdoItems = new ArrayList<PDOItem>(); 
	}
}
