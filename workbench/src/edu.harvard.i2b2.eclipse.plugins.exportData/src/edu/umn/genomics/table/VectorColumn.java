/*
 * @(#) $RCSfile: VectorColumn.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;

/**
 * VectorColumn presents values of a List as a column in a TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see edu.umn.genomics.table.VirtualTableModel
 */
public class VectorColumn implements VirtualColumn {
    protected String name = "";
    Vector values = new Vector();
    boolean cellEditable = true;
    protected Class columnClass = null;
    protected Vector commonColumnClasses = new Vector();

    /**
     * Present the values of a List of cells of a column in a TableModel.
     */
    public VectorColumn() {
    }

    /**
     * Present the values of a List as cells of a column in a TableModel.
     * 
     * @param columnName
     *            The name for this column.
     * @param columnClass
     *            The java class that all values for this column must extend, if
     *            null there is no restriction.
     */
    public VectorColumn(String columnName, Class columnClass) {
	this(columnName, columnClass, null);
    }

    /**
     * Present the values of a List as cells of a column in a TableModel.
     * 
     * @param columnName
     *            The name for this column.
     * @param columnClass
     *            The java class that all values for this column must extend, if
     *            null there is no restriction.
     * @param values
     *            The data values for the column.
     */
    public VectorColumn(String columnName, Class columnClass, List values) {
	setName(columnName);
	setColumnClass(columnClass);
	if (values != null) {
	    this.values = new Vector(values);
	}
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column. This is used by the <code>JTable</code> to set up a default
     * renderer and editor for the column.
     * 
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass() {
	return columnClass != null ? columnClass : commonColumnClasses != null
		&& commonColumnClasses.size() > 0 ? (Class) commonColumnClasses
		.get(0) : java.lang.Object.class;
    }

    /**
     * Specify a class or interface that all Object values of the TableModel
     * column at columnIndex must extend.
     * 
     * @param columnClass
     *            the java class of the values in this column.
     * @exception ClassCastException
     *                Thrown when a data element in the column is not assignable
     *                to the given columnClass.
     */
    public void setColumnClass(Class columnClass) throws ClassCastException {
	if (columnClass != null && values != null) {
	    for (int ri = 0; ri < values.size(); ri++) {
		Object obj = values.get(ri);
		if (obj != null
			&& !columnClass.isAssignableFrom(obj.getClass())) {
		    throw new ClassCastException("At row " + ri + " " + obj
			    + " (" + obj.getClass()
			    + ") can not be assigned to " + columnClass);
		}
	    }
	}
	this.columnClass = columnClass;
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
     * @return The common classes for the column cells.
     */
    public Vector getCommonClasses() {
	Vector commonClasses = null;
	if (values != null) {
	    for (int row = 0; row < values.size(); row++) {
		Object o = values.get(row);
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
     * Set whether cells in this column should be editable.
     * 
     * @param b
     *            Whether cells in this column should be editable.
     */
    public void setCellEditable(boolean b) {
	cellEditable = b;
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code> is editable. Otherwise, <code>setValueAt</code>
     * on the cell will not change the value of that cell.
     * 
     * @param rowIndex
     *            the row whose value to be queried
     * @param columnIndex
     *            the column whose value to be queried
     * @return true if the cell is editable
     * @see #setValueAt
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return cellEditable;
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     * 
     * @param aValue
     *            the new value
     * @param rowIndex
     *            the row whose value is to be changed
     * @param columnIndex
     *            the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (isCellEditable(rowIndex, columnIndex)) {
	    if (values == null) {
		values = new Vector();
	    }
	    if (rowIndex >= values.size()) {
		values.setSize(rowIndex + 1);
	    }
	    if (columnClass != null) {
		if (aValue != null
			&& !columnClass.isAssignableFrom(aValue.getClass())) {
		    throw new ClassCastException(aValue + " ("
			    + aValue.getClass() + ") can not be assigned to "
			    + columnClass);
		}
	    } else if (aValue == null) { // commonClass could get more specific
		Object prev = values.get(rowIndex);
		if (prev != null) {
		    commonColumnClasses = getCommonClasses();
		}
	    } else { // commonClass could get less specific
		Vector cl = getObjectClasses(aValue);
		if (commonColumnClasses == null) {
		    commonColumnClasses = cl;
		} else {
		    commonColumnClasses.retainAll(cl);
		}
	    }
	    values.set(rowIndex, aValue);
	}
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (values != null && rowIndex < values.size()) {
	    return values.get(rowIndex);
	}
	return null;
    }

    /**
     * Get a name for this column.
     * 
     * @return The name given to this cell.
     */
    public String getName() {
	return name;
    }

    /**
     * Set the name for this column.
     * 
     * @param name
     *            The name given to this cell.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Get a type name for this cell.
     * 
     * @return The type name for this cell.
     */
    public String getType() {
	Class theClass = getColumnClass();
	return theClass != null ? theClass.getName().substring(
		theClass.getPackage().getName().length() + 1) : "Unknown";
    }

    /**
     * Get a description of this VirtualCell.
     * 
     * @return a description of this VirtualCell.
     */
    public String getDescription() {
	return getType();
    }

}
