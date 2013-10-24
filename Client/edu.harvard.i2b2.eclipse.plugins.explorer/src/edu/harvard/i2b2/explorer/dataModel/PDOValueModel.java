/*
 * Copyright (c) 2006-2010 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *   
 *     Wensong Pan
 *     
 */

package edu.harvard.i2b2.explorer.dataModel;

import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.ConstrainOperatorType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.ConstrainValueType;
import edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query.ItemType.ConstrainByValue;

public class PDOValueModel {

    public double left;
    public double right;
    public String height;
    public String color;

    private String operator;

    public String operator() {
	return operator;
    }

    public void operator(String str) {
	operator = new String(str);
    }

    private String value;

    public String value() {
	return value;
    }

    public void value(String str) {
	value = new String(str);
    }
    
    private String valueFlag;

    public String valueFlag() {
	return valueFlag;
    }

    public void valueFlag(String str) {
	valueFlag = new String(str);
    }
    
    private boolean useValueFlag = false;

    public boolean useValueFlag() {
	return useValueFlag;
    }

    public void useValueFlag(boolean b) {
	useValueFlag = b;
    }

    private String unit;

    public String unit() {
	return unit;
    }

    public void unit(String str) {
	unit = new String(str);
    }

    private boolean useNumericValue = false;
        
    public boolean useNumericValue() {
	return useNumericValue;
    }

    public void useNumericValue(boolean b) {
	useNumericValue = b;
    }

    private boolean useTextValue = false;

    public boolean useTextValue() {
	return useTextValue;
    }

    public void useTextValue(boolean b) {
	useTextValue = b;
    }

    public PDOValueModel() {
    	
    }

    public boolean inRange(double val) {
	if (val >= left && val <= right) {
	    return true;
	}

	return false;
    }

    public ConstrainByValue writeValueConstrain(PSMValueModel model) {
	ConstrainByValue valueConstrain = new ConstrainByValue();

	if (model.useNumericValue()) {
	    valueConstrain.setValueType(ConstrainValueType.NUMBER);

	    if (model.operator()!= null && model.operator().equalsIgnoreCase("BETWEEN")) {
		valueConstrain.setValueConstraint(model.lowValue() + " and " + model.highValue());
		valueConstrain.setValueOperator(ConstrainOperatorType.BETWEEN);
	    } else {
		valueConstrain.setValueConstraint(model.value());
		valueConstrain.setValueOperator(getOperator(model.operator()));
	    }
	} else if (model.useTextValue()) {
		valueConstrain.setValueType(ConstrainValueType.TEXT);
	    valueConstrain.setValueConstraint(model.getSelectedTexts());
	    valueConstrain.setValueOperator(ConstrainOperatorType.IN);
	}
	else if (model.useValueFlag()) {
	    valueConstrain.setValueType(ConstrainValueType.FLAG);
	    valueConstrain.setValueConstraint(model.value());
	    valueConstrain.setValueOperator(ConstrainOperatorType.EQ);
	}
	valueConstrain.setValueUnitOfMeasure(model.unit());

	return valueConstrain;
    }
    
    public ConstrainOperatorType getOperator(String op) {
		ConstrainOperatorType result = null;
		if (op == null) {
			return result;
		}

		if (op.equalsIgnoreCase("LESS THAN (<)") || op.equalsIgnoreCase("LT")) {
			result = ConstrainOperatorType.LT;
		} else if (op.equalsIgnoreCase("LESS THAN OR EQUAL TO (<=)")|| op.equalsIgnoreCase("LE")) {
			result = ConstrainOperatorType.LE;
		} else if (op.equalsIgnoreCase("EQUAL TO (=)")
				|| op.equalsIgnoreCase("EQ")) {
			result = ConstrainOperatorType.EQ;
		} else if (op.equalsIgnoreCase("GREATER THAN (>)")
				|| op.equalsIgnoreCase("GT")) {
			result = ConstrainOperatorType.GT;
		} else if (op.equalsIgnoreCase("GREATER THAN OR EQUAL TO (>=)")
				|| op.equalsIgnoreCase("GE")) {
			result = ConstrainOperatorType.GE;
		}

		return result;
	}
    
}
