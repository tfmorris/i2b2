/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */
/**
 * 
 */
package edu.harvard.i2b2.explorer;

import java.util.ArrayList;

/**
 * @author wp066
 *
 */
public class TimelineRow {
	
	public String displayName = "";
	
	public ArrayList<PDOSet> pdoSets;

	public TimelineRow() {
		pdoSets = new ArrayList<PDOSet>(); 
	}
}
