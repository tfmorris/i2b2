/*
 * @(#) $RCSfile: TypedTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * see: http://www.gnu.org/copyleft/gpl.html
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 */

package edu.umn.genomics.table;

import java.io.Serializable;
import java.util.*;
import javax.swing.table.*;

/**
 * A TypedTableModel object extends the DefaultTableModel by overriding the
 * getColumnClass method such that it returns the most specific class common to
 * all cells in the column. The Class for a column can be explicitly set by
 * {@link #setColumnClass}.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.table.DefaultTableModel
 */
public class TypedTableModel extends DefaultTableModel implements Serializable {
    /** All classes common to all members of each column. */
    Vector commonColumnClasses = new Vector();
    Vector columnClasses = new Vector();

    /**
     * Constructs a default TypedTableModel which is a table of zero columns and
     * zero rows.
     */
    public TypedTableModel() {
	super();
    }

    /**
     * Constructs a TypedTableModel with <i>numRows</i> and <i>numColumns</i> of
     * <b>null</b> object values.
     * 
     * @param numRows
     *            The number of rows the table holds
     * @param numColumns
     *            The number of columns the table holds
     * 
     * @see #setValueAt
     */
    public TypedTableModel(int numRows, int numColumns) {
	super(numRows, numColumns);
    }

    /**
     * Constructs a TypedTableModel with as many columns as there are elements
     * in <i>columnNames</i> and <i>numRows</i> of <b>null</b> object values.
     * Each column's name will be taken from the <i>columnNames</i> vector.
     * 
     * @param columnNames
     *            Vector containing the names of the new columns. If this null
     *            then the model has no columns
     * @param numRows
     *            The number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    public TypedTableModel(Vector columnNames, int numRows) {
	super(columnNames, numRows);
    }

    /**
     * Constructs a TypedTableModel with as many columns as there are elements
     * in <i>columnNames</i> and <i>numRows</i> of <b>null</b> object values.
     * Each column's name will be taken from the <i>columnNames</i> array.
     * 
     * @param columnNames
     *            Array containing the names of the new columns. If this null
     *            then the model has no columns
     * @param numRows
     *            The number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    public TypedTableModel(Object[] columnNames, int numRows) {
	super(columnNames, numRows);
    }

    /**
     * Constructs a TypedTableModel and initializes the table by passing
     * <i>data</i> and <i>columnNames</i> to the setDataVector() method.
     * 
     * @param data
     *            The data of the table
     * @param columnNames
     *            Vector containing the names of the new columns.
     * @see #getDataVector
     * @see #setDataVector
     */
    public TypedTableModel(Vector data, Vector columnNames) {
	super(data, columnNames);
    }

    /**
     * Constructs a TypedTableModel and initializes the table by passing
     * <i>data</i> and <i>columnNames</i> to the setDataVector() method. The
     * first index in the Object[][] is the row index and the second is the
     * column index.
     * 
     * @param data
     *            The data of the table
     * @param columnNames
     *            The names of the columns.
     * @see #getDataVector
     * @see #setDataVector
     */
    public TypedTableModel(Object[][] data, Object[] columnNames) {
	super(data, columnNames);
    }

    /**
     * Return the class hierarchy for this object. the given column.
     * 
     * @param o
     *            the object being queried
     * @return The classes for this object.
     */
    public static Vector getObjectClasses(Object o) {
	Vector cl = new Vector();
	for (Class cc = o.getClass(); cc != null; cc = cc.getSuperclass()) {
	    cl.addElement(cc);
	}
	return cl;
    }

    /**
     * Return the classes that are common to all members of the given column.
     * 
     * @param columnIndex
     *            the column being queried
     * @return The common classes for the column cells.
     */
    public Vector getCommonClasses(int columnIndex) {
	Vector commonClasses = null;
	if (columnIndex >= 0 && columnIndex < getColumnCount()) {
	    for (int row = 0; row < getRowCount(); row++) {
		Object o = getValueAt(row, columnIndex);
		if (o != null) {
		    Vector cl = getObjectClasses(o);
		    if (commonClasses == null) {
			commonClasses = cl;
		    } else {
			commonClasses.retainAll(cl);
		    }
		}
	    }
	}
	return commonClasses;
    }

