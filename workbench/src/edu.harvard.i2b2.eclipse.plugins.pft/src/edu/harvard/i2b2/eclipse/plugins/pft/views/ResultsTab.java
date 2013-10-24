/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.pft.views;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Font;

/**
 * The ResultsTab class provides the Results table 
 * 
 * @author Lori Phillips   
 */


public class ResultsTab {
	private Table table;
	private static ResultsTab instance;
	
	/**
	 * The constructor
	 */
	private ResultsTab(Composite  tabFolder, Font font) {
	    TableLayout tableLayout = new TableLayout();
	    tableLayout.addColumnData(new ColumnWeightData(33, 75, false));
	    tableLayout.addColumnData(new ColumnWeightData(33, 75, false));
	    tableLayout.addColumnData(new ColumnWeightData(33, 75, false));
	    	    
		table = new Table(tabFolder, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(font);
		
		TableColumn col1 = new TableColumn(table, SWT.LEFT);
		TableColumn col2 = new TableColumn(table, SWT.LEFT);
		TableColumn col3 = new TableColumn(table, SWT.LEFT);
		col1.setText("Name");
		col2.setText("Value/Units");
		col3.setText("Code");
	}
	/**
	 * Function to set the initial ResponseTab instance
	 * 
	 * @param tabFolder Composite to place tabFolder into
	 * @return  ResultsTab object
	 */
	public static void setInstance(Composite tabFolder, Font font) {
		instance = new ResultsTab(tabFolder, font);
	}

	/**
	 * Function to return the ResultsTab instance
	 * 
	 * @return  ResultsTab object
	 */
	public static ResultsTab getInstance() {
		return instance;
	}

	/**
	 * Function to return the Table widget
	 * 
	 * @return  Table object
	 */
	public Table getTable(){
		return table;
	}

	/**
	 * Function to add an item to the Table widget
	 * 
	 * @param name,value_units,code Strings to place in Table widget
	 */
	public void setItem(String name, String value_units, String code){
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText( new String[] {name, value_units, code });
	}

	/**
	 * Function to clear the Table widget
	 * 
	 */
	public void clear() {
		int count = table.getItemCount();
		for(int i=0; i < count ; i++ )	    			
			table.remove(0);
	}

	/**
	 * Function to remove the last item in the Table widget
	 * 
	 */
	public void removeLastLine() {
		int count = table.getItemCount();
		if(count != 0)
			table.remove(count-1);
	}
}
