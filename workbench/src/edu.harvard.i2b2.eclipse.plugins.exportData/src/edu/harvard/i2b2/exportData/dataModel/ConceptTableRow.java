/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 *     Christopher D. Herrick 
 */
package edu.harvard.i2b2.exportData.dataModel;

import org.eclipse.swt.graphics.RGB;

public class ConceptTableRow {
    public int rowId;
    public int rowNumber;
    public String conceptName;
    public String valueType;
    public String valueText;
    public String height;
    // public String constrainDate;
    public RGB color;
    public String conceptXml;

    public QueryConceptData data;

    public void data(QueryConceptData data_) {
	if (!data_.hasValue()) {
	    valueText = "Not Applicable";
	} else {
	    if (data_.valuePropertyData().noValue()) {
		valueText = "All values";
	    }
	}
	data = data_;
    }

    public QueryConceptData data() {
	return data;
    }

    // public int timelineRowNumber;
    // public String fullConceptPath;

    public ConceptTableRow() {
    }
}
