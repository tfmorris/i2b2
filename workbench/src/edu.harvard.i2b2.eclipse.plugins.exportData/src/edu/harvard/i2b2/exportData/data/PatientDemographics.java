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

import java.util.HashMap;
import java.util.List;

import edu.harvard.i2b2.common.datavo.pdo.ParamType;

/**
 * class: PatientDemographics
 * 
 * 
 */

public class PatientDemographics {
    private String age;

    public void age(String str) {
	age = new String(str);
    }

    public String age() {
	return age;
    }

    private String gender;

    public void gender(String str) {
	gender = new String(str);
    }

    public String gender() {
	return gender;
    }

    private String vitalStatus;

    public void vitalStatus(String str) {
	vitalStatus = new String(str);
    }

    public String vitalStatus() {
	return vitalStatus;
    }

    private String race;

    public void race(String str) {
	race = new String(str);
    }

    public String race() {
	return race;
    }

    private String patientNumber;

    public void patientNumber(String str) {
	patientNumber = new String(str);
    }

    public String patientNumber() {
	return patientNumber;
    }

    private String lastName = "xxxxx";

    public String lastName() {
	return lastName;
    }

    public void lastName(String str) {
	lastName = new String(str);
    }

    private String firstName = "xxxxx";

    public String firstName() {
	return firstName;
    }

    public void firstName(String str) {
	firstName = new String(str);
    }

    private String birthDate;

    public void birthDate(String str) {
	birthDate = new String(str);
    }

    public String birthDate() {
	return birthDate;
    }

    private String deathDate;

    public void deathDate(String str) {
	deathDate = new String(str);
    }

    public String deathDate() {
	return deathDate;
    }

    public HashMap<String, String> params = null;

    public PatientDemographics() {
	age("");
	gender("");
	vitalStatus("");
	race("");
	patientNumber("");
	birthDate("");
	deathDate("");

	params = new HashMap<String, String>();
    }

    public void setParamData(List<ParamType> list) {
	for (int i = 0; i < list.size(); i++) {
	    ParamType param = list.get(i);
	    if (param.getColumn().equalsIgnoreCase("lastName")) {
		lastName(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("firstName")) {
		firstName(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("age_in_years_num")) {
		age(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("race_cd")) {
		race(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("sex_cd")) {
		gender(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("vital_status_cd")) {
		vitalStatus(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("birth_date")) {
		birthDate(param.getValue());
	    } else if (param.getColumn().equalsIgnoreCase("death_date")) {
		deathDate(param.getValue());
	    } else {
		params.put(param.getColumn(), param.getValue());
	    }
	}
    }
}
