/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import java.util.List;
/**
 * Interface for handler of InputOptionListType
 * $Id: IInputOptionListHandler.java,v 1.5 2007/08/31 14:42:36 rk903 Exp $
 * @author rkuttan
 * @see {@link InputOptionListType}
 */
public interface IInputOptionListHandler {
	/**
	 * Get min index in enumeration list
	 * @return
	 */
	public int getMinIndex();
	/**
	 * Get max index in enumeration list
	 * @return
	 */
	public int getMaxIndex();
	/**
	 * Function to generate where clause of sql
	 * from input option list
	 * @return
	 */
	public String generateWhereClauseSql();
	/**
	 * Return true if list type is entire set 
	 * @return
	 */
	public boolean isEntireSet();
	/**
	 * Return true if list type is collection
	 * @return
	 */
	public boolean isCollectionId();
	/**
	 * Return true if list type is enumeration
	 * @return
	 */
	public boolean isEnumerationSet();
	/**
	 * Return collection id
	 * @return
	 */
	public String getCollectionId();
	/**
	 * Return enumneration list 
	 * @return
	 */
	public List<String> getEnumerationList();
}
