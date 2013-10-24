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
/**
 * 
 */

package edu.harvard.i2b2.exportData.data;

import java.util.ArrayList;

import edu.harvard.i2b2.exportData.dataModel.ValuePropertyData;
import edu.harvard.i2b2.exportData.ui.ValueData;

public class PDOItem {

    public String fullPath;

    public String dimcode;

    public String tableType; // fact, visit, provider or patient

    public boolean hasValueDisplayProperty = false;

    public String height;

    public String color;

    public ArrayList<ValuePropertyData> valDisplayProperties;

    private int startYear = -1;

    public void startYear(int i) {
	startYear = i;
    }

    public int startYear() {
	return startYear;
    }

    private int startMonth = -1;

    public void startMonth(int i) {
	startMonth = i;
    }

    public int startMonth() {
	return startMonth;
    }

    private int startDay = -1;

    public void startDay(int i) {
	startDay = i;
    }

    public int startDay() {
	return startDay;
    }

    private long startTime = -1;

    public void startTime(long l) {
	startTime = l;
    }

    public long startTime() {
	return startTime;
    }

    private int endYear = -1;

    public void endYear(int i) {
	endYear = i;
    }

    public int endYear() {
	return endYear;
    }

    private int endMonth = -1;

    public void endMonth(int i) {
	endMonth = i;
    }

    public int endMonth() {
	return endMonth;
    }

    private int endDay = -1;

    public void endDay(int i) {
	endDay = i;
    }

    public int endDay() {
	return endDay;
    }

    private long endTime = -1;

    public void endTime(long l) {
	endTime = l;
    }

    public long endTime() {
	return endTime;
    }

    public PDOItem() {
	valDisplayProperties = new ArrayList<ValuePropertyData>();
    }

}