    /**
     * Return the most specific class that is common to all Object values of the
     * TableModel column at columnIndex.
     * 
     * @param columnIndex
     *            the column being queried
     * @return The common classes for the collection members.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < getColumnCount()) {
	    if (columnClasses != null && columnIndex < columnClasses.size()
		    && columnClasses.get(columnIndex) != null) {
		return (Class) columnClasses.get(columnIndex);
	    }
	    Vector commonClasses = null;
	    if (columnIndex >= commonColumnClasses.size()) {
		for (int i = commonColumnClasses.size(); i < getColumnCount(); i++) {
		    commonColumnClasses.addElement(null);
		}
	    }
	    if (commonColumnClasses.elementAt(columnIndex) == null) {
		commonClasses = getCommonClasses(columnIndex);
		commonColumnClasses.setElementAt(commonClasses, columnIndex);
	    } else {
		commonClasses = (Vector) commonColumnClasses
			.elementAt(columnIndex);
	    }
	    if (commonClasses != null && commonClasses.size() > 0) {
		return (Class) commonClasses.elementAt(0);
	    }
	}
	return Object.class;
    }

    /**
     * Specify a class or interface that all Object values of the TableModel
     * column at columnIndex must extend.
     * 
     * @param columnIndex
     *            the column being queried
     * @param columnClass
     *            the java class of the values in this column.
     * @exception ClassCastException
     *                Thrown when a data element in the column is not assignable
     *                to the given columnClass.
     */
    public void setColumnClass(int columnIndex, Class columnClass)
	    throws ClassCastException {
	if (columnIndex >= 0 && columnIndex < getColumnCount()
		&& columnClass != null) {
	    if (columnClasses == null) {
		columnClasses = new Vector(getColumnCount());
	    }
	    if (columnIndex >= columnClasses.size()) {
		columnClasses.setSize(columnIndex + 1);
	    }
	    for (int ri = 0; ri < getRowCount(); ri++) {
		Object obj = getValueAt(ri, columnIndex);
		if (obj != null
			&& !columnClass.isAssignableFrom(obj.getClass())) {
		    throw new ClassCastException("At row " + ri + " " + obj
			    + " (" + obj.getClass()
			    + ") can not be assigned to " + columnClass);
		}
	    }
	    columnClasses.set(columnIndex, columnClass);
	}
    }

