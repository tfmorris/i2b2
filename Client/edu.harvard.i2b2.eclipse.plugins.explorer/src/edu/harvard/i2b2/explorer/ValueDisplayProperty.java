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

import org.eclipse.swt.graphics.RGB;

/**
 * @author wp066
 *
 */
public class ValueDisplayProperty {

	public double left;
	
	public double right;
	
	public String height;
	
	public String color;
	
	public ValueDisplayProperty() {
		
	}
	
	public boolean inRange(double val) {
		if(val >= left && val <= right) {
			return true;
		}
		
		return false;
	}
	
}
