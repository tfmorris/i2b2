/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     
 */
package edu.harvard.i2b2.smlib;

public interface Callback {
  public boolean callBackMethod(String name, String value);
}