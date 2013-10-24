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
package edu.harvard.i2b2.exportData.data;

public class PatientSetData {

    private String setNumber;

    public void setNumber(String str) {
	setNumber = new String(str);
    }

    public String setNumber() {
	return setNumber;
    }

    private String setName;

    public void setName(String str) {
	setName = new String(str);
    }

    public String setName() {
	return setName;
    }

    private String setId;

    public void setId(String str) {
	setId = new String(str);
    }

    public String setId() {
	return setId;
    }

    public PatientSetData() {
    }

}
