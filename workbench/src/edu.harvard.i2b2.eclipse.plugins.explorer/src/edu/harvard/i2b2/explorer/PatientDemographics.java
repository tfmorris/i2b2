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
package edu.harvard.i2b2.explorer;

/**
 * class: PatientDemographics
 *
 * 
 */

public class PatientDemographics {
	private String age;
	public void age(String str) {age = new String(str);}
	public String age() {return age;}
	
	private String gender;
	public void gender(String str) {gender = new String(str);}
	public String gender() {return gender;}
	
	private String vitalStatus;
	public void vitalStatus(String str) {vitalStatus = new String(str);}
	public String vitalStatus() {return vitalStatus;}
	
	private String race;
	public void race(String str) {race = new String(str);}
	public String race() {return race;}
	
	private String patientNumber;
	public void patientNumber(String str) {patientNumber = new String(str);}
	public String patientNumber() {return patientNumber;}
	
	public PatientDemographics() {	
		age("");
		gender("");
		vitalStatus("");
		race("");
		patientNumber("");
	}
}
