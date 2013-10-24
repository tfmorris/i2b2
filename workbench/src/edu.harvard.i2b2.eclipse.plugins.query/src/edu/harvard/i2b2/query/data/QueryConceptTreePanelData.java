/*
 * Copyright (c) 2006-2009 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution.  
 * 
 * Contributors: 
 *     Wensong Pan
 */
package edu.harvard.i2b2.query.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ConstrainDateType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ItemType.ConstrainByDate;
import edu.harvard.i2b2.query.ui.QueryConstrains;

/**
 * Class: QueryConceptTreeData.
 * 
 * A data holder class for QueryConceptTreePanel.
 */

public class QueryConceptTreePanelData implements QueryConstrains {
    private boolean exclude = false;

    public void exclude(boolean b) {
	exclude = b;
    }

    public boolean exclude() {
	return exclude;
    }

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

    private boolean includePrincipleVisit = true;

    public void includePrincipleVisit(boolean b) {
	includePrincipleVisit = b;
    }

    public boolean includePrincipleVisit() {
	return includePrincipleVisit;
    }

    private boolean includeSecondaryVisit = true;

    public void includeSecondaryVisit(boolean b) {
	includeSecondaryVisit = b;
    }

    public boolean includeSecondaryVisit() {
	return includeSecondaryVisit;
    }

    private boolean includeAdmissionVisit = true;

    public void includeAdmissionVisit(boolean b) {
	includeAdmissionVisit = b;
    }

    public boolean includeAdmissionVisit() {
	return includeAdmissionVisit;
    }

    private int occurrenceTimes = 1;

    public void setOccurrenceTimes(int i) {
	occurrenceTimes = i;
    }

    public int getOccurrenceTimes() {
	return occurrenceTimes;
    }

    private ArrayList<QueryConceptTreeNodeData> items = null;

    public QueryConceptTreePanelData() {
	items = new ArrayList<QueryConceptTreeNodeData>();
    }

    public ArrayList<QueryConceptTreeNodeData> getItems() {
	return items;
    }

    public ConstrainByDate writeTimeConstrain(ConstrainDateType from,
	    ConstrainDateType to) {
	ConstrainByDate timeConstrain = new ConstrainByDate();
	DTOFactory dtoFactory = new DTOFactory();

	// ConstrainDateType constraindateType = new ConstrainDateType();
	// constraindateType.setValue(dtoFactory.getXMLGregorianCalendarDate(
	// endYear(),
	// endMonth(), endDay()));
	Calendar cal = Calendar.getInstance();
	if (from != null) {
	    timeConstrain.setDateFrom(from);
	    startYear = from.getValue().getYear();
	    startMonth = from.getValue().getMonth();
	    startDay = from.getValue().getDay();
	    cal.set(startYear, startMonth, startDay);
	    startTime = cal.getTimeInMillis(); // new Date(startYear,
	    // startMonth,
	    // startDay).getTime();
	    // startTime(from.getTime().
	}
	if (to != null) {
	    timeConstrain.setDateTo(to);
	    endYear = to.getValue().getYear();
	    endMonth = to.getValue().getMonth();
	    endDay = to.getValue().getDay();
	    cal.set(endYear, endMonth, endDay);
	    endTime = cal.getTimeInMillis(); // new Date(startYear, startMonth,
	    // startDay).getTime();

	}
	return timeConstrain;
    }

    public ConstrainByDate writeTimeConstrain() {
	ConstrainByDate timeConstrain = new ConstrainByDate();
	DTOFactory dtoFactory = new DTOFactory();

	if (startTime() != -1) {
	    ConstrainDateType constraindateType = new ConstrainDateType();
	    constraindateType.setValue(dtoFactory.getXMLGregorianCalendarDate(
		    startYear(), startMonth() + 1, startDay()));
	    timeConstrain.setDateFrom(constraindateType);
	}

	if (endTime() != -1) {
	    ConstrainDateType constraindateType = new ConstrainDateType();
	    constraindateType.setValue(dtoFactory.getXMLGregorianCalendarDate(
		    endYear(), endMonth() + 1, endDay()));

	    timeConstrain.setDateTo(constraindateType);
	}
	return timeConstrain;
    }
}
