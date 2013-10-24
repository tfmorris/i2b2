/*
 * @(#) $RCSfile: CollectionTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A CollectionTableModel object provides a TableModel interface to a
 * Collection. The Collection itself, or fields and methods of the most specific
 * common Class for all members of the Collection, may be used as columns of the
 * TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public class CollectionTableModel extends AbstractTableModel implements
	Serializable {
    /** A LinkedList copy of the input collection in order to access by index. */
    LinkedList collection;
    /** The most specific Class common to all members of the Collection. */
    Class commonClass;
    /** All classes common to all members of the Collection. */
    LinkedList commonClasses = null;
    /** All interfaces common to all members of the Collection. */
    HashSet commonInterfaces = null;
    /**
     * Columns defined for the TableModel. A column will be a Method, a Field,
     * or the collection itself.
     */
    LinkedList column = new LinkedList();

    /**
     * Create a TableModel interface for the given collection. The collection is
     * copied, so changes to the given collection won't be reflected in the
     * TableModel.
     * 
     * @param collection
     *            The Collection to be presented as a TableModel.
     */
    public CollectionTableModel(Collection collection) {
	this.collection = new LinkedList(collection);
	commonClasses = getCommonClasses(collection);
	commonClass = getCommonClass(collection);
	commonInterfaces = getCommonInterfaces(collection);
    }

    /**
     * Return the interfaces that are common to all members of the given
     * collection.
     * 
     * @param collection
     *            The Collection to examine.
     * @return The common interfaces for the collection members.
     */
    public static HashSet getCommonInterfaces(Collection collection) {
	HashSet commonInterfaces = null;
	if (collection != null && collection.size() > 0) {
	    for (Iterator i = collection.iterator(); i.hasNext();) {
		Object o = i.next();
		Class ic[] = o.getClass().getInterfaces();
		HashSet ichs = new HashSet();
		for (int j = 0; j < ic.length; j++) {
		    ichs.add(ic[j]);
		}
		if (commonInterfaces == null) {
		    commonInterfaces = ichs;
		} else {
		    commonInterfaces.retainAll(ichs);
		}
	    }
	}
	return commonInterfaces;
    }

    /**
     * Return the classes that are common to all members of the given
     * collection.
     * 
     * @param collection
     *            The Collection to examine.
     * @return The common classes for the collection members.
     */
    public static LinkedList getCommonClasses(Collection collection) {
	LinkedList commonClasses = null;
	if (collection != null && collection.size() > 0) {
	    for (Iterator i = collection.iterator(); i.hasNext();) {
		Object o = i.next();
		LinkedList cl = new LinkedList();
		for (Class cc = o.getClass(); cc != null; cc = cc
			.getSuperclass()) {
		    cl.add(cc);
		}
		if (commonClasses == null) {
		    commonClasses = cl;
		} else {
		    commonClasses.retainAll(cl);
		}
	    }
	}
	return commonClasses;
    }

    /**
     * Return the most specific class that is common to all members of the given
     * collection.
     * 
     * @param collection
     *            The Collection to examine.
     * @return The common classes for the collection members.
     */
    public static Class getCommonClass(Collection collection) {
	LinkedList commonClasses = getCommonClasses(collection);
	if (commonClasses != null && commonClasses.size() > 0) {
	    return (Class) commonClasses.getFirst();
	}
	return null;
    }

    /**
     * Return the most specific class that is common to the collection.
     * 
     * @return The common classes for the collection members.
     */
    public Class getCommonClass() {
	if (commonClass == null) {
	    commonClass = getCommonClass(collection);
	}
	return commonClass;
    }

    /**
     * Return the classes that are common to all members of the collection.
     * 
     * @return The common classes for the collection members.
     */
    public LinkedList getCommonClasses() {
	if (commonClasses == null) {
	    commonClasses = getCommonClasses(collection);
	}
	return commonClasses;
    }

    /**
     * Return the interfaces that are common to all members of the collection.
     * 
     * @return The common interfaces for the collection members.
     */
    public HashSet getCommonInterfaces() {
	if (commonInterfaces == null) {
	    commonInterfaces = getCommonInterfaces(collection);
	}
	return commonInterfaces;
    }

    /**
     * Add the collection as a column in the TableModel.
     */
    public void addColumn() {
	column.add(collection);
	fireTableStructureChanged();
    }

    /**
     * Add the given method of the common class as a column in the TableModel.
     * 
     * @param m
     *            A method of the common class of the collection.
     * @see #getCommonClass()
     */
    public void addColumn(Method m) {
	column.add(m);
	fireTableStructureChanged();
    }

    /**
     * Add the given field of the common class as a column in the TableModel.
     * 
     * @param f
     *            A field of the common class of the collection.
     * @see #getCommonClass()
     */
    public void addColumn(Field f) {
	column.add(f);
	fireTableStructureChanged();
    }

    /**
     * Add the collection as a column in the TableModel.
     */
    public void addColumn(TableModelFormula formula) {
	column.add(formula);
	fireTableStructureChanged();
    }

    /**
     * Remove the given field or method from the columns of the TableModel. This
     * may alter the indices of remaining columns.
     * 
     * @param o
     *            A field or method that was previously added as a column.
     * @see #addColumn()
     * @see #addColumn(Field)
     * @see #addColumn(Method)
     */
    public void removeColumn(Object o) {
	column.remove(o);
	fireTableStructureChanged();
    }

    /**
     * Remove the column at the given index from the columns of the TableModel.
     * This may alter the indices of remaining columns.
     * 
     * @param index
     *            The index of a column of the TableModel..
     * @see #addColumn()
     * @see #addColumn(Field)
     * @see #addColumn(Method)
     */
    public void removeColumn(int index) {
	column.remove(index);
	fireTableStructureChanged();
    }

    /**
     * Get the value of each argument to the method.
     * 
     */
    public Object[] getMethodArgs(Method m, Object o, int rowIndex,
	    int columnIndex) {
	// Not yet implemented, just return 0 length args for now.
	Object[] args = new Object[0];
	return args;
    }

    /**
     * Returns the number of rows in the model. A <B>JTable</B> uses this method
     * to determine how many rows it should display. This method should be
     * quick, as it is called frequently during rendering.
     * 
     * @return the number or rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
	if (collection == null)
	    return 0;
	return collection.size();
    }

    /**
     * Returns the number of columns in the model. A <B>JTable</B> uses this
     * method to determine how many columns it should create and display by
     * default.
     * 
     * @return the number or columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
	if (column == null)
	    return 0;
	return column.size();
    }

    /**
     * Returns the name of the column at <i>columnIndex</i>. This is used to
     * initialize the table's column header name. Note: this name does not need
     * to be unique; two columns in a table can have the same name.
     * 
     * @param columnIndex
     *            the index of column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
	if (column != null && columnIndex >= 0 && columnIndex < column.size()) {
	    Object o = column.get(columnIndex);
	    if (o instanceof Field) {
		return ((Field) o).getName();
	    } else if (o instanceof Method) {
		return ((Method) o).getName();
	    } else if (o == collection) {
		return getCommonClass().getName();
	    } else {
		return o.toString();
	    }
	}
	return null;
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column. This is used by the JTable to set up a default renderer and
     * editor for the column.
     * 
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
	Class cc = null;
	if (column != null && columnIndex >= 0 && columnIndex < column.size()) {
	    Object o = column.get(columnIndex);
	    if (o instanceof Field) {
		cc = ((Field) o).getType();
	    } else if (o instanceof Method) {
		cc = ((Method) o).getReturnType();
	    } else if (o instanceof TableModelFormula) {
		cc = (new Object()).getClass();
	    } else {
		cc = getCommonClass();
	    }
	}
	if (cc != null && cc.isPrimitive()) {
	    try {
		if (cc.equals(Boolean.TYPE))
		    return Class.forName("java.lang.Boolean");
		if (cc.equals(Character.TYPE))
		    return Class.forName("java.lang.Character");
		if (cc.equals(Byte.TYPE))
		    return Class.forName("java.lang.Byte");
		if (cc.equals(Short.TYPE))
		    return Class.forName("java.lang.Short");
		if (cc.equals(Integer.TYPE))
		    return Class.forName("java.lang.Integer");
		if (cc.equals(Long.TYPE))
		    return Class.forName("java.lang.Long");
		if (cc.equals(Float.TYPE))
		    return Class.forName("java.lang.Float");
		if (cc.equals(Double.TYPE))
		    return Class.forName("java.lang.Double");
		if (cc.equals(Void.TYPE))
		    return Class.forName("java.lang.Void");
	    } catch (ClassNotFoundException cnfe) {
		System.err.println(cnfe);
	    }
	}
	return cc;
    }

    /**
     * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I> is
     * editable. Otherwise, setValueAt() on the cell will not change the value
     * of that cell.
     * 
     * @param rowIndex
     *            the row whose value is to be looked up
     * @param columnIndex
     *            the column whose value is to be looked up
     * @return true if the cell is editable.
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }

    /**
     * Returns the value for the cell at <I>columnIndex</I> and <I>rowIndex</I>.
     * 
     * @param rowIndex
     *            the row whose value is to be looked up
     * @param columnIndex
     *            the column whose value is to be looked up
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (collection != null && rowIndex >= 0 && rowIndex < collection.size()
		&& column != null && columnIndex >= 0
		&& columnIndex < column.size()) {
	    try {
		Object o = collection.get(rowIndex);
		Object co = column.get(columnIndex); // column Object
		if (co instanceof Field) {
		    Class cc = ((Field) co).getType();
		    if (cc.isPrimitive()) {
			if (cc.equals(Boolean.TYPE))
			    return new Boolean(((Field) co).getBoolean(o));
			if (cc.equals(Character.TYPE))
			    return new Character(((Field) co).getChar(o));
			if (cc.equals(Byte.TYPE))
			    return new Byte(((Field) co).getByte(o));
			if (cc.equals(Short.TYPE))
			    return new Short(((Field) co).getShort(o));
			if (cc.equals(Integer.TYPE))
			    return new Integer(((Field) co).getInt(o));
			if (cc.equals(Long.TYPE))
			    return new Long(((Field) co).getLong(o));
			if (cc.equals(Float.TYPE))
			    return new Float(((Field) co).getFloat(o));
			if (cc.equals(Double.TYPE))
			    return new Double(((Field) co).getDouble(o));
			if (cc.equals(Void.TYPE))
			    return null;
		    } else {
			return ((Field) co).get(o);
		    }
		} else if (co instanceof Method) {
		    // Need to deal with method args
		    Method m = (Method) co;
		    // System.err.println(m.getName() + " " +
		    // m.getParameterTypes().length);
		    return m.invoke(o, getMethodArgs(m, o, rowIndex,
			    columnIndex));
		} else if (co instanceof TableModelFormula) {
		    return ((TableModelFormula) co).getValueAt(rowIndex,
			    columnIndex);
		} else {
		    return o;
		}
	    } catch (Exception e) {
		System.err.println(this.getClass() + ".getValueAt(" + rowIndex
			+ "," + columnIndex + ") " + e);
	    }
	}
	return null;
    }

    /**
     * Sets the value in the cell at <I>columnIndex</I> and <I>rowIndex</I> to
     * <I>aValue</I> is the new value.
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
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

}
