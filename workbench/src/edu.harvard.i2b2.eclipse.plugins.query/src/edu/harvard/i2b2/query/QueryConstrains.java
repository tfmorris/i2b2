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
 * Interface: QueryConstrains
 * 
 */
package edu.harvard.i2b2.query;

/**
 * @author wp066
 *
 */
public interface QueryConstrains {
    
	public void startYear(int i); 
	public int startYear();
	
	public void startMonth(int i);
	public int startMonth();
	
	public void startDay(int i); 
	public int startDay(); 
	
	public void startTime(long l); 
	public long startTime(); 
		
	public void endYear(int i); 
	public int endYear(); 
	
	public void endMonth(int i); 
	public int endMonth(); 
	
	public void endDay(int i); 
	public int endDay(); 
	
	public void endTime(long l); 
	public long endTime(); 
	
	public void includePrincipleVisit(boolean b);
	public boolean includePrincipleVisit();
	
	public void includeSecondaryVisit(boolean b);
	public boolean includeSecondaryVisit();
	
	public void includeAdmissionVisit(boolean b);
	public boolean includeAdmissionVisit();
}
