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
 *  Class: QueryResultData
 */
package edu.harvard.i2b2.previousquery.data;



/**
 * @author wp066
 *
 */
public class QueryResultData extends QueryData {
	
	private String type;
	public void type(String str) {type = str;}
	public String type() {return type;}
	
	private String queryId;
	public void queryId(String str) {queryId = str;}
	public String queryId() {return queryId;}
	
	private String finishedTime;
	public void finishedTime(String str) {finishedTime = str;}
	public String finishedTime() {return finishedTime;}
	
	private String patientCount;
	public void patientCount(String str) {patientCount = str;}
	public String patientCount() {return patientCount;}
	
	private String patientRefId;
	public void patientRefId(String str) {patientRefId = str;}
	public String patientRefId() {return patientRefId;}

	
	public QueryResultData() {}

	@Override
	public String writeContentQueryXML() {
		
		String requestXml = null;
		int count = Integer.parseInt(patientCount);
		try {
			PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();	
			//requestXml = pdoFactory.requestXmlMessage("zzp___050206101533684227.xml", new Integer(0), new Integer(5), false);
			requestXml = pdoFactory.
				requestXmlMessage(patientRefId, new Integer(1), new Integer(count), false);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "error";
		}
		return requestXml;
	}
	
	
}
