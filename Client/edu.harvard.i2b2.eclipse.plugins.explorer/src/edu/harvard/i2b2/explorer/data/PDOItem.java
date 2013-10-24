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
/**
 * 
 */

package edu.harvard.i2b2.explorer.data;

import java.util.ArrayList;

import edu.harvard.i2b2.explorer.ui.ValueData;

public class PDOItem {

public String fullPath;
	
	public String dimcode;
	
	public String tableType; //fact, visit, provider or patient
	
	public boolean hasValueDisplayProperty = false;
	
	public String height;
	
	public String color;
	
	public ArrayList<ValueData> valDisplayProperties;
	
	public PDOItem() {
		valDisplayProperties = new ArrayList<ValueData>();
	}

}
