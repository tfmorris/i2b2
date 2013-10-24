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

import java.util.ArrayList;
import java.util.List;

import edu.harvard.i2b2.common.datavo.pdo.ParamType;
import edu.harvard.i2b2.common.datavo.pdo.PidType.PatientMapId;

public class PatientTableRow {
    public int rowId;
    public int rowNumber;
    public String patientSetNumber;
    public String patientID;
    public String patientName;

    public String gender;

    public String gender() {
	return gender;
    }

    public void gender(String str) {
	gender = new String(str);
    }

    public String race;

    public String race() {
	return race;
    }

    public void race(String str) {
	race = new String(str);
    }

    public String dateOfBirth;

    public String dateOfBirth() {
	return dateOfBirth;
    }

    public void dateOfBirth(String str) {
	dateOfBirth = new String(str);
    }

    public String age;

    public String age() {
	return age;
    }

    public void age(String str) {
	age = new String(str);
    }

    public String decision;

    public String decision() {
	return decision;
    }

    public void decision(String str) {
	decision = new String(str);
    }

    public ArrayList<String> MRN;

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

    public String MRNString;

    public PatientTableRow() {
    }

    {
	decision = new String("UnD");
	MRN = new ArrayList<String>();
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
	    } else if (param.getColumn().equalsIgnoreCase("birth_date")) {
		dateOfBirth(param.getValue());
	    }
	}

	patientName = lastName + ", " + firstName;
    }

    public void setMRNs(List<PatientMapId> list) {
	for (int i = 0; i < list.size(); i++) {
	    PatientMapId pmId = list.get(i);
	    MRN.add(pmId.getSource() + pmId.getValue());
	}

	getMRNs();
    }

    public String getMRNs() {
	MRNString = new String("");

	MRNString = MRN.get(0);
	for (int i = 1; i < MRN.size(); i++) {
	    MRNString += "," + MRN.get(i);
	}
	return MRNString;
    }

    public void addKnownSet(String number) {

    }
}
