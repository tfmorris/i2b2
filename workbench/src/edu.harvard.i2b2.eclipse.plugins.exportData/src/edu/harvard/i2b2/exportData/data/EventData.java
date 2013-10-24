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

import java.util.List;

import edu.harvard.i2b2.common.datavo.pdo.ParamType;

public class EventData {

    private String patientID;

    public void patientID(String str) {
	patientID = new String(str);
    }

    public String patientID() {
	return patientID;
    }

    private String eventID;

    public void eventID(String str) {
	eventID = new String(str);
    }

    public String eventID() {
	return eventID;
    }

    private String admissionStatus;

    public void admissionStatus(String str) {
	admissionStatus = new String(str);
    }

    public String admissionStatus() {
	return admissionStatus;
    }

    private String site;

    public void site(String str) {
	site = new String(str);
    }

    public String site() {
	return site;
    }

    private String location;

    public void location(String str) {
	location = new String(str);
    }

    public String location() {
	return location;
    }

    private String startDate;

    public void startDate(String str) {
	startDate = new String(str);
    }

    public String startDate() {
	return startDate;
    }

    private String endDate;

    public void endDate(String str) {
	endDate = new String(str);
    }

    public String endDate() {
	return endDate;
    }

    public EventData() {
	eventID("");
	patientID("");
    }

    public void setParamData(List<ParamType> list) {
	for (int i = 0; i < list.size(); i++) {
	    ParamType param = list.get(i);
	    if (param.getColumn() != null
		    && param.getColumn().equalsIgnoreCase("inout_cd")) {
		admissionStatus(param.getValue());
	    } else if (param.getColumn() != null
		    && param.getColumn().equalsIgnoreCase("site_cd")) {
		site(param.getValue());
	    }
	}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

}