    /**
     * Sets the value in the cell at <I>columnIndex</I> and <I>rowIndex</I> to
     * <I>aValue</I> is the new value. This will throw a ClassCastException if
     * the value cannot be assigned to the column class explicitly set by
     * {@link #setColumnClass}.
     * 
     * @param aValue
     *            the new value
     * @param rowIndex
     *            the row whose value is to be changed
     * @param columnIndex
     *            the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     * @see #setColumnClass
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
		&& columnIndex < getColumnCount()) {
	    if (columnClasses != null && columnIndex < columnClasses.size()
		    && columnClasses.get(columnIndex) != null) {
		Class columnClass = (Class) columnClasses.get(columnIndex);
		if (aValue != null
			&& !columnClass.isAssignableFrom(aValue.getClass())) {
		    throw new ClassCastException(aValue + " ("
			    + aValue.getClass() + ") can not be assigned to "
			    + columnClass);
		}
		super.setValueAt(aValue, rowIndex, columnIndex);
	    } else {
		super.setValueAt(aValue, rowIndex, columnIndex);
		if (aValue != null && columnIndex >= 0
			&& columnIndex <= commonColumnClasses.size()
			&& commonColumnClasses.elementAt(columnIndex) != null) {
		    Vector cl = getObjectClasses(aValue);
		    getCommonClasses(columnIndex).retainAll(cl);
		}
	    }
	}
    }

    /**
     * Replaces the data in the table, and removes any column class constraints
     * previously specified by {@link #setColumnClass}.
     * 
     * @param dataVector
     *            the new data vector
     * @param columnIdentifiers
     *            the names of the columns
     * @see DefaultTableModel#setDataVector(Vector,Vector)
     * @see #setColumnClass
     */
    @Override
    public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
	columnClasses = null;
	super.setDataVector(dataVector, columnIdentifiers);
    }

    /**
     * Insert a new row into the table.
     * 
     * @param row
     * @param rowData
     * @exception ClassCastException
     *                Thrown when a data element in the row is not assignable to
     *                the given columnClass for the corresponding column.
     * @see #setColumnClass
     */
    @Override
    public void insertRow(int row, Vector rowData) {
	verifyRowClasses(rowData);
	super.insertRow(row, rowData);
    }

    /**
     * Insert a new column into the table.
     * 
     * @param column
     *            The index of the column position at which to insert the column
     *            into the table.
     * @param columnName
     *            The name of the column being added.
     * @param columnClass
     *            The java Class type of the column being added.
     */
    public void insertColumn(int column, String columnName, Class columnClass) {
	if (column < 0) {
	} else if (column >= getColumnCount()) {
	    addColumn(columnName);
	    if (columnClasses != null) {
		columnClasses.add(column, columnClass != null ? columnClass
			: java.lang.Object.class);
	    }
	} else {
	    Vector data = getDataVector();
	    Vector cols = new Vector();
	    for (int i = 0; i < getColumnCount(); i++) {
		cols.add(getColumnName(i));
	    }
	    cols.add(column, columnName);
	    for (int i = 0; i < data.size(); i++) {
		Vector row = (Vector) data.get(i);
		row.add(column, null);
	    }
	    super.setDataVector(data, cols);
	    if (columnClasses != null) {
		columnClasses.add(column, columnClass != null ? columnClass
			: java.lang.Object.class);
	    }
	}
    }

    /**
     * Delete a column from the table.
     * 
     * @param column
     *            The index of the column to delete from the table.
     */
    public void deleteColumn(int column) {
	int[] cols = new int[1];
	cols[0] = column;
	deleteColumns(cols);
    }

    /**
     * Delete columns from the table.
     * 
     * @param columns
     *            The indices of the columns to delete from the table.
     */
    public void deleteColumns(int[] columns) {
	if (columns == null) {
	    return;
	}
	Arrays.sort(columns);
	Vector data = getDataVector();
	Vector cols = new Vector();
	for (int i = 0; i < getColumnCount(); i++) {
	    cols.add(getColumnName(i));
	}
	for (int ci = columns.length - 1; ci >= 0; ci--) {
	    int column = columns[ci];
	    if (column >= 0 && column < getColumnCount()) {
		cols.remove(column);
		for (int i = 0; i < data.size(); i++) {
		    Vector row = (Vector) data.get(i);
		    row.remove(column);
		}
		if (columnClasses != null && column < columnClasses.size()) {
		    columnClasses.remove(column);
		}
	    }
	}
	super.setDataVector(data, cols);
    }

    /**
     * Verify that one row of data values from the table given by rowData
     * matches the column classes that were specified with
     * {@link #setColumncClass}.
     * 
     * @param rowData
     *            The date values of a row in the table.
     * @exception ClassCastException
     *                Thrown when a data element in the row is not assignable to
     *                the given columnClass for the corresponding column.
     */
    private void verifyRowClasses(Vector rowData) throws ClassCastException {
	if (columnClasses != null) {
	    for (int ci = 0; ci < rowData.size() && ci < columnClasses.size()
		    && ci < getColumnCount(); ci++) {
		Object obj = rowData.get(ci);
		if (obj != null) {
		    Class columnClass = (Class) columnClasses.get(ci);
		    if (!columnClass.isAssignableFrom(obj.getClass())) {
			throw new ClassCastException("At column " + ci + " "
				+ obj + " (" + obj.getClass()
				+ ") can not be assigned to " + columnClass);
		    }
		}
	    }
	}
    }
}
